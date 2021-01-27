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
회원 가입과 조회 API를 가진 전문 통신 구현 샘플입니다.

1. Api 인터페이스 구현(서비스 레이어)
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
	
	....
}
````

2. ServerHandler 구현체와 Server를 이용하여 API서버 생성 및 구동 
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

위처럼 간단하게 전문 통신 서버를 구현할 수 있도록 고수준 API도 제공하지만
경우에 따라 커스트마이징 할 수 있도록 다양한 레벨의 API를 제공합니다.

* [조금 더 상세한 시작 가이드](https://jronin.tistory.com/111)

<br>
<br>

## maven repository
<dependency> 
	<groupId>org.osdkim.neso</groupId> 
	<artifactId>neso-core</artifactId> 
	<version>0.9.3</version> 
</dependency>

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
