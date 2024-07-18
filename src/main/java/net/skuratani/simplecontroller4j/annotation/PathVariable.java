package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>パスパラメータ アノテーション</p>
 * <pre>
 * リクエストパスの値をメソッド引数にマッピングするアノテーションです。
 * 以下の情報をアノテーションに記述して、マッピングを設定します。
 *    (ex.)リクエストパス：/[コンテキスト]/[ルートマッピング]/{userId}
 *         メソッド：pretected void someMethod([at]PathVariable("userId") int userId)
 *       　　　      → パスパラメータが「userId」にバインドされます。
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface PathVariable {
    String value() default "";    
}
