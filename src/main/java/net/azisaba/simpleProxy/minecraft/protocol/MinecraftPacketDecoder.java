package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;

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
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (/*flow == PacketFlow.SERVERBOUND && */msg instanceof ByteBuf) {
            try {
                handle(readFlow, (ByteBuf) msg);
            } catch (NullPointerException ignored) {
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (/*flow == PacketFlow.CLIENTBOUND) && */msg instanceof ByteBuf) {
            try {
                handle(writeFlow, (ByteBuf) msg);
            } catch (NullPointerException ignored) {}
        }
        super.write(ctx, msg, promise);
    }

    private void handle(@NotNull PacketFlow flow, ByteBuf buf) {
        if (buf.readableBytes() != 0) {
            int packetId = Packet.readVarInt(buf);
            Protocol.PacketData<?> packetData = connection.getState().getPackets(flow, connection.getProtocolVersion()).getPacket(packetId);
            if (packetData != null) {
                Packet packet = packetData.getPacketConstructor().get();
                packet.read(buf, flow, connection.getProtocolVersion());
                packet.handle(connection);
            }
        }
    }
}
