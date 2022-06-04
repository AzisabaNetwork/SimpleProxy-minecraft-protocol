package net.azisaba.simpleProxy.minecraft.network.login;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import net.azisaba.simpleProxy.minecraft.network.listener.LoginPacketListener;
import org.jetbrains.annotations.NotNull;

public class ServerboundLoginStartPacket extends Packet<LoginPacketListener> {
    public String username;

    @Override
    public void read(@NotNull ByteBuf buf) {
        username = readString(buf, 16);
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        writeString(buf, username, 16);
    }

    @Override
    public void handle(@NotNull LoginPacketListener listener) {
        listener.handle(this);
    }

    @NotNull
    public String getUsername() {
        return username;
    }
}
