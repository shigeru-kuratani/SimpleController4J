# SimpleController4Jについて

## 開発の動機

SimpleController4Jを開発した動機ですが、「サーブレットがもう少しだけ面倒を見てくたらいいのにな」 というのが理由です。
私は仕事で開発業務に携わっておりますが、各開発案件でいくつかのJavaで構築されたWEBアプリケーションを見て来ました。 対象となるWEBアプリケーションは、StrutsやSpring Frameworkなどのフレームワークを使用したものだけでなく、 サーブレット・JSPを使用したものもいくつかありました。  
WEBサービスを担当していれば、長く同じアプリケーション（プロダクト）を扱いますので、そのアプリケーションで使用している フレームワークを使用して、素早くプロトタイプなどを開発出来ると思います。 しかし、私のように開発案件ごとに使用されているフレームワークが異なっていると、そのフレームワークの設定などで 多くの時間をとられてしまいます。JavaのWEBコンポーネント技術の基礎として、サーブレット・JSPの知識を持っていれば、 これを使用して小さなものを素早く開発することが出来ます。  
そこで、サーブレットに私が「少し足りない」と感じる部分を拡張（強化）し、サーブレット・JSPを使用した スモールスタートの開発支援する為に開発したのが、SimpleController4Jです。

SimpleController4Jはプレゼンテーション層のフレームワークです。ドメイン層（ビジネスロジック層）・パーシステンス層 （データアクセス層）に関しては、皆さんのアプリケーションに適したフレームワークを組み合わせてください。 ※ DI・AOP・ORMなどに関して任意のフレームワークを使用して頂くと良いと思います。

## ルーティング

サーブレットを使用してコントローラを作成する場合は、サーブレットパス単位でマッピングを行います。 そして、HTTPメソッド毎にサーブレットコンテナからdoGetなどのメソッドが呼び出されます。

```
/**
 * ユーザ一覧サーブレット
 */
@WebServlet("/userList")
public class UserList extends HttpServlet {

	/**
	 * 初期表示
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
						 throws ServletException, IOException {
		// 初期表示処理を実行します
	}

	/**
	 * ユーザ検索メソッド
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
						  throws ServletException, IOException {
		// ユーザ検索処理などを実行します
	}
}
```

例えば、「ユーザ登録処理」を考えてみます。  
「ユーザ登録処理」には「初期表示」「確認画面」「完了画面」、そして「確認画面から戻る画面」が通常は存在します。  
「/userRegist」というサーブレットパスによりコントローラクラスが固定された場合、以下のように「画面モード」のような 変数を設定して処理を実装することになります。

```
/**
 * ユーザ登録サーブレット
 */
@WebServlet("/userRgist")
public class UserRgist extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * 初期表示
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
						 throws ServletException, IOException {
		// 初期表示処理を実行します
	}

	/**
	 * 確認画面・完了画面・確認画面から戻る画面
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
						  throws ServletException, IOException {
		// 処理モード
		String execMode = null;
		// 画面モード取得
		String dispMode = request.getParameter("dispMode");
		// 処理モード設定
		if ("init".equals(dispMode)) {
			doGet(request, response);
		} else if ("confirm".equals(dispMode)) {
			execMode = "confirm";
		} else if ("complete".equals(dispMode)) {
			execMode = "complete";
		} else if ("confirmBack".equals(dispMode)) {
			execMode = "confirmBack";
		}
		// 処理モードに従った処理
	}
}
```

上記のようにサーブレットパスのより実行クラス（コントローラ）が固定されてしまうとPOSTメソッドで実行される メソッドに過剰な分岐処理を実装することになります。本来はそれぞれの画面表示毎に処理（メソッド）が分かれて いるのが理想です。そうすれば、コントローラに見通しの悪くなる分岐処理を記述する必要がありません。  
SimpleController4Jでは、起点となるパス（例：userRegist）を設定して、コントローラの各メソッドに 異なるリクエストパスをマッピングすることで、見通しの良い実装をすることが可能です。

```
/**
 * ユーザ登録コントーラ
 */
@Controller
@Route(path = "/userRegist")
public class UserRegistController {

	/**
	 * 初期表示
	 */
	@Route(path = "/init", method = Method.GET)
	public void init(HttpServletRequest request, HttpServletResponse response)
					 throws ServletException, IOException {
		// 初期表示処理を実装します
	}

	/**
	 * 確認画面
	 */
	@Route(path = "/confirm", method = Method.POST)
	public void confirm(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		// 確認画面処理を実装します
	}

	/**
	 * 完了画面
	 */
	@Route(path = "/complete", method = Method.POST)
	public void complete(HttpServletRequest request, HttpServletResponse response)
						 throws ServletException, IOException {
		// 完了画面処理を実装します
	}

	/**
	 * 確認画面から戻る
	 */
	@Route(path = "/confirmBack", method = Method.POST)
	public void confirmBack(HttpServletRequest request, HttpServletResponse response)
							throws ServletException, IOException {
		// 確認画面から戻る処理を実装します
	}
}
```

## データバインディング

例えば、「ユーザ情報更新処理」を考えてみます。  
サーブレットでは以下のような処理になると思います。

```
/**
 * Userクラス
 */
public class User {
	/** ユーザID */
	public int userId;
	/** ユーザ名 */
	public String userName;
	/** パスワード */
	public String password;
}
```

```
/**
 * ユーザ編集画面
 */
@WebServlet("/userUpdate")
public class UserUpdate extends HttpServlet {

	/**
	 * 初期表示
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		// 初期表示処理を実行します
	}

	/**
	 * ユーザ更新処理
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		//-----------------------------------------//
		// POSTパラメータ値を取得
		//-----------------------------------------//
		// ユーザID
		int userId = Integer.parseInt(request.getParameter("userId"));
		// ユーザ名
		String userName = request.getParameter("userName");
		// パスワード
		String password = request.getParameter("password");

		// 更新処理を実行
	}
}
```

サーブレットでは、クエリストリング（GETパラメータ）・フォームパラメータ（POSTパラメータ）共に 実行メソッドの引数であるHttpServletRequestオブジェクトから、getParameterメソッドを使用して 値を取得する処理が必要になります。  
いつも上記処理を記述するのは少し冗長に感じられます。  
そこで、SimpleController4Jでは、DispatcherServletからディスパッチされた実行メソッドの 引数に、自動でクエリストリング（GETパラメータ）・フォームパラメータ（POSTパラメータ）をバインドします。  
パスパラメータ（例：http://[ホスト名]/[コンテキスト名]/userUpdate/update/135/Mike）も自動でバインドをします。（後述）
以下のように記述すれば、自動で「引数：user」にパラメータ値が設定されます。

```
/**
 * ユーザ編集画面
 */
@Controller
@Route(path = "/userUpdate")
public class UserUpdateController {

	/**
	 * 初期表示
	 */
	@Route(path = "/init", method = Method.GET)
	public void init(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		// JSPへフォワード
		request.getRequestDispatcher("/WEB-INF/view/userUpdate.jsp").forward(request, response);
	}

	/**
	 * ユーザ更新処理
	 */
	@Route(path = "/update", method = Method.POST)
	public void update(User user, HttpServletRequest request, HttpServletResponse response) {
		// 更新処理を実行
		// 引数：userにパラメータ値が設定されています
		try {
			new UserService().update(user);
		} catch (SQLException e) {
			// 例外処理を実行します
		}
	}
}
```

```
■ クエリストリング例
http://[ホスト名]/[コンテキスト名]/userUpdate/update?userId=135&userName=Mike&password=Pj89HrF
```

```
■ フォームパラメータ例（リクエストボディー）
userId=135&userName=Mike&password=Pj89HrF
```

## アスペクト

サーブレットではフィルター（jakarta.servlet.Filter）を使用して、サーブレット実行前後に 処理を挿入することが出来ます。  
ログ出力をするフィルターは以下のようになるかと思います。

```
/**
 * ログフィルター
 */
@WebFilter(urlPatterns = { "/*" })
public class LogFilter implements Filter {

	// ロガー
	Logger logger;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {

		// ロガーを取得してログレベルをINFOに設定
		logger = Logger.getLogger(this.getClass().getName());
		logger.setLevel(Level.INFO);

        	// ハンドラーを作成してロガーに登録
        	Handler handler = null;
		try {
			handler = new FileHandler("/var/log/samplelog/sample.log");
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
        	logger.addHandler(handler);

        	// フォーマッターを作成してハンドラーに登録
        	Formatter formatter =  new SimpleFormatter();
        	handler.setFormatter(formatter);
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// フィルター前にログ出力
		logger.log(Level.INFO, "ログメッセージ（サーブレット実行前）");

		chain.doFilter(request, response);

		// フィルター後にログ出力
		logger.log(Level.INFO, "ログメッセージ（サーブレット実行後）");
	}
}
```

サーブレットがURLパターンによりマッピングされるのに呼応して、フィルターもURLパターンにより 実行クラスが決定されます。  
SimpleController4Jでは、サーブレットパスでなく、リクエストパスによりディスパッチ先クラス・メソッドが 決定されます。そこで、フィルターに該当する「処理挿入機能」をアスペクト(Aspect)として、ディスパッチ先 メソッドを指定して記述する仕様としております。  
URLパターンではなく、ディスパッチ先メソッドを指定することで、より細かな制御を実現しています。 また、処理挿入のポイントである「ジョインポイント(JoinPoint)」もフィルターより細かな以下の挿入点があります。

- メソッド実行前(Before)
- メソッド実行後(After)
- メソッド実行前後(Around)
- メソッドリターン後(AfterReturning)
- 例外スロー後(AfterThrowing)

例えば、全てのコントローラの「初期表示」処理で実行すべき処理がある場合に、メソッド実行前に挿入処理である アドバイス(Advice)を実行するには以下のようになります。

```
/**
 * 初期処理アスペクト
 */
@Aspect
public class InitialAspect {

	@Before(execution = "*.init")
	public void initExec(ServletRequest request, ServletResponse response) {
		// 全てのコントローラクラスの「init」メソッドの実行前に処理されます
		// 初期処理などを記述することが出来ます
	}
}
```

## RESTful API

SimpleController4Jでは、Routeアノテーションを使用して、リクエストパスとHTTPメソッドの組み合わせで 実行クラス・メソッドを記述することが出来ます。  
そして、コントローラメソッドから文字列を返却すると、その返却文字列がクライアントへ送信されます。  
上記特性を活かして、サーブレット感覚でRESTful APIの構築を支援します。 例えば、URLで「ユーザID」をAPIで受信して、ユーザ情報をJSONで返却するには以下のように実装することが可能です。  
※下記はjackson([https://github.com/FasterXML/jackson](https://github.com/FasterXML/jackson))を使用してJSON文字列をレスポンスするサンプルです。

```
/**
 * RESTful APIサンプル
 */
@Controller
public class UserController {

	@Route(path = "/user/<id:int>", method = Method.GET)
	public String getUser(int id) {

		User user;
		try {
			user = new UserService().findById(id);
		} catch (SQLException e) {
			// 例外処理を実行します
		}

		ObjectMapper mapper = new ObjectMapper();
        	String json = mapper.writeValueAsString(user);

		return json;
	}
}
```

```
■ 呼び出し結果
CW-02:~ XXX$ curl localhost:8080/SimpleControllerSample/user/1
{"id":1,"age":28,"name":"Falco"}
```

## ライセンス

本プログラムは、GPLv2 with the Classpath Exceptionとして配布を致します。  
[GNU GPL v2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
本プログラムの利用者は、独立したモジュールと本ライブラリをリンクして実行可能プログラムを生成し、利用者が選んだ条件の元で結果の実行可能プログラムを複製および配布することができます。 独立したモジュールとは、本ライブラリの派生物でもなく、本ライブラリを基にしてもいないモジュールです。

また、著作権者、または上記で 許可されている通りに『プログラム』を改変または再頒布したその他の団体は、 あなたに対して『プログラム』の利用ないし利用不能で生じた通常損害や特別 損害、偶発損害、 間接損害(データの消失や不正確な処理、あなたか第三者が 被った損失、あるいは『プログラム』が他のソフトウェアと一緒に動作しない という不具合などを含むがそれらに限らない)に一切の責任を負いません。  
本プログラムは利用者の責任において使用されますようにお願い致します。
