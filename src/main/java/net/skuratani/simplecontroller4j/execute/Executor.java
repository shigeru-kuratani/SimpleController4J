package net.skuratani.simplecontroller4j.execute;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import net.skuratani.simplecontroller4j.annotation.JoinPoint;
import net.skuratani.simplecontroller4j.annotation.PathVariable;
import net.skuratani.simplecontroller4j.annotation.RequestBody;
import net.skuratani.simplecontroller4j.annotation.RequestParam;
import net.skuratani.simplecontroller4j.aspectmapping.AspectMapping;
import net.skuratani.simplecontroller4j.requestmapping.RequestMapping;

/**
 * <p>ルーティングメソッド実行</p>
 * <pre>
 * 以下の情報を使用してディスパッチャサーブレットからメソッドを実行する
 *     1. RouterがリクエストURLから探索した「実行クラス・実行メソッド」
 *     2. DataBinderが生成した実行メソッド引数値リスト
 * </pre>
 *
 * @author Shigeru Kuratani
 * @version 0.0.3
 */
public class Executor {

	/**
	 * <p>
	 * RequestMethodメソッド実行
	 * </p>
	 *
	 * @param requestMapping リクエスト情報
	 * @param bidingList     バインド値リスト
	 * @return responseString レスポンス文字列
	 * @throws InstantiationException    実行クラスのインスタンス生成に失敗した場合
	 * @throws IllegalAccessException    実行メソットアクセスに異常が発生した場合
	 * @throws IllegalArgumentException  実行メソッッド実行の引数に不正がある場合
	 * @throws InvocationTargetException 実行メソッドがスローする例外をラップする例外
	 * @throws SecurityException         セキュリティ侵害が発生した場合
	 * @throws NoSuchMethodException     パラメータ格納インスタンスの生成に失敗した場合
	 */
	public String executeMethod(RequestMapping requestMapping, List<Map<String, Object>> bidingList)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
				   NoSuchMethodException, SecurityException {
		// 実行クラス
		Class<?> clazz = requestMapping.getRequestClass();
		// 実行メソッド
		Method method = requestMapping.getRequestMethod();
		// レスポンス文字列
		String responseString = null;
		// ------------------------------------------//
		// メソッド実行
		// ------------------------------------------//
		List<Object> arguments = new ArrayList<>();
		Parameter[] parameters = method.getParameters();
		for (Parameter parameter : parameters) {
			for (Map<String, Object> bindArg : bidingList) {
				for (Annotation annotation: parameter.getDeclaredAnnotations()) {
					if (annotation.annotationType().equals(PathVariable.class)
					    && ((PathVariable) annotation).value().equals(bindArg.entrySet().iterator().next().getKey())) {
						arguments.add(bindArg.entrySet().iterator().next().getValue());	
					}
					if (annotation.annotationType().equals(RequestParam.class)
					    && ((RequestParam) annotation).value().equals(bindArg.entrySet().iterator().next().getKey())) {
						arguments.add(bindArg.entrySet().iterator().next().getValue());	
					}
					if (annotation.annotationType().equals(RequestBody.class)
					&& bindArg.entrySet().iterator().next().getKey().equals("body")) {
						arguments.add(bindArg.entrySet().iterator().next().getValue());	
					}
				}
				if (parameter.getType() == ServletContext.class
					&& bindArg.entrySet().iterator().next().getKey().equals("context")) {
					arguments.add(bindArg.entrySet().iterator().next().getValue());
				}
				if (parameter.getType() == HttpServletRequest.class
					&& bindArg.entrySet().iterator().next().getKey().equals("request")) {
					arguments.add(bindArg.entrySet().iterator().next().getValue());
				}
				if (parameter.getType() == HttpServletResponse.class
					&& bindArg.entrySet().iterator().next().getKey().equals("response")) {
					arguments.add(bindArg.entrySet().iterator().next().getValue());
				}
			}
		}
		Object obj = clazz.getConstructor().newInstance();
		Object[] args = arguments.toArray(new Object[arguments.size()]);
		responseString = (String) method.invoke(obj, args);

		return responseString;
	}

	/**
	 * <p>
	 * RequestMethodメソッド実行
	 * </p>
	 *
	 * @param request           HTTPリクエスト
	 * @param response          HTTPレスポンス
	 * @param aspectMappingList アスペクトマッピング情報
	 * @param joinPoint         ジョインポイント
	 * @throws InstantiationException    実行クラスのインスタンス生成に失敗した場合
	 * @throws IllegalAccessException    実行メソットアクセスに異常が発生した場合
	 * @throws IllegalArgumentException  実行メソッッド実行の引数に不正がある場合
	 * @throws InvocationTargetException 実行メソッドがスローする例外をラップする例外
	 * @throws SecurityException         セキュリティ侵害が発生した場合
	 * @throws NoSuchMethodException     パラメータ格納インスタンスの生成に失敗した場合
	 */
	public void executeAspect(HttpServletRequest request, HttpServletResponse response,
							  List<AspectMapping> aspectMappingList, JoinPoint joinPoint)
							  throws InstantiationException, IllegalAccessException,
							  		 IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
									 SecurityException {

		for (AspectMapping aspectMapping : aspectMappingList) {
			if (aspectMapping.getJoinPoint() == joinPoint) {
				// 実行クラス
				Class<?> clazz = aspectMapping.getAspectClass();
				// 実行メソッド
				Method method = aspectMapping.getAspectMethod();
				// データバインド
				List<Object> arguments = new ArrayList<>();
				for (Parameter param : aspectMapping.getAspectMethod().getParameters()) {
					if (param.getType() == ServletRequest.class) {
						arguments.add((ServletRequest) request);
					}
					if (param.getType() == ServletResponse.class) {
						arguments.add((ServletResponse) response);
					}
				}
				// メソッド実行
				Object obj = clazz.getConstructor().newInstance();
				Object[] args = arguments.toArray(new Object[arguments.size()]);
				method.invoke(obj, args);
			}
		}
	}
}
