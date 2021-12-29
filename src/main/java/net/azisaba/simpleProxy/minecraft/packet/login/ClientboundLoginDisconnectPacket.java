package net.azisaba.simpleProxy.minecraft.packet.login;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;

public class ClientboundLoginDisconnectPacket extends Packet {
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

    @Override
    public void handle(@NotNull Connection connection) {
        // no-op
    }
}
