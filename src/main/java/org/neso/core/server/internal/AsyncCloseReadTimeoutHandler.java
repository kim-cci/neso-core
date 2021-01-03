package org.neso.core.server.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * ReadTimeoutHandler 커스텀
 * 
 * readTimedOut override 함
 */
public class AsyncCloseReadTimeoutHandler extends ChannelInboundHandlerAdapter {
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final long timeoutNanos;

    private long lastReadTime;

    private volatile ScheduledFuture<?> timeout;

    private volatile int state; // 0 - none, 1 - Initialized, 2 - Destroyed;

    private volatile boolean reading;
    private boolean closed;
    
    private ByteLengthBasedReader reader;

    public AsyncCloseReadTimeoutHandler(int timeoutSeconds, ByteLengthBasedReader reader) {
        this(timeoutSeconds, TimeUnit.SECONDS, reader);
    }


    public AsyncCloseReadTimeoutHandler(long timeout, TimeUnit unit, ByteLengthBasedReader reader) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        if (timeout <= 0) {
            timeoutNanos = 0;
        } else {
            timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
        }
        
        this.reader = reader;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            // channelActvie() event has been fired already, which means this.channelActive() will
            // not be invoked. We have to initialize here instead.
            initialize(ctx);
        } else {
            // channelActive() event has not been fired yet.  this.channelActive() will be invoked
            // and initialization will occur there.
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // Initialize early if channel is active already.
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // This method will be invoked only if this handler was added
        // before channelActive() event is fired.  If a user adds this handler
        // after the channelActive() event, initialize() will be called by beforeAdd().
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        reading = true;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        lastReadTime = System.nanoTime();
        reading = false;
        ctx.fireChannelReadComplete();
    }

    private void initialize(ChannelHandlerContext ctx) {
        // Avoid the case where destroy() is called before scheduling timeouts.
        // See: https://github.com/netty/netty/issues/143
        switch (state) {
        case 1:
        case 2:
            return;
        }

        state = 1;

        lastReadTime = System.nanoTime();
        if (timeoutNanos > 0) {
            timeout = ctx.executor().schedule(
                    new ReadTimeoutTask(ctx),
                    timeoutNanos, TimeUnit.NANOSECONDS);
        }
    }

    private void destroy() {
        state = 2;

        if (timeout != null) {
            timeout.cancel(false);
            timeout = null;
        }
    }


    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
        	reader.onReadException(ReadTimeoutException.INSTANCE);
            closed = true;
        }
    }

    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = timeoutNanos;
            if (!reading) {
                nextDelay -= System.nanoTime() - lastReadTime;
            }

            if (nextDelay <= 0) {
                // Read timed out - set a new timeout and notify the callback.
                timeout = ctx.executor().schedule(this, timeoutNanos, TimeUnit.NANOSECONDS);
                try {
                    readTimedOut(ctx);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                timeout = ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}
