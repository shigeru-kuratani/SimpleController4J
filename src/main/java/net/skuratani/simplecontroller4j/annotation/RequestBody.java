package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>リクエストボディー アノテーション</p>
 * <pre>
 * リクエストボディーの値をメソッド引数にマッピングするアノテーションです。
 * 以下の情報をアノテーションに記述して、マッピングを設定します。
 *    (ex.)リクエストボディー：XXXXX_YYYYY_ZZZZZ
 *         メソッド：pretected void someMethod([at]RequestBody String reqBody)
 *       　　　      → リクエストボディーが「reqBody」にバインドされます。
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RequestBody {    
}
