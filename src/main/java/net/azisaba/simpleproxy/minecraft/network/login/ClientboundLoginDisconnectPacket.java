package net.azisaba.simpleproxy.minecraft.network.login;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleproxy.minecraft.network.Packet;
import net.azisaba.simpleproxy.minecraft.network.listener.LoginPacketListener;
import org.jetbrains.annotations.NotNull;

public class ClientboundLoginDisconnectPacket extends Packet<LoginPacketListener> {
    public String reason;

    public ClientboundLoginDisconnectPacket() {
    }

    public ClientboundLoginDisconnectPacket(String reason) {
        this.reason = reason;
    }

    @Override
    public void read(@NotNull ByteBuf buf) {
        reason = readString(buf, 262144);
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        writeString(buf, reason, 262144);
    }

    @NotNull
    public String getReason() {
        return reason;
    }

    @Override
    public void handle(@NotNull LoginPacketListener listener) {
        listener.handle(this);
    }
}
