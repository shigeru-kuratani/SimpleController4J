package com.benefiss.simplecontroller4j.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.benefiss.simplecontroller4j.annotation.JoinPoint;
import com.benefiss.simplecontroller4j.aspectmapping.AspectMapping;
import com.benefiss.simplecontroller4j.requestmapping.RequestMapping;

/**
 * <p>ルーティングメソッド実行</p>
 * <pre>
 * 以下の情報を使用してディスパッチャサーブレットからメソッドを実行する
 *     1. RouterがリクエストURLから探索した「実行クラス・実行メソッド」
 *     2. DataBinderが生成した実行メソッド引数値リスト
 * </pre>
 *
 * @author Shigeru Kuratani
 * @version 0.0.1
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
	 */
	public String executeMethod(RequestMapping requestMapping, List<Map<String, Object>> bidingList)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
				if (parameter.getName().equals(bindArg.entrySet().iterator().next().getKey())) {
					arguments.add(bindArg.entrySet().iterator().next().getValue());
				}
			}
		}
		Object obj = clazz.newInstance();
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
	 */
	public void executeAspect(HttpServletRequest request, HttpServletResponse response,
							  List<AspectMapping> aspectMappingList, JoinPoint joinPoint)
							  throws InstantiationException, IllegalAccessException,
							  		 IllegalArgumentException, InvocationTargetException {

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
				Object obj = clazz.newInstance();
				Object[] args = arguments.toArray(new Object[arguments.size()]);
				method.invoke(obj, args);
			}
		}
	}
}
