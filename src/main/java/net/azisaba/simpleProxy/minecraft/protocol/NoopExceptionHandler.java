package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.api.ProxyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class NoopExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final NoopExceptionHandler INSTANCE = new NoopExceptionHandler();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ProxyServer.getProxy().getConfig().isDebug()) {
            LOGGER.warn("Caught exception somewhere in the pipeline", cause);
        }
    }
}
