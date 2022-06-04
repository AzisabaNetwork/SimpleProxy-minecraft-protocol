package net.azisaba.simpleProxy.minecraft.network.handshaking;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.network.BadPacketException;
import net.azisaba.simpleProxy.minecraft.network.listener.HandshakePacketListener;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import org.jetbrains.annotations.NotNull;

public class ServerboundHandshakePacket extends Packet<HandshakePacketListener> {
    private static final BadPacketException INVALID_PROTOCOL_VERSION_EXCEPTION = new BadPacketException("Invalid protocol version. " + Packet.CACHED_EXCEPTION_SUFFIX);
    private static final BadPacketException INVALID_NEXT_STATE_EXCEPTION = new BadPacketException("Invalid Next State. " + Packet.CACHED_EXCEPTION_SUFFIX);
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
        checkValues();
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        checkValues();
        writeVarInt(buf, protocolVersion);
        writeString(buf, serverAddress, 255);
        buf.writeShort(port);
        writeVarInt(buf, nextState);
    }

    @Override
    public void handle(@NotNull HandshakePacketListener listener) {
        listener.handle(this);
    }

    private void checkValues() {
        if (protocolVersion < 0) {
            if (DEBUG) {
                throw new BadPacketException("Invalid protocol version: " + protocolVersion);
            }
            throw INVALID_PROTOCOL_VERSION_EXCEPTION;
        }
        if (nextState != 1 && nextState != 2) {
            if (DEBUG) {
                throw new BadPacketException("Invalid Next State: " + nextState);
            }
            throw INVALID_NEXT_STATE_EXCEPTION;
        }
    }

    public int getPort() {
        return port;
    }

    public int getNextState() {
        return nextState;
    }

    @NotNull
    public String getServerAddress() {
        return serverAddress;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }
}
