package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Route {
	String path() default "";
	Method method() default Method.ALL;
}
