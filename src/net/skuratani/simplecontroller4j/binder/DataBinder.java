package net.skuratani.simplecontroller4j.binder;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.skuratani.simplecontroller4j.requestmapping.RequestMapping;

/**
 * <p>データバインダクラス</p>
 * <pre>
 * 以下の情報から実行メソッドの引数にバインドする情報（キー：引数名、値：各パラメータ値）を生成する。
 *     1. URLパスと実行メソッドパス
 *        (ex.) URLパス：/userlist/init/100/3/pc
 *            実行メソッドパス：/userlist/init/&lt;count:int&gt;/&lt;column:int&gt;/&lt;device&gt;
 *     2. リクエストパラメータと実行メソッド引数
 *        ※ GETパラメータ・POSTパラメータ共にバインドリストが生成される。
 *        (ex.) リクエストパラメータ：/userlist/init?count=100&amp;column=3&amp;device=pc
 *            実行メソッド引数：public void init(int count, int column, String device)
 *     3. HTTPサーブレットリクエスト・HTTPサーブレットレスポンスと実行メソッド引数
 *        (ex.)public list(HttpServletRequest request, HttpServletResponse response)
 * 生成したバインドリストはディスパッチャサーブレットに返却され、ディスパッチ先のメソッド実行の時に、
 * パラメータとして使用される。
 * また、上記2では実行メソッド引数に存在しないパラメータを送信しても無視される。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.3
 */
public class DataBinder {

	/**
	 * <p>バインドリスト生成</p>
	 *
	 * @param  context        サーブレットコンテキスト
	 * @param  request        HTTPサーブレットリクエスト
	 * @param  response       HTTPサーブレットレスポンス
	 * @param  requestPath    リクエストパス
	 * @param  requestMapping リクエスト情報
	 * @return bidingList     バインドリスト
	 * @throws InstantiationException    パラメータ引数クラスのインスタンス生成に失敗した場合
	 * @throws IllegalAccessException    パラメータクラスのフィールドアクセスに失敗した場合
	 * @throws IntrospectionException    プロパティーディスクリプタの生成に失敗した場合
	 * @throws InvocationTargetException セッターメソッドの実行に失敗した場合
	 * @throws IllegalArgumentException  セッターメソッドの実行引数に不正がある場合
	 */
	public List<Map<String, Object>> getBidingList(ServletContext context,
												   HttpServletRequest request,
												   HttpServletResponse response,
												   String requestPath,
												   RequestMapping requestMapping)
									 throws InstantiationException, IllegalAccessException,
									        IllegalArgumentException, InvocationTargetException,
									        IntrospectionException {

		// バインディングリスト
		List<Map<String, Object>> bidingList = new ArrayList<>();
		// パスバイディングリスト抽出
		List<Map<String, Object>> pathBidingList = getPathBidingList(requestPath, requestMapping);
		// パラメータバイディングリスト抽出
		List<Map<String, Object>> paramBidingList = getParamBindingList(context, request, response, requestMapping);
		// サーブレットコンテキスト・HTTPサーブレットリクエスト・HTTPサーブレットレスポンスバイディングリスト抽出
		List<Map<String, Object>> paramHttpCxtReqResBidingList = getHttpCxtReqResBidingList(context, request, response, requestMapping);
		// バイディングリスト生成
		bidingList.addAll(pathBidingList);
		bidingList.addAll(paramBidingList);
		bidingList.addAll(paramHttpCxtReqResBidingList);

		return bidingList;
	}

	/**
	 * <p>パスバインドリスト生成</p>
	 *
	 * @param  requestPath    リクエストパス
	 * @param  requestMapping リクエスト情報
	 * @return pathBidingList パスバインドリスト
	 */
	@SuppressWarnings("serial")
	protected List<Map<String, Object>> getPathBidingList(String requestPath, RequestMapping requestMapping) {

		// パスバインディングリスト
		List<Map<String, Object>> pathBidingList = new ArrayList<>();
		// 仮パスバイディングリスト
		List<Map<String, Object>> pathBidingTmpList = new ArrayList<>();

		if (requestMapping.getExecuteMethodPath().matches(".+<.+")) {

			// バインディングキー抽出
			List<String> bidingKeys = extractBindingKey(requestMapping);
			// バインディング値抽出
			List<String> bidingValues = extractBindingValue(requestPath, requestMapping);

			// バイディングリスト生成
			for (int i = 0; i < bidingKeys.size(); i++) {
				Map<String, Object> tmpMap = new HashMap<>();
				tmpMap.put(bidingKeys.get(i), bidingValues.get(i));
				pathBidingTmpList.add(tmpMap);
			}

			//------------------------------------------//
			// バイディング値の変換
			//   変換に失敗した場合は各例外をスロー
			//   1. int   : <XXX:int>
			//   2. float : <XXX:float>
			//   3. regex : <XXX:re:exp>
			//------------------------------------------//
			for (Map<String, Object> bindMap : pathBidingTmpList) {
				Map.Entry<String, Object> entry = bindMap.entrySet().iterator().next();
				// 型指定がない場合
				if (!entry.getKey().matches(".+:.+")) {
					pathBidingList.add(new HashMap<String, Object>() {
						{ put(entry.getKey(), entry.getValue()); }
					});
				// int型の場合
				} else if (entry.getKey().split(":")[1].equals("int")) {
					pathBidingList.add(new HashMap<String, Object>() {
						{ put(entry.getKey().split(":")[0], Integer.parseInt((String) entry.getValue())); }
					});
				// float型の場合
				} else if (entry.getKey().split(":")[1].equals("float")) {
					pathBidingList.add(new HashMap<String, Object>() {
						{ put(entry.getKey().split(":")[0], Float.parseFloat((String) entry.getValue())); }
					});
				// 正規表現の場合
				} else if (entry.getKey().split(":")[1].equals("re")) {
					if (((String) entry.getValue()).matches(entry.getKey().split(":")[2])) {
						pathBidingList.add(new HashMap<String, Object>() {
							{ put(entry.getKey().split(":")[0], entry.getValue()); }
						});
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
		}

		return pathBidingList;
	}

	/**
	 * <p>ルーティングパスからバインド変数名を抽出</p>
	 *
	 * @param  requestMapping リクエストマッピング情報
	 * @return bindingKeys    バインド変数名リスト
	 */
	protected List<String> extractBindingKey(RequestMapping requestMapping) {

		String executeMethodPath = requestMapping.getExecuteMethodPath();
		List<String> bindingKeys = new ArrayList<>();
		boolean inFlag = false;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < executeMethodPath.length(); i++) {
			if (executeMethodPath.charAt(i) == '<') {
				inFlag = true;
				continue;
			}
			if (executeMethodPath.charAt(i) == '>') {
				inFlag = false;
				bindingKeys.add(builder.toString());
				builder.setLength(0);
				continue;
			}
			if (inFlag) {
				builder.append(executeMethodPath.charAt(i));
			}
		}

		return bindingKeys;
	}

	/**
	 * <p>リクエストパスからバインド変数値を抽出</p>
	 *
	 * @param  requestPath    リクエストパス
	 * @param  requestMapping リクエストマッピング情報
	 * @return bindingValues  バインド値リスト
	 */
	protected List<String> extractBindingValue(String requestPath, RequestMapping requestMapping) {

		String executeMethodPath = requestMapping.getExecuteMethodPath();
		int startIndex = executeMethodPath.indexOf("<");

		List<String> bindingValues = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (int i = startIndex; i < requestPath.length(); i++) {
			if (requestPath.charAt(i) == '/') {
				bindingValues.add(builder.toString());
				builder.setLength(0);
				continue;
			}
			builder.append(requestPath.charAt(i));
		}
		if (0 < builder.length()) {
			bindingValues.add(builder.toString());
		}

		return bindingValues;
	}

	/**
	 * <p>パラメータバインドリスト生成</p>
	 *
	 * @param  context  サーブレットコンテキスト
	 * @param  request  HTTPサーブレットリクエスト
	 * @param  response HTTPサーブレットレスポンス
	 * @param  requestMapping リクエストマッピング情報
	 * @return paramBidingList パラメータバインドリスト
	 * @throws InstantiationException    パラメータ引数クラスのインスタンス生成に失敗した場合
	 * @throws IllegalAccessException    パラメータクラスのフィールドアクセスに失敗した場合
	 * @throws IntrospectionException    プロパティーディスクリプタの生成に失敗した場合
	 * @throws InvocationTargetException セッターメソッドの実行に失敗した場合
	 * @throws IllegalArgumentException  セッターメソッドの実行引数に不正がある場合
	 */
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	protected List<Map<String, Object>> getParamBindingList(ServletContext context,
															HttpServletRequest request,
															HttpServletResponse response,
															RequestMapping requestMapping)
									  throws InstantiationException, IllegalAccessException, IntrospectionException,
									  		 IllegalArgumentException, InvocationTargetException {

		// フォームパラメータ取得
		List<Map<String, Object>> paramBidingList = new ArrayList<>();
		Map paramMap = request.getParameterMap();
		Iterator itr = paramMap.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String[] values = (String[]) paramMap.get(key);
			paramBidingList.add(new HashMap() {
				{ put(key, (Object[]) values); }
			});
		}

		// フォームパラメータ型変換
		Method executeMethod = requestMapping.getRequestMethod();
		for (Parameter parameter : executeMethod.getParameters()) {
			for (int i = 0; i <  paramBidingList.size(); i++) {
				Map.Entry<String, Object> entry =  paramBidingList.get(i).entrySet().iterator().next();
				if (parameter.getName().equals(entry.getKey())) {
					if (parameter.getType().isArray()) {
						paramBidingList.set(i, castPramArrayValue(parameter, entry));
					} else {
						paramBidingList.set(i, castPramValue(parameter, entry));
					}
				}
			}
			if (    !parameter.getType().isArray()
				&& (!(parameter.getType() == int.class))
				&& (!(parameter.getType() == long.class))
				&& (!(parameter.getType() == float.class))
				&& (!(parameter.getType() == double.class))
				&& (!(parameter.getType() == String.class))
				&& (!(parameter.getType().isInstance(context)))
				&& (!(parameter.getType().isInstance(request)))
				&& (!(parameter.getType().isInstance(response)))) {
				Object obj = parameter.getType().newInstance();
				for (Field field : parameter.getType().getDeclaredFields()) {
					for (int i = 0; i <  paramBidingList.size(); i++) {
						Map.Entry<String, Object> paramEntry =  paramBidingList.get(i).entrySet().iterator().next();
						if (field.getName().equals(paramEntry.getKey())) {
							PropertyDescriptor prop = new PropertyDescriptor(field.getName(), obj.getClass());
							Method setter = prop.getWriteMethod();
							setter.invoke(obj, castValue(field, ((String[])paramEntry.getValue())[0]));
						}
					}
				}
				paramBidingList.add(new HashMap() {
					{ put(parameter.getName(), obj); }
				});
			}
		}

		return paramBidingList;
	}

	/**
	 * <p>配列パラメータキャスト処理</p>
	 *
	 * @param  parameter       実行メソッドパラメータ
	 * @param  entry           パラメータマップエントリ
	 * @return パラメータ値を実行メソッド型キャストしたマップ
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	protected Map<String, Object> castPramArrayValue(Parameter parameter, Map.Entry<String, Object> entry) {
		// boolean型
		if (parameter.getType().getComponentType() == boolean.class) {
			boolean[] tmpArray = new boolean[((String[]) entry.getValue()).length];
			for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
				tmpArray[i] = Boolean.parseBoolean(((String[]) entry.getValue())[i]);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpArray); } }
			};
		// int型
		} else if (parameter.getType().getComponentType() == int.class) {
			int[] tmpArray = new int[((String[]) entry.getValue()).length];
			for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
				tmpArray[i] = Integer.parseInt(((String[]) entry.getValue())[i]);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpArray); } }
			};
		// long型
		} else if (parameter.getType().getComponentType() == long.class) {
			long[] tmpArray = new long[((String[]) entry.getValue()).length];
			for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
				tmpArray[i] = Long.parseLong(((String[]) entry.getValue())[i]);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpArray); } }
			};
		// float型
		} else if (parameter.getType().getComponentType() == float.class) {
			float[] tmpArray = new float[((String[]) entry.getValue()).length];
			for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
				tmpArray[i] = Float.parseFloat(((String[]) entry.getValue())[i]);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpArray); } }
			};
		// double型
		} else if (parameter.getType().getComponentType() == double.class) {
			double[] tmpArray = new double[((String[]) entry.getValue()).length];
			for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
				tmpArray[i] = Double.parseDouble(((String[]) entry.getValue())[i]);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpArray); } }
			};
		// String型
		} else {
			List<String> tmpList = new ArrayList<>();
			for (String val : (String[]) entry.getValue()) {
				tmpList.add(val);
			}
			return new HashMap() {
				{ { put(entry.getKey(), tmpList.toArray(new String[tmpList.size()])); } }
			};
		}
	}

	/**
	 * <p>パラメータキャスト処理</p>
	 *
	 * @param  parameter       実行メソッドパラメータ
	 * @param  entry           パラメータマップエントリ
	 * @return パラメータ値を実行メソッド型キャストしたマップ
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	protected Map<String, Object> castPramValue(Parameter parameter, Map.Entry<String, Object> entry) {

		// boolean型
		if (parameter.getType() == boolean.class) {
			return new HashMap() {
				{ { put(entry.getKey(), Boolean.parseBoolean(((String[]) entry.getValue())[0])); } }
			};
		// int型
		} else if (parameter.getType() == int.class) {
			return new HashMap() {
				{ { put(entry.getKey(), Integer.parseInt(((String[]) entry.getValue())[0])); } }
			};
		// long型
		} else if (parameter.getType() == long.class) {
			return new HashMap() {
				{ { put(entry.getKey(), Long.parseLong(((String[]) entry.getValue())[0])); } }
			};
		// float型
		} else if (parameter.getType() == float.class) {
			return new HashMap() {
				{ { put(entry.getKey(), Float.parseFloat(((String[]) entry.getValue())[0])); } }
			};
		// double型
		} else if (parameter.getType() == double.class) {
			return new HashMap() {
				{ { put(entry.getKey(), Double.parseDouble(((String[]) entry.getValue())[0])); } }
			};
		// String型
		} else {
			return new HashMap() {
				{ { put(entry.getKey(), String.valueOf(((String[]) entry.getValue())[0])); } }
			};
		}
	}

	/**
	 * <p>値キャスト処理</p>
	 *
	 * @param  field パラメータクラスフィールド
	 * @param  value パラメータ値
	 * @return フィールドタイプに従ってキャストした値
	 */
	protected Object castValue(Field field, Object value) {

		// boolean型
		if (field.getType() == boolean.class) {
			return Boolean.parseBoolean((String) value);
		// int型
		} else if (field.getType() == int.class) {
			return Integer.parseInt((String) value);
		// long型
		} else if (field.getType() == long.class) {
			return Long.parseLong((String) value);
		// float型
		} else if (field.getType() == float.class) {
			return Float.parseFloat((String) value);
		// double型
		} else if (field.getType() == double.class) {
			return Double.parseDouble((String) value);
		// String型
		} else if (field.getType() == String.class) {
			return String.valueOf(value);
		}

		return value;
	}

	/**
	 * <p>HTTPサーブレットリクエスト・レスポンス バインドリスト生成</p>
	 *
	 * @param  context  サーブレットコンテキスト
	 * @param  request  HTTPサーブレットリクエスト
	 * @param  response HTTPサーブレットレスポンス
	 * @param  requestMapping リクエストマッピング情報
	 * @return paramHttpReqResBidingList HTTPサーブレットリクエスト・レスポンス バインドリスト
	 */
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	protected List<Map<String, Object>> getHttpCxtReqResBidingList(ServletContext context,
																   HttpServletRequest request,
			   													   HttpServletResponse response,
			   													   RequestMapping requestMapping) {

		List<Map<String, Object>> paramHttpCxtReqResBidingList = new ArrayList<>();
		for (Parameter param : requestMapping.getRequestMethod().getParameters()) {
			if (param.getType().isInstance(context)) {
				paramHttpCxtReqResBidingList.add(new HashMap() {
					{ put(param.getName(), context); }
				});
			}
			if (param.getType().isInstance(request)) {
				paramHttpCxtReqResBidingList.add(new HashMap() {
					{ put(param.getName(), request); }
				});
			}
			if (param.getType().isInstance(response)) {
				paramHttpCxtReqResBidingList.add(new HashMap() {
					{ put(param.getName(), response); }
				});
			}
		}

		return paramHttpCxtReqResBidingList;
	}

}
