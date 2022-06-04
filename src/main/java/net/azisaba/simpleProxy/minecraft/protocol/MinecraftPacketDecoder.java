package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import net.azisaba.simpleProxy.minecraft.network.connection.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinecraftPacketDecoder extends ChannelDuplexHandler {
    private final PacketFlow readFlow;
    private final PacketFlow writeFlow;
    private final Connection connection;

    public MinecraftPacketDecoder(@NotNull PacketFlow readFlow, @NotNull PacketFlow writeFlow, @NotNull Connection connection) {
        this.readFlow = readFlow;
        this.writeFlow = writeFlow;
        this.connection = connection;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        process(readFlow, msg, ctx::fireChannelRead);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        process(writeFlow, msg, o -> ctx.write(o, promise));
    }

    private void process(PacketFlow flow, Object msg, @NotNull Consumer<@NotNull Object> readWrite) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                Packet<?> packet = decode(flow, buf);
                if (packet != null) {
                    buf.release();
                    readWrite.accept(packet);
                    return;
                }
            } catch (NullPointerException ignored) {
            }
        }
        readWrite.accept(msg);
    }

    @Nullable
    private Packet<?> decode(@NotNull PacketFlow flow, ByteBuf buf) {
        if (buf.readableBytes() != 0) {
            int packetId = Packet.readVarInt(buf);
            try {
                Protocol.PacketData<?> packetData = connection.getState().getPackets(flow, connection.getProtocolVersion()).getPacket(packetId);
                if (packetData != null) {
                    Packet<?> packet = packetData.getPacketConstructor().get();
                    packet.read(buf, flow, connection.getProtocolVersion());
                    return packet;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to handle packet " + packetId, e);
            }
        }
        return null;
    }
}
