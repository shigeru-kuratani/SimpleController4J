package net.skuratani.simplecontroller4j.annotation;

/**
 * <p>HTTPリクエストメソッド列挙型</p>
 * <pre>
 * HTTPリクエストメソッドを表現する列挙型です。
 * 以下のHTTメソッドをサポートしています。
 *    1. GET
 *    2. POST
 *    3. PUT
 *    4. DELETE
 *    5. OPTIONS
 *    6. TRACE
 * </pre>
 */
public enum Method {
	GET, POST, PUT, DELETE, OPTIONS, TRACE, ALL
}
