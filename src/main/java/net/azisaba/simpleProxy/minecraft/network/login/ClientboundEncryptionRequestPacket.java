package net.azisaba.simpleProxy.minecraft.network.login;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import net.azisaba.simpleProxy.minecraft.network.listener.LoginPacketListener;
import org.jetbrains.annotations.NotNull;

public class ClientboundEncryptionRequestPacket extends Packet<LoginPacketListener> {
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

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }

    public int getPublicKeyLength() {
        return publicKeyLength;
    }

    public int getVerifyTokenLength() {
        return verifyTokenLength;
    }

    public String getServerId() {
        return serverId;
    }

    @Override
    public void handle(@NotNull LoginPacketListener listener) {
        listener.handle(this);
    }
}
