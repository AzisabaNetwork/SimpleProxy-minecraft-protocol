package net.azisaba.simpleProxy.minecraft.packet.login;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;

public class ClientboundEncryptionRequestPacket extends Packet {
    public String serverId; // String (20)
    public int publicKeyLength; // VarInt
    public byte[] publicKey; // Byte Array
    public int verifyTokenLength; // Verify Token Length
    private byte[] verifyToken; // Byte Array

    @Override
    public void read(@NotNull ByteBuf buf) {
        serverId = readString(buf, 20);
        publicKeyLength = readVarInt(buf);
        publicKey = buf.readBytes(publicKeyLength).array();
        verifyTokenLength = readVarInt(buf);
        verifyToken = buf.readBytes(verifyTokenLength).array();
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        writeString(buf, serverId, 20);
        writeVarInt(buf, publicKeyLength);
        buf.writeBytes(publicKey);
        writeVarInt(buf, verifyTokenLength);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void handle(@NotNull Connection connection) {
        connection.setEncryptionRequestPacket(this);
    }
}
