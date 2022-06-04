package net.azisaba.simpleProxy.minecraft.network.listener;

import net.azisaba.simpleProxy.minecraft.network.PacketListener;
import net.azisaba.simpleProxy.minecraft.network.handshaking.ServerboundHandshakePacket;
import org.jetbrains.annotations.NotNull;

public interface HandshakePacketListener extends PacketListener {
    void handle(@NotNull ServerboundHandshakePacket packet);
}
