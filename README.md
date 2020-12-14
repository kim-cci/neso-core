# neso-core
소켓 서버(-흔히 전문 통신) 개발 시 사용할 수 있는 고수준 프레임워크입니다.
손 쉽게 바이트-길이 기반으로 통신하는 서버를 만들 수 있습니다. 
<br>
<br>
## 특징
- 내부 구현은 netty를 사용하였습니다.
- 웹 개발자에게 생소하지 않은 추상화 레벨을 제공합니다.
- 대량 접속 및 대량 처리를 위한 인터페이스를 제공합니다.
<br>
<br>

## 환경
- 자바 1.6 이상
<br>
<br>

## quick start & example
- Api 구현(서비스 레이어)
````java
//회원번호 조회 API
public class SearchApi implements Api {
	
	private Service service;
	
	@Override
	public byte[] handle(HeadBodyRequest request) throws Exception {
		//서비스 레이어
		//service.getUser(....
		return userNo.getBytes();
	}
}
````

````java
//회원 가입 API
public class SignUpApi implements Api {
	
	private Service service;
	
	@Override
	public byte[] handle(HeadBodyRequest request) throws Exception {
		
		byte[] bodyBytes = request.getBodyBytes();
		//서비스 레이어 
		//service.join(...
		return "ok".getBytes(); //성공 응답
	}
}
````

  ServerHandler구현체와 Server로 서버 시작
  10002번 포트로 서버 기동
````java
public static void main(String[] args) {
		
	ServerHandler serverHandler = new HeadBasedServerHandler(8, 0, 2, 2, 6); //헤드 8바이트, 본문길이 필드 0 ~ 2, API식별자 필드 2 ~ 8
	serverHandler.registApi("search", new SignUpApi()); //회원번호 조회 API
	serverHandler.registApi("sign", new SearchApi()); //회원 가입 API
		
	new Server(serverHandler, 10002) 
		.maxConnections(5000)           //최대 접속자 수
		.readTimeoutMillisOnRead(1000)  //timeout 설정
		.start();
}
````
<br>
<br>
RequestHandler(ServerHandler 상위 인터페이스) : 범용적

````java
public static void main(String[] args) {
		
	new Server(new RequestHandlerAdapter() {
			
		@Override
		public int headLength() {
			return 20;
		}
		
		@Override
		public int bodyLength(HeadRequest request) {
			return Utils.byteToInt(request.getHeadBytes());
		}
		
		@Override
		public void doRequest(Client client, HeadBodyRequest request) throws Exception {
			//사용자 요청 처리
		}
			
		public void onConnect(Client client) {
			//사용자 접속 처리
		};

	}	, 10002)
	.maxConnections(5000)
	.connectionOriented(true)
	.requestExecutorType(IoThreadExecutor.class)	//io스레드에서 요청처리
	.readTimeoutMillisOnRead(1000)
	.start();
}
````
<br>
<br>

## architecture 
![object](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdPdGuC%2FbtqBUTRLxtY%2FtKwb81Qp0zZ8I4LumoZjw1%2Fimg.jpg)
<br>
<br>

## License
[MIT](https://choosealicense.com/licenses/mit/)
<br>
<br>

## Links
 * [개인 블로그](https://jronin.tistory.com/93)
<br>
<br>
## repositoryㅡ
