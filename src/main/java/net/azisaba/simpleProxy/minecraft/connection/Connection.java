package net.azisaba.simpleProxy.minecraft.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.minecraft.packet.login.ClientboundEncryptionRequestPacket;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftDecipher;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftEncipher;
import net.azisaba.simpleProxy.minecraft.protocol.PacketFlow;
import net.azisaba.simpleProxy.minecraft.protocol.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import java.util.Objects;

public class Connection {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Mode mode;
    private Channel playerChannel;
    private Channel remoteChannel;
    private Protocol state = Protocol.HANDSHAKE;
    private int protocolVersion = 0;
    private boolean encrypted = false;
    private ClientboundEncryptionRequestPacket encryptionRequestPacket;
    private String username;

    public Connection(@NotNull Mode mode, @Nullable Channel playerChannel, @Nullable Channel remoteChannel) {
        if (mode == Mode.FULL) throw new RuntimeException(mode + " mode not supported");
        this.mode = mode;
        this.playerChannel = playerChannel;
        this.remoteChannel = remoteChannel;
    }

    @NotNull
    public Mode getMode() {
        return mode;
    }

    public void setState(@NotNull Protocol state) {
        if (ProxyServer.getProxy().getConfig().isDebug()) {
            LOGGER.info("Transitioning state of {} to {}", playerChannel.remoteAddress(), state);
        }
        this.state = state;
    }

    public void setPlayerChannel(@NotNull Channel playerChannel) {
        this.playerChannel = playerChannel;
    }

    @NotNull
    public Channel getPlayerChannel() {
        return Objects.requireNonNull(playerChannel);
    }

    public void setRemoteChannel(@NotNull Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    @NotNull
    public Channel getRemoteChannel() {
        return Objects.requireNonNull(remoteChannel);
    }

    @NotNull
    public Protocol getState() {
        return state;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncryptionRequestPacket(ClientboundEncryptionRequestPacket encryptionRequestPacket) {
        this.encryptionRequestPacket = encryptionRequestPacket;
    }

    public ClientboundEncryptionRequestPacket getEncryptionRequestPacket() {
        return encryptionRequestPacket;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setEncryptionKey(@NotNull Cipher cipher1, @NotNull Cipher cipher2) {
        encrypted = true;
        playerChannel.pipeline()
                .addBefore("splitter", "decrypt", new MinecraftDecipher(cipher1))
                .addBefore("prepender", "encrypt", new MinecraftEncipher(cipher2));
        remoteChannel.pipeline()
                .addBefore("splitter", "decrypt", new MinecraftDecipher(cipher1))
                .addBefore("prepender", "encrypt", new MinecraftEncipher(cipher2));
    }

    public void sendPacket(@NotNull PacketFlow flow, Object packet) {
        if (flow == PacketFlow.CLIENTBOUND) {
            playerChannel.writeAndFlush(packet);
        } else {
            remoteChannel.writeAndFlush(packet);
        }
    }
}
