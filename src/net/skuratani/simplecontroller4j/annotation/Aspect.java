package net.skuratani.simplecontroller4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>アスペクトアノテーション</p>
 * <pre>
 * このアノテーションが記述されたクラスはアスペクトとして認識されます。
 * アスペクトを識別する為のマーカーアノテーションです。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aspect {}
