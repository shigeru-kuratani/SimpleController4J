package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>リクエストパラメータ アノテーション</p>
 * <pre>
 * リクエストパラメータの値をメソッド引数にマッピングするアノテーションです。
 * 以下の情報をアノテーションに記述して、マッピングを設定します。
 *    1. クエリストリング
 *    (ex.)リクエストパス：/[コンテキスト]/[ルートマッピング]?id=XXX
 *         メソッド：pretected void someMethod([at]RequestParam("id") int id)
 *       　　　      → リクエストパラメータが「id」にバインドされます。
 *    2. フォームパラメータ
 *    (ex.)リクエストボディー：id=XXX&name=YYY
 *         メソッド：pretected void someMethod([at]RequestParam("id") int id
 *                                             [at]RequestParam("name") String name)
 *       　　　      → リクエストパラメータが「id」「name」にバインドされます。
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RequestParam {
    String value() default "";    
}
