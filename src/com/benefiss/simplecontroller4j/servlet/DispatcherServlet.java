package com.benefiss.simplecontroller4j.servlet;

import java.beans.IntrospectionException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.benefiss.simplecontroller4j.annotation.JoinPoint;
import com.benefiss.simplecontroller4j.annotation.Method;
import com.benefiss.simplecontroller4j.aspect.Aspect;
import com.benefiss.simplecontroller4j.aspectmapping.AspectMapping;
import com.benefiss.simplecontroller4j.binder.DataBinder;
import com.benefiss.simplecontroller4j.execute.Executor;
import com.benefiss.simplecontroller4j.requestmapping.RequestMapping;
import com.benefiss.simplecontroller4j.routing.Router;
/**
 * <p>ディスパッチサーブレット</p>
 * <pre>
 * MVCモデルでのフロントコントローラとして動作し、各コントローラへ処理をディスパッチする。
 * コントローラクラスとそのメソッドには、以下のアノテーションを記述する。
 * ＜アノテーション＞
 *     1. Controller：コントローラマーカーアノテーション
 *                    このアノテーションを記述するとコントローラとして認識される。
 *                    （例）[at]Controller
 *                         public class void SampleController(...) {
 *     2. Route：ルーティングマッピングパスアノテーション
 *               リクエストで実行する[コンテキスト][パス]の[パス]を定義する。
 *               methodにリクセストメソッドをマッピングする。
 *               例：[at]Route(path = "/init", method = Method.GET)
 * </pre>
 * @author  Shigeru Kuratani
 * @version 0.0.1
 */
public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * <p>serviceメソッド（ディスパッチ処理）</p>
     *
     * @throws ServletException ルーティング・バインディング・ディスパッチ処理で例外発生した場合に、ServletExceptionで例外をラップしてスロー
     * @throws IOException リソースファイルの入出力に異常が発生した場合
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
                           throws ServletException, IOException {

    	// classesディレクトリ絶対パス
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        // リクエストパス
        String requestPath = request.getServletPath();
        // リクエストメソッド
        Method requestMethod = getRequestMethod(request);

        //-------------------------------------------//
        // リソースファイルの場合はファイル内容を返却
        //-------------------------------------------//
        Pattern pattern = Pattern.compile("^/[\\s\\S]+\\.[a-zA-Z]{1,}$");
		Matcher matcher = pattern.matcher(requestPath);
		if (matcher.find()) {
			responseFileContent(getServletContext().getRealPath(requestPath), response);
			return;
		}

		//-------------------------------------------//
		// ルーティング探索（ルーティングクラス・メソッド探索）
		//-------------------------------------------//
		RequestMapping requestMapping;
		try {
			requestMapping = new Router().findRoutingClass(requestPath, requestMethod, classesPath);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e.getMessage(), e);
		}

		//-------------------------------------------//
		// データバインディング処理
		//-------------------------------------------//
		List<Map<String, Object>> bidingList;
		try {
			bidingList = new DataBinder().getBidingList(getServletContext(), request, response, requestPath, requestMapping);
		} catch (IllegalArgumentException | InvocationTargetException | IntrospectionException |
				 InstantiationException | IllegalAccessException e) {
			throw new ServletException(e.getMessage(), e);
		}

		//-------------------------------------------//
		// アスペクト探索（アスペクトクラス・メソッド探索）
		//-------------------------------------------//
		List<AspectMapping> aspectMappingList;
		try {
			aspectMappingList = new Aspect().findAspectClass(requestMapping, classesPath);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e.getMessage(), e);
		}

		// エグゼキューター
		Executor executor = new Executor();

		//-------------------------------------------//
		// アスペクト実行(BEFORE・AROUND)
		//-------------------------------------------//
		try {
			executor.executeAspect(request, response, aspectMappingList, JoinPoint.AROUND);
			executor.executeAspect(request, response, aspectMappingList, JoinPoint.BEFORE);
		} catch (InstantiationException | IllegalAccessException |
				 IllegalArgumentException | InvocationTargetException e) {
			throw new ServletException(e.getMessage(), e);
		}

		//-------------------------------------------//
		// ディスパッチ（ルーティグメソッド実行）
		//-------------------------------------------//
		String responseString = null;
		try {
			responseString = executor.executeMethod(requestMapping, bidingList);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			throw new ServletException(e.getMessage(), e);
		} catch (InvocationTargetException ite) {
			try {
				//-------------------------------------------//
				// アスペクト実行(AFTER_THROWING)
				//-------------------------------------------//
				executor.executeAspect(request, response, aspectMappingList, JoinPoint.AFTER_THROWING);
				throw new ServletException(ite.getMessage(), ite);
			} catch (InstantiationException | IllegalAccessException |
					 IllegalArgumentException | InvocationTargetException e) {
				throw new ServletException(e.getMessage(), e);
			}
		}

		//-------------------------------------------//
		// アスペクト実行(AFTER_RETURNING)
		//-------------------------------------------//
		if (responseString != null) {
			try {
				executor.executeAspect(request, response, aspectMappingList, JoinPoint.AFTER_RETURNING);
			} catch (InstantiationException | IllegalAccessException |
					 IllegalArgumentException | InvocationTargetException e) {
				throw new ServletException(e.getMessage(), e);
			}
		}

		//-------------------------------------------//
		// アスペクト実行(AFTER・AROUND)
		//-------------------------------------------//
		try {
			executor.executeAspect(request, response, aspectMappingList, JoinPoint.AFTER);
			executor.executeAspect(request, response, aspectMappingList, JoinPoint.AROUND);
		} catch (InstantiationException | IllegalAccessException |
				 IllegalArgumentException | InvocationTargetException e) {
			throw new ServletException(e.getMessage(), e);
		}

		//-------------------------------------------//
		// レスポンス文字列リターン
		//-------------------------------------------//
		if (responseString != null) {
			PrintWriter out = response.getWriter();
			out.print(responseString);
			out.flush();
		}
    }

    /**
     * <p>リクエストメソッド取得</p>
     *
     * @param  request HTTPサーブレットリクエスト
     * @return リクエストメソッド
     */
    protected Method getRequestMethod(HttpServletRequest request) {

    	//---------------------------------------//
    	// リクエストメソッド判定
    	//---------------------------------------//
    	Method requestMethod = Method.GET; // HTTPメソッド（初期値）
		if ("GET".equals(request.getMethod())) {
			requestMethod = Method.GET;
		} else if ("POST".equals(request.getMethod())) {
			requestMethod = Method.POST;
		} else if ("PUT".equals(request.getMethod())) {
			requestMethod = Method.PUT;
		} else if ("DELETE".equals(request.getMethod())) {
			requestMethod = Method.DELETE;
		} else if ("OPTIONS".equals(request.getMethod())) {
			requestMethod = Method.OPTIONS;
		} else if ("TRACE".equals(request.getMethod())) {
			requestMethod = Method.TRACE;
		}
		return requestMethod;
    }

    /**
	 * <p>リソースファイル レスポンス処理</p>
	 *
	 * @param  filePath リソースファイルパス
	 * @param  response レスポンスオブジェクト
	 * @throws FileNotFoundException リソーフファイルが存在しない場合
	 * @throws IOException リソースファイルの入出力に異常が発生した場合
	 */
    protected void responseFileContent(String filePath, HttpServletResponse response)
									 throws FileNotFoundException, IOException {

		// ファイルインスタンス
		File file = new File(filePath);
		// 入出力ストリーム
		OutputStream os = response.getOutputStream();
		BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));

		// ファイル出力
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = reader.read(buffer)) >= 0) {
            os.write(buffer, 0, len);
        }

        // リソースクローズ
        reader.close();
	}

}
