package net.skuratani.simplecontroller4j.routing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.skuratani.simplecontroller4j.annotation.Controller;
import net.skuratani.simplecontroller4j.annotation.Route;
import net.skuratani.simplecontroller4j.requestmapping.RequestMapping;

/**
 * <p>ルーティングクラス・メソッド探索</p>
 * <pre>
 * ディスパッチャサーブレットからの依頼を受けて、リクエストURLから以下の情報を探索・取得する。
 *     1. 実行クラス
 *     2. 実行メソッド
 *     3. リクエストパス情報 (例)/edit/init/35
 * リクエストURLに対応する実行クラスは「WEB-INF/classes」配下の全クラスをロードして、探索を実行する。
 * </pre>
 */
public class Router {

	/** ルーティングクラス */
	private Class<?> routingClass;
	/** ルーティングメソッド */
	private Method routingMethod;
	/** 実行メソッドパス */
	private String executeMethodPath;

	/**
	 * <p>ディスパッチクラスを探索</p>
	 *
	 * @param  requestPath   リクエストパス
	 * @param  requestMethod リクエストメソッド
	 * @param  classList     クラスリスト
	 * @return routingMap    ルーティング情報マップ（クラス・メソッド）
	 */
	public RequestMapping findRoutingClass(String requestPath,
										   net.skuratani.simplecontroller4j.annotation.Method requestMethod,
										   List<Class<?>> classList) {

        for (Class<?> clazz : classList) {

            // インターフェイスの場合はスキップ
            if (clazz.isInterface()) continue;

            // Controllerアノテーションチェック
            if (!hasControllerAnnotation(clazz)) continue;

            // Routeアノテーションのマッチング
            if (!matchRoutingPath(clazz, requestPath, requestMethod)) continue;

            // リターン
            return new RequestMapping() {
            	{ setRequestClass(routingClass);
            	  setRequestMethod(routingMethod);
            	  setExecuteMethodPath(executeMethodPath); }
            };
        }

        // ルーティングクラス・メソッドが存在しない場合
        return null;
	}

    /**
     * <p>Controllerアノテーション確認</p>
     *
     * @param  clazz クラス
     * @return boolean
     *         true  : 存在する
     *         false : 存在しない
     */
	protected boolean hasControllerAnnotation(Class<?> clazz) {
		return Arrays.asList(clazz.getDeclaredAnnotations()).stream()
					.anyMatch(a -> a.annotationType().equals(Controller.class));
    }

    /**
     * <p>ルーティングパスとのマッチング確認</p>
     *
     * @param  clazz         クラス
     * @param  requestPath   リクエストパス
     * @param  requestMethod リクエストメソッド
     * @return boolean
     *         true  : マッチする
     *         false : マッチしない
     */
	protected boolean matchRoutingPath(Class<?> clazz, String requestPath,
    								 net.skuratani.simplecontroller4j.annotation.Method requestMethod) {

    	boolean matchRoutingFlag = false;
    	String classMappingPath = "";
    	for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            if (annotation.annotationType().equals(Route.class)) {
            	classMappingPath = ((Route) annotation).path();
            }
        }
    	for (Method method : clazz.getDeclaredMethods()) {
    		String mappingPath = "";
	        for (Annotation annotation : method.getDeclaredAnnotations()) {
		        	if (annotation.annotationType().equals(Route.class)) {
		        		if (((Route) annotation).path().matches(".+\\{.+")) {
		        			mappingPath = classMappingPath + ((Route) annotation).path().substring(0, ((Route) annotation).path().indexOf("{") - 1);
		        		} else {
		        			mappingPath = classMappingPath + ((Route) annotation).path();
		        		}
			            if (requestPath.matches(mappingPath + ".*")) {
			            	if (net.skuratani.simplecontroller4j.annotation.Method.ALL == ((Route) annotation).method()) {
			            		routingClass = clazz;
			            		routingMethod = method;
			            		executeMethodPath = classMappingPath + ((Route) annotation).path();
			            		matchRoutingFlag = true;
			            	} else if (requestMethod == ((Route) annotation).method()) {
			            		routingClass = clazz;
			            		routingMethod = method;
			            		executeMethodPath = classMappingPath + ((Route) annotation).path();
			            		matchRoutingFlag = true;
			            	}
			            }
		        	}
	        }
    	}
        return matchRoutingFlag;
    }

}
