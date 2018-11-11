package com.benefiss.simplecontroller4j.requestmapping;

import java.lang.reflect.Method;

/**
 * <p>リクエストマッピング情報クラス</p>
 * <pre>
 * 以下の情報を格納するリクエスト情報クラス。
 *     1. 実行クラス
 *     2. 実行メソッド
 *     3. 実行クラスマッピングパス
 *        (e.g.)/userlist/init/&lt;count:int&gt;/&lt;column:int&gt;/&lt;device&gt;
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.1
 */
public class RequestMapping {

	/** リクエストクラス */
	private Class<?> requestClass;

	/** リエクストメソッド */
	private Method requestMethod;

	/** 実行メソッドパス */
	private String executeMethodPath;

	public Class<?> getRequestClass() {
		return requestClass;
	}

	public void setRequestClass(Class<?> requestClass) {
		this.requestClass = requestClass;
	}

	public Method getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(Method requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getExecuteMethodPath() {
		return executeMethodPath;
	}

	public void setExecuteMethodPath(String executeMethodPath) {
		this.executeMethodPath = executeMethodPath;
	}
}
