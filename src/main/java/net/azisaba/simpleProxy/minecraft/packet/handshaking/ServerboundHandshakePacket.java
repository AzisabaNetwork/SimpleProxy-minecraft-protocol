package net.azisaba.simpleProxy.minecraft.packet.handshaking;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.BadPacketException;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import net.azisaba.simpleProxy.minecraft.protocol.Protocol;
import org.jetbrains.annotations.NotNull;

public class ServerboundHandshakePacket extends Packet {
    private static final BadPacketException INVALID_NEXT_STATE_EXCEPTION = new BadPacketException("Invalid Next State");
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
