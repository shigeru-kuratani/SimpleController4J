package com.benefiss.simplecontroller4j.aspectmapping;

import java.lang.reflect.Method;

import com.benefiss.simplecontroller4j.annotation.JoinPoint;

/**
 * <p>アスペクトマッピング情報クラス</p>
 * <pre>
 * 以下の情報を格納するリクエスト情報クラス。
 *     1. 実行クラス
 *     2. 実行メソッド
 *     3. ジョインポイント(BEFORE・AFTER・AROUND・AFTER_RETURNING・AFTER_THROWING)
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.1
 */
public class AspectMapping {

	/** リクエストクラス */
	private Class<?> aspectClass;

	/** リエクストメソッド */
	private Method aspectMethod;

	/** ジョインポイント */
	private JoinPoint joinPoint;

	public Class<?> getAspectClass() {
		return aspectClass;
	}

	public void setAspectClass(Class<?> aspectClass) {
		this.aspectClass = aspectClass;
	}

	public Method getAspectMethod() {
		return aspectMethod;
	}

	public void setAspectMethod(Method aspectMethod) {
		this.aspectMethod = aspectMethod;
	}

	public JoinPoint getJoinPoint() {
		return joinPoint;
	}

	public void setJoinPoint(JoinPoint joinPoint) {
		this.joinPoint = joinPoint;
	}

}
