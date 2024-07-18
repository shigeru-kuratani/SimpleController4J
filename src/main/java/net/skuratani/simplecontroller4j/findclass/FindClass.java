package net.skuratani.simplecontroller4j.findclass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>クラス探索</p>
 * <pre>
 * ルーター・アスペクトからの依頼を受けて、指定ディレクトリ配下の全クラスを取得する。
 * </pre>
 */
public class FindClass {

    /**
     * <p>指定ディレクトリ配下の全クラスをリスト取得</p>
     *
     * @param  dir     探索ディレクトリ
     * @return classes クラスリスト
     * @throws ClassNotFoundException 探索したクラスが存在しない場合
     * @throws IOException 
     */
	public static List<Class<?>> findClasses(Path dir)
        throws IOException, ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();
        for (Path entry : Files.list(dir).toList()) {
            if (Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(".class")) {
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(
                        entry.toAbsolutePath().toString()
                            .substring(entry.toAbsolutePath().toString().indexOf("classes") + 8)
                        	.replace("\\", ".")
                            .replace("/", ".")
                            .replace(".class", ""))
                );
            } else if (Files.isDirectory(entry)) {
                classes.addAll(findClasses(dir.resolve(entry)));
            }
        }

        return classes;
    }

}
