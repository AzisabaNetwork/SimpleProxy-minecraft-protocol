package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import org.jetbrains.annotations.NotNull;

public class DummyMessageForwarder extends ChannelInboundHandlerAdapter {
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
            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }
}
