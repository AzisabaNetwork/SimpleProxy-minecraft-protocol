package net.azisaba.simpleproxy.minecraft.network.listener;

import net.azisaba.simpleproxy.minecraft.network.PacketListener;
import net.azisaba.simpleproxy.minecraft.network.login.ClientboundEncryptionRequestPacket;
import net.azisaba.simpleproxy.minecraft.network.login.ClientboundLoginDisconnectPacket;
import net.azisaba.simpleproxy.minecraft.network.login.ServerboundLoginStartPacket;
import org.jetbrains.annotations.NotNull;

public interface LoginPacketListener extends PacketListener {
    void handle(@NotNull ServerboundLoginStartPacket packet);
    void handle(@NotNull ClientboundLoginDisconnectPacket packet);
    void handle(@NotNull ClientboundEncryptionRequestPacket packet);
}
