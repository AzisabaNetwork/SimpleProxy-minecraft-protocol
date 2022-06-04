package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.minecraft.network.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DummyMessageForwarder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");

    private final Connection connection;

    public DummyMessageForwarder(@NotNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (connection.packetsQueue != null) {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                connection.packetsQueue.add(buf.copy());
            } else {
                connection.packetsQueue.add(msg);
            }
        } else {
            // packetsQueue was freed, so this handler is no longer needed.
            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        int freed = connection.releasePacketQueue();
        if (freed > 0 && ProxyServer.getProxy().getConfig().isVerbose()) {
            LOGGER.info("Freed {} packets from {}", freed, ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }
}
