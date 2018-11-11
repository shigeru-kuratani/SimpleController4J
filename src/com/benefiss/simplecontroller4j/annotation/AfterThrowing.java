package com.benefiss.simplecontroller4j.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * <p>ジョインポント(AfterThrowing) アノテーション</p>
 * <pre>
 * このアノテーションを記述するとexcution値がディスパッチメソッド名にマッチする場合は、
 * ディスパッチメソッドが例外をスローした後に、このアノテーションを記述したメソッドが実行される。
 * (ex.)[at]AfterThrowing(execution = "com.somecompany.service.UserService.init")
 *        → ディスパッチメソッドの完全修飾名が「com.somecompany.service.UserService.init」の場合に実行されます。
 * (ex.)[at]AfterThrowing(execution = "*.UserService.init")
 *        → ディスパッチメソッドが「クラス名：UserService」「メソッド名：init」の場合に全てのパッケージで実行されます。
 * (ex.)[at]AfterThrowing(execution = "*.init")
 *        → ディスパッチメソッドが「メソッド名：init」の場合に全てのクラスで実行されます。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AfterThrowing {
	String[] execution();
}
