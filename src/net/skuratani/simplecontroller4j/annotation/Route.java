package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * <p>リクエストマッピング アノテーション</p>
 * <pre>
 * ディスパッチャサーブレットがディスパッチするメソッドをマッピングするアノテーションです。
 * 以下の情報をアノテーションに記述して、マッピングを設定します。
 *    1. パス(path)
 *       (ex.)[at]Route(path = "/init")
 *       　　　 → リクエストパスが「[コンテキストパス]/init」の場合に実行されます。
 *    2. メソッド(method)
 *       (ex.)[at]Route(path = "/init", method = Method.GET)
 *             → リクエストメソッドが「GET」の場合に実行されます。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Route {
	String path();
	Method method() default Method.ALL;
}
