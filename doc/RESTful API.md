# RESTful API

## RESTful APIについて
SimpleController4Jでは、Routeアノテーションを使用して、リクエストパスとHTTPメソッドの組み合わせで 実行クラス・メソッドを記述することが出来ます。  
そして、コントローラメソッドから文字列を返却すると、その返却文字列がクライアントへ送信されます。  
上記特性を活かして、サーブレット感覚でRESTful APIの構築を支援します。 例えば、URLで「ユーザID」をAPIで受信して、ユーザ情報をJSONで返却するには以下のように実装することが可能です。  
※下記は[jackson](https://github.com/FasterXML/jackson)を使用してJSON文字列をレスポンスするサンプルです。

```
/**
 * RESTful APIサンプル
 */
@Controller
public class UserController {

	@Route(path = "/user/<id:int>", method = Method.GET)
	public String getUser(int id) throws JsonProcessingException {

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

## サンプル
SimpleController4Jで、システムユーザに関するRESTful APIを構築すると以下のような 実装が可能だと思います。参考にして頂ければ幸いです。  
※下記は[jackson](https://github.com/FasterXML/jackson)を使用してJSON文字列をレスポンスするサンプルです。

```
/**
 * RESTful APIサンプル
 */
@Controller
public class UserController {

	/**
	 * ユーザ情報取得
	 *
	 * @param  id ユーザID
	 * @return ユーザ情報(JSON)
	 * @throws  JsonProcessingException
	 */
	@Route(path = "/user/{id:int}", method = Method.GET)
	public String getUser(@PathVariable("id") int id) throws JsonProcessingException {

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

	/**
	 * ユーザ登録
	 *
	 * @param  user ユーザ情報
	 * @return　登録結果(JSON)
	 * @throws  JsonProcessingException
	 */
	@Route(path = "/user", method = Method.POST)
	public String registUser(@RequestBody User user) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			new UserService().regist(user);
		} catch (SQLException e) {
			// 例外処理を実行します
			json = mapper.writeValueAsString(new HashMap() { { put("result", "false"); } });
			return json;
		}

		json = mapper.writeValueAsString(new HashMap() { { put("result", "true"); } });
		return json;
	}

	/**
	 * ユーザ更新
	 *
	 * @param  user ユーザ情報
	 * @return　更新結果(JSON)
	 * @throws  JsonProcessingException
	 */
	@Route(path = "/user", method = Method.PUT)
	public String updateUser(@RequestBody User user) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			new UserService().update(user);
		} catch (SQLException e) {
			// 例外処理を実行します
			json = mapper.writeValueAsString(new HashMap() { { put("result", "false"); } });
			return json;
		}

		json = mapper.writeValueAsString(new HashMap() { { put("result", "true"); } });
		return json;
	}

	/**
	 * ユーザ削除
	 *
	 * @param  id ユーザID
	 * @return　削除結果(JSON)
	 * @throws  JsonProcessingException
	 */
	@Route(path = "/user/{id:int}", method = Method.DELETE)
	public String deleteUser(@PathVariable("id") int id) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			new UserService().delete(id);
		} catch (SQLException e) {
			// 例外処理を実行します
			json = mapper.writeValueAsString(new HashMap() { { put("result", "false"); } });
			return json;
		}

		json = mapper.writeValueAsString(new HashMap() { { put("result", "true"); } });
		return json;
	}
}
```

