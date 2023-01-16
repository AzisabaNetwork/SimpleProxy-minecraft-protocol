package net.azisaba.simpleproxy.minecraft.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleproxy.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class NoopExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    public static final NoopExceptionHandler INSTANCE = new NoopExceptionHandler();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Packet.DEBUG) {
            LOGGER.warn("Caught exception somewhere in the pipeline", cause);
        }
    }
}
