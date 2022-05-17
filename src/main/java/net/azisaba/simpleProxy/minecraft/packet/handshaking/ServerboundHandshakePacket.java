package net.azisaba.simpleProxy.minecraft.packet.handshaking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.BadPacketException;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import net.azisaba.simpleProxy.minecraft.protocol.Protocol;
import org.jetbrains.annotations.NotNull;

public class ServerboundHandshakePacket extends Packet {
    private static final BadPacketException INVALID_NEXT_STATE_EXCEPTION = new BadPacketException("Invalid Next State " + Packet.CACHED_EXCEPTION_SUFFIX);
    public int protocolVersion; // VarInt
    public String serverAddress; // String (255)
    public int port; // Unsigned Short
    public int nextState; // VarInt

    @Override
    public void read(@NotNull ByteBuf buf) {
        protocolVersion = readVarInt(buf);
        serverAddress = readString(buf, 255);
        port = buf.readUnsignedShort();
        nextState = readVarInt(buf);
        if (nextState != 1 && nextState != 2) {
            if (DEBUG) {
                throw new BadPacketException("Invalid Next State: " + nextState);
            }
            throw INVALID_NEXT_STATE_EXCEPTION;
        }
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        writeVarInt(buf, protocolVersion);
        writeString(buf, serverAddress, 255);
        buf.writeShort(port);
        writeVarInt(buf, nextState);
    }

    @Override
    public void handle(@NotNull Connection connection) {
        if (connection.getPlayerChannel().pipeline().names().contains("message_forwarder")) {
            connection.packetsQueue = null;
        } else {
            // this means the user cannot have servers and virtual-hosts at same time

            ServerInfo serverInfo = connection.getTargetServerForVirtualHost(serverAddress);
            if (serverInfo == null) {
                // no server to connect, disconnect the client.
                connection.close();
                return;
            }

            ChannelInboundHandlerAdapter messageForwarder =
                    ProxyServer.getProxy()
                            .unsafe()
                            .createMessageForwarder(connection.getPlayerChannel(), connection.getOriginalListenerInfo(), serverInfo);
            connection.getPlayerChannel().pipeline().addBefore("splitter", "message_forwarder", messageForwarder);
            try {
                messageForwarder.getClass().getMethod("activate").invoke(messageForwarder);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        connection.setProtocolVersion(protocolVersion);
        if (nextState != 1 && nextState != 2) {
            if (DEBUG) {
                throw new BadPacketException("Invalid Next State: " + nextState);
            }
            throw INVALID_NEXT_STATE_EXCEPTION;
        }
        connection.setState(Protocol.values()[nextState]);
    }
}
