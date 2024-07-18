package net.skuratani.simplecontroller4j.requestmapping;

import java.lang.reflect.Method;

import lombok.Data;

/**
 * <p>リクエストマッピング情報クラス</p>
 * <pre>
 * 以下の情報を格納するリクエスト情報クラス。
 *     1. 実行クラス
 *     2. 実行メソッド
 *     3. 実行クラスマッピングパス
 *        (e.g.)/userlist/init/&lt;count:int&gt;/&lt;column:int&gt;/&lt;device&gt;
 * </pre>
 */
@Data
public class RequestMapping {

	/** リクエストクラス */
	private Class<?> requestClass;

	/** リエクストメソッド */
	private Method requestMethod;

	/** 実行メソッドパス */
	private String executeMethodPath;

}
