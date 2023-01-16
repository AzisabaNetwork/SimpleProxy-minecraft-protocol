package net.azisaba.simpleproxy.minecraft.network.listener.impl;

import net.azisaba.simpleproxy.minecraft.Minecraft;
import net.azisaba.simpleproxy.minecraft.network.connection.Connection;
import net.azisaba.simpleproxy.minecraft.network.connection.Mode;
import net.azisaba.simpleproxy.minecraft.network.listener.LoginPacketListener;
import net.azisaba.simpleproxy.minecraft.network.login.ClientboundEncryptionRequestPacket;
import net.azisaba.simpleproxy.minecraft.network.login.ClientboundLoginDisconnectPacket;
import net.azisaba.simpleproxy.minecraft.network.login.ServerboundLoginStartPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class LoginPacketListenerImpl implements LoginPacketListener {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    private final Connection connection;

    public LoginPacketListenerImpl(@NotNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(@NotNull ServerboundLoginStartPacket packet) {
        connection.setUsername(packet.getUsername());
        Minecraft.addConnection(connection);
        LOGGER.info("{} is trying to login from {} ({} active connections)", packet.getUsername(),
                connection.getPlayerChannel().remoteAddress(), Minecraft.getConnections().size());
        if (connection.getMode() == Mode.NORMAL) {
            connection.detach();
        }
    }

    @Override
    public void handle(@NotNull ClientboundLoginDisconnectPacket packet) {
        // no-op
    }

    @Override
    public void handle(@NotNull ClientboundEncryptionRequestPacket packet) {
        connection.setEncryptionRequestPacket(packet);
    }
}
