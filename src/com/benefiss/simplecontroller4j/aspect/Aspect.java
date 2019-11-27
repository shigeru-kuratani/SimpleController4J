package com.benefiss.simplecontroller4j.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.benefiss.simplecontroller4j.annotation.After;
import com.benefiss.simplecontroller4j.annotation.AfterReturning;
import com.benefiss.simplecontroller4j.annotation.AfterThrowing;
import com.benefiss.simplecontroller4j.annotation.Around;
import com.benefiss.simplecontroller4j.annotation.Before;
import com.benefiss.simplecontroller4j.annotation.JoinPoint;
import com.benefiss.simplecontroller4j.aspectmapping.AspectMapping;
import com.benefiss.simplecontroller4j.requestmapping.RequestMapping;

/**
 * <p>アスペクトクラス・メソッド探索</p>
 * <pre>
 * ディスパッチャサーブレットからの依頼を受けて、アスペクトアノテーションから以下の情報を探索・取得する。
 *     1. 実行クラス
 *     2. 実行メソッド
 * アスペクト実行クラスは「WEB-INF/classes」配下の全クラスをロードして、探索を実行する。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.2
 */
public class Aspect {

	/** アスペクトマッピングリスト */
	private List<AspectMapping> aspectMappingList;

	/**
	 * <p>コンストラクタ</p>
	 */
	public Aspect() {
		aspectMappingList = new ArrayList<AspectMapping>();
	}

	/**
	 * <p>アスペクトクラスを探索</p>
	 *
	 * @param  requestMapping    ルーティング情報マップ（クラス・メソッド）
	 * @param  classList         コントローラclassインスタンスリスト
	 * @return aspectMappingList アスペクトマッピングリスト
	 */
	public List<AspectMapping> findAspectClass(RequestMapping requestMapping, List<Class<?>> classList) {

        for (Class<?> clazz : classList) {

            // インターフェイスの場合はスキップ
            if (clazz.isInterface()) continue;

            // Aspectアノテーションチェック
            if (!hasAspectAnnotation(clazz)) continue;

            // ジョインポイントとマッチするクラス・メソッド探索
            findJoinPoint(clazz, requestMapping);
        }

        return aspectMappingList;
	}

    /**
     * <p>Aspectアノテーション確認</p>
     *
     * @param  clazz クラス
     * @return boolean
     *         true  : 存在する
     *         false : 存在しない
     */
	protected boolean hasAspectAnnotation(Class<?> clazz) {

    	boolean aspectFlag = false;
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().equals(com.benefiss.simplecontroller4j.annotation.Aspect.class)) {
            	aspectFlag = true;
            }
        }
        return aspectFlag;
    }

    /**
     * <p>ジョインポイントとマッチするクラス・メソッド探索</p>
     *
     * @param  clazz          クラス
     * @param  requestMapping ルーティング情報マップ（クラス・メソッド）
     */
	protected void findJoinPoint(Class<?> clazz, RequestMapping requestMapping) {

    	for (Method method : clazz.getDeclaredMethods()) {
	        for (Annotation annotation : method.getDeclaredAnnotations()) {
	        	// Beforeアノテーション
	        	if (annotation.annotationType().equals(Before.class)) {
	        		for (String exec : ((Before) annotation).execution()) {
		        		if ((requestMapping.getRequestClass().getName() + "." + requestMapping.getRequestMethod().getName())
		        			.matches(exec.replace(".", "\\.").replace("*", ".*"))) {
		        			aspectMappingList.add(new AspectMapping() {
					            	{ setAspectClass(clazz);
					            	  setAspectMethod(method);
					            	  setJoinPoint(JoinPoint.BEFORE); }
					            });
		            	}
	        		}
	            }
	        	// Afterアノテーション
	        	if (annotation.annotationType().equals(After.class)) {
	        		for (String exec : ((After) annotation).execution()) {
		        		if ((requestMapping.getRequestClass().getName() + "." + requestMapping.getRequestMethod().getName())
		        			.matches(exec.replace(".", "\\.").replace("*", ".*"))) {
		        			aspectMappingList.add(new AspectMapping() {
					            	{ setAspectClass(clazz);
					            	  setAspectMethod(method);
					            	  setJoinPoint(JoinPoint.AFTER); }
					            });
		            	}
	        		}
	            }
	        	// Aroundアノテーション
	        	if (annotation.annotationType().equals(Around.class)) {
	        		for (String exec : ((Around) annotation).execution()) {
		        		if ((requestMapping.getRequestClass().getName() + "." + requestMapping.getRequestMethod().getName())
		        			.matches(exec.replace(".", "\\.").replace("*", ".*"))) {
		        			aspectMappingList.add(new AspectMapping() {
					            	{ setAspectClass(clazz);
					            	  setAspectMethod(method);
					            	  setJoinPoint(JoinPoint.AROUND); }
					            });
		            	}
	        		}
	            }
	        	// AfterReturningアノテーション
	        	if (annotation.annotationType().equals(AfterReturning.class)) {
	        		for (String exec : ((AfterReturning) annotation).execution()) {
		        		if ((requestMapping.getRequestClass().getName() + "." + requestMapping.getRequestMethod().getName())
		        			.matches(exec.replace(".", "\\.").replace("*", ".*"))) {
		        			aspectMappingList.add(new AspectMapping() {
					            	{ setAspectClass(clazz);
					            	  setAspectMethod(method);
					            	  setJoinPoint(JoinPoint.AFTER_RETURNING); }
					            });
		            	}
	        		}
	            }
	        	// AfterThrowingアノテーション
	        	if (annotation.annotationType().equals(AfterThrowing.class)) {
	        		for (String exec : ((AfterThrowing) annotation).execution()) {
		        		if ((requestMapping.getRequestClass().getName() + "." + requestMapping.getRequestMethod().getName())
		        			.matches(exec.replace(".", "\\.").replace("*", ".*"))) {
		        			aspectMappingList.add(new AspectMapping() {
					            	{ setAspectClass(clazz);
					            	  setAspectMethod(method);
					            	  setJoinPoint(JoinPoint.AFTER_THROWING); }
					            });
		            	}
	        		}
	            }
        	}
        }
    }

}
