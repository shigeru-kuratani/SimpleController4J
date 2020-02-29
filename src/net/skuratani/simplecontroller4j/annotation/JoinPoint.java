package net.skuratani.simplecontroller4j.annotation;

/**
 * <p>アスペクト ジョインポイント列挙型</p>
 * <pre>
 * アスペクトのジョインポイントを表現する列挙型です。
 * 以下のジョインポイントをサポートしています。
 *    1. BEFORE：メソッド実行前
 *    2. AFTER：メソッド実行後
 *    3. AROUND：メソッド実行前後
 *    4. AFTER_RETURNING：メソッド正常終了後
 *    5. AFTER_THROWING：メソッド異常終了後（例外発生後）
 * </pre>
 *
 * @author  Shigeru Kuratani
 * @version 0.0.3
 */
public enum JoinPoint {
	BEFORE, AFTER, AROUND, AFTER_RETURNING, AFTER_THROWING
}
