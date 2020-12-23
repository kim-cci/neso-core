package org.neso.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GenericFutureListener;

import java.lang.reflect.Constructor;

import org.neso.core.netty.ByteLengthBasedInboundHandler;
import org.neso.core.netty.ClientAgent;
import org.neso.core.request.factory.InMemoryRequestFactory;
import org.neso.core.request.handler.RequestHandler;
import org.neso.core.request.handler.task.RequestExecutor;
import org.neso.core.support.ConnectionManagerHandler;
import org.neso.core.support.ConnectionRejectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends ServerOptions {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
    final private ServerBootstrap sbs = new ServerBootstrap();

    final RequestHandler requestHandler;
    
    final int port;
    
    
    private ServerContext context;	//서버 기동시 초기화
    
    private int soBackLog = -1;
    
    
    public Server(RequestHandler requestHandler, int port) {
    	this.requestHandler = requestHandler;
    	this.port = port;
	}
 
    
    @Override
    public <T> ServerOptions channelOption(ChannelOption<T> option, T value) {
    	if (option == ChannelOption.SO_BACKLOG && value != null) {
    		soBackLog = (Integer) value;
    	}
    	sbs.option(option, value);
    	return this;
    }
    
    @Override
    public <T> ServerOptions childChannelOption(ChannelOption<T> childOption, T value) {
    	sbs.childOption(childOption, value);
    	return this;
    }
    
    @Override
    public void start() {
    	
    	configurationContext();
    	
    	int bossThreads = Runtime.getRuntime().availableProcessors();
    	EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads, new DefaultThreadFactory("bossThread"));
    	

		int ioThreads = 0; //0은 네티 전략 따름 -> core * 2;
    	if (context.requestExecutor().isRunOnIoThread()) {
			ioThreads = context.options().getMaxRequests() + 1; //io스레드가 request처리 동시실행숫자보다 작으면 io스레드에서 병목이 발생하므로.. io스레드가 무조건 커야한다.
		}
    	
    	EventLoopGroup workerGroup = new NioEventLoopGroup(ioThreads, new DefaultThreadFactory("ioThread"));//connectionManager.getMaxConnectionSize() + 1
    	
        try {
        	
        	if (soBackLog == -1) {
        		soBackLog = bossThreads * 200;
        	}
        	sbs.option(ChannelOption.SO_BACKLOG, soBackLog);
        	
            sbs.group(bossGroup, workerGroup);
            sbs.channel(NioServerSocketChannel.class);
            
//        	if (getPipeLineLogLevel() != null) {
//    			sbs.handler(new LoggingHandler(getPipeLineLogLevel()));
//    		}
        	
            sbs.childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 
                	 initializeHandlerByAccept(ch);
                 }
             });
            
            
            final ChannelFuture cf = sbs.bind(context.port()).sync().addListener(new GenericFutureListener<ChannelFuture>() {
                public void operationComplete(ChannelFuture future) throws Exception {

                    if (future.isSuccess()) {
                        logger.info("socket server started !!  bind port={}", context.port());
                        
                        context.requestHandler().init(context);
                    } else {
                        logger.info("socket server start failed !!");
                    }
                }
            });
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
    			@Override
    			public void run() {
    				cf.channel().close();
    			}
    		});
            
            logger.info(sbs.toString());
            
            cf.channel().closeFuture().sync().addListener(new GenericFutureListener<ChannelFuture>() {
                public void operationComplete(ChannelFuture future) throws Exception {
                	
                	context.requestExecutor().shutdown();
                	
                    if (future.isSuccess()) {
                        logger.info("socket server shutdown .... bind port={}", context.port());
                    } else {
                        logger.info("socket server shutdown fail !!! bind port={}", context.port());
                    }
                } 
            });
        } catch (Exception e) {
        	throw new RuntimeException("server start fail", e);
            
        } finally {
            try {
                workerGroup.shutdownGracefully().get();
                bossGroup.shutdownGracefully().get();
            } catch (Exception e) {
            	throw new RuntimeException(e);
            }           
        }
    }
    


    private void configurationContext() {
    	
    	RequestExecutor requestTaskExecutor = null;
    	try {
    	    
        	//Constructor<? extends RequestTaskExecutor> cons = requestExecutorType.getConstructor(new Class[]{int.class});
    		Constructor<? extends RequestExecutor> cons = getRequestExecutorType().getConstructor();
        	requestTaskExecutor = cons.newInstance();
        	requestTaskExecutor.init(getMaxRequests(), getRequestExecutorshutdownWaitSeconds());
        	
        	//TODO requestTaskExecutor 로그 출력
    	} catch (Exception e) {
    		throw new RuntimeException("configuration server context .. requestExecutor create error", e);
    	}

    	int maxConnections = getMaxConnections();
		ConnectionManagerHandler connectionManagerHandler = new ConnectionManagerHandler(maxConnections);
		if (context.requestHandler() instanceof ConnectionRejectListener) {
			connectionManagerHandler.setConnectionRejectListener((ConnectionRejectListener) context.requestHandler());
		}
		
		this.context = new ServerContext(port, requestHandler, this, new InMemoryRequestFactory(), requestTaskExecutor, connectionManagerHandler);
    }
    

    private void initializeHandlerByAccept(SocketChannel sc) {
    	

    	ClientAgent clientAgent = new ClientAgent(sc, context);
    	
		ChannelPipeline cp = sc.pipeline();
		
		if (getPipeLineLogLevel() != null) {
			cp.addLast(new LoggingHandler(getPipeLineLogLevel()));
		}
		
		cp.addLast((ConnectionManagerHandler) context.connectionManager());
		
		ByteLengthBasedInboundHandler readHandler = new ByteLengthBasedInboundHandler(clientAgent.getReader());
		
		cp.addLast(ByteLengthBasedInboundHandler.class.getSimpleName(), readHandler); //4. READ 처리
    }

}
