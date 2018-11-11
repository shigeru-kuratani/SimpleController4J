package com.benefiss.simplecontroller4j.findclass;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>クラス探索</p>
 * <pre>
 * ルーター・アスペクトからの依頼を受けて、指定ディレクトリ配下の全クラスを取得する。
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.1
 */
public class FindClass {

    /**
     * <p>指定ディレクトリ配下の全クラスをリスト取得</p>
     *
     * @param  dir     探索ディレクトリ
     * @return classes クラスリスト
     * @throws ClassNotFoundException 探索したクラスが存在しない場合
     */
	public static List<Class<?>> findClasses(File dir) throws ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();
        for (String path : dir.list()) {
            File entry = new File(dir, path);
            if (entry.isFile() && entry.getName().endsWith(".class")) {
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(
                        entry.getAbsolutePath().substring(entry.getAbsolutePath().indexOf("classes") + 8)
                        					   .replace("\\", ".").replace("/", ".").replace(".class", ""))
                );
            } else if (entry.isDirectory()) {
                classes.addAll(findClasses(new File(dir, path)));
            }
        }

        return classes;
    }

}
