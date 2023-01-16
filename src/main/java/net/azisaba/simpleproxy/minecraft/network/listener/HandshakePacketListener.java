package net.azisaba.simpleproxy.minecraft.network.listener;

import net.azisaba.simpleproxy.minecraft.network.PacketListener;
import net.azisaba.simpleproxy.minecraft.network.handshaking.ServerboundHandshakePacket;
import org.jetbrains.annotations.NotNull;

public interface HandshakePacketListener extends PacketListener {
    void handle(@NotNull ServerboundHandshakePacket packet);
}
