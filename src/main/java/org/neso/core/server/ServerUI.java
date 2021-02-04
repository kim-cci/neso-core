package org.neso.core.server;

/**
 * 초기에 Server를 Connectless, ConnectOriented로 분리했을 경우 메서드 체이닝을 위해 생성
 * 현재는 불필요하지만 차후에 Server를 다시 분리할 수도 있어서 살려둠
 */
public interface ServerUI {
	
	public void start();
}
