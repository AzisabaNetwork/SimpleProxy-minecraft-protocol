package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SimplePacketLogger extends ChannelDuplexHandler {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    private final String prefix;

    public SimplePacketLogger(@NotNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        log(prefix, "IN", msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log(prefix, "OUT", msg);
        super.write(ctx, msg, promise);
    }

    public static void log(String prefix, String type, Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                int id = Packet.readVarInt(buf);
                LOGGER.info(prefix + " {} {} {}", type, id, msg);
            } catch (Exception ignored) {
                LOGGER.info(prefix + " {} ? {}", type, msg);
            } finally {
                buf.resetReaderIndex();
            }
        }
    }
}
