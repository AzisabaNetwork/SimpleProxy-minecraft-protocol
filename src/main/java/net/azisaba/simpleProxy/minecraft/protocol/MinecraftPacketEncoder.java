package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinecraftPacketEncoder extends ChannelDuplexHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final PacketFlow readFlow;
    private final PacketFlow writeFlow;
    private final Connection connection;

    public MinecraftPacketEncoder(@NotNull PacketFlow readFlow, @NotNull PacketFlow writeFlow, @NotNull Connection connection) {
        this.readFlow = readFlow;
        this.writeFlow = writeFlow;
        this.connection = connection;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if ((true/* || flow == PacketFlow.CLIENTBOUND*/)) {
            ByteBuf buf = null;
            try {
                buf = handle(readFlow, msg);
            } catch (NullPointerException ignored) {}
            if (buf != null) {
                super.channelRead(ctx, buf);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ((true/* || flow == PacketFlow.SERVERBOUND*/)) {
            ByteBuf buf = null;
            try {
                buf = handle(writeFlow, msg);
            } catch (NullPointerException ignored) {}
            if (buf != null) {
                super.write(ctx, buf, promise);
                return;
            }
        }
        super.write(ctx, msg, promise);
    }

    @Nullable
    private ByteBuf handle(@NotNull PacketFlow flow, Object in) {
        if (in instanceof ByteBuf) {
            return (ByteBuf) in;
        }
        if (in instanceof Packet) {
            ByteBuf buf = Unpooled.buffer();
            int id = connection.getState().getPackets(flow, connection.getProtocolVersion()).getPacketId(in.getClass());
            if (id == -1) {
                LOGGER.warn("Trying to send unknown packet: {}", in.getClass().getTypeName());
            }
            Packet.writeVarInt(buf, id);
            ((Packet) in).write(buf, flow, connection.getProtocolVersion());
            return buf;
        }
        return null;
    }
}