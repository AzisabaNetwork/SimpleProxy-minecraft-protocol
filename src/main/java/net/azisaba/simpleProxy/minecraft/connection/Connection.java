package net.azisaba.simpleProxy.minecraft.connection;

import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.api.yaml.YamlArray;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Connection {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private final ListenerInfo originalListenerInfo;
    private final Mode mode;
    private Channel playerChannel;
    private Channel remoteChannel;
    private Protocol state = Protocol.HANDSHAKE;
    private int protocolVersion = 0;
    private boolean encrypted = false;
    private ClientboundEncryptionRequestPacket encryptionRequestPacket;
    private String username;
    public List<Object> packetsQueue = Collections.synchronizedList(new ArrayList<>());

    public Connection(@NotNull ListenerInfo listenerInfo, @NotNull Mode mode, @Nullable Channel playerChannel, @Nullable Channel remoteChannel) {
        if (mode == Mode.FULL) {
            throw new RuntimeException(mode + " mode not supported");
        }
        this.originalListenerInfo = listenerInfo;
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
        Objects.requireNonNull(cipher1, "cipher1");
        Objects.requireNonNull(cipher2, "cipher2");
        playerChannel.pipeline()
                .addBefore("splitter", "decrypt", new MinecraftDecipher(cipher1))
                .addBefore("prepender", "encrypt", new MinecraftEncipher(cipher2));
        remoteChannel.pipeline()
                .addBefore("splitter", "decrypt", new MinecraftDecipher(cipher1))
                .addBefore("prepender", "encrypt", new MinecraftEncipher(cipher2));
        encrypted = true;
    }

    public void sendPacket(@NotNull PacketFlow flow, Object packet) {
        if (flow == PacketFlow.CLIENTBOUND) {
            if (playerChannel == null) {
                throw new IllegalStateException("Player channel is null");
            }
            playerChannel.writeAndFlush(packet);
        } else {
            if (remoteChannel == null) {
                throw new IllegalStateException("Remote channel is null");
            }
            remoteChannel.writeAndFlush(packet);
        }
    }

    @NotNull
    public ListenerInfo getOriginalListenerInfo() {
        return originalListenerInfo;
    }

    @Nullable
    public ServerInfo getTargetServerForVirtualHost(@NotNull String serverAddress) {
        ListenerInfo info = getOriginalListenerInfo();
        ServerInfo randomServerInfo;
        if (info.getServers().isEmpty()) {
            randomServerInfo = null;
        } else {
            randomServerInfo = info.getServers().get(RANDOM.nextInt(info.getServers().size()));
        }
        YamlObject virtualHosts = info.getConfig().getObject("virtual-hosts");
        if (virtualHosts == null) {
            return randomServerInfo;
        }
        YamlArray servers = virtualHosts.getArray(serverAddress);
        if (servers == null || servers.isEmpty()) {
            servers = virtualHosts.getArray("default");
        }
        if (servers == null || servers.isEmpty()) {
            return randomServerInfo;
        }
        YamlObject server = servers.getObject(RANDOM.nextInt(servers.size()));
        try {
            return ProxyServer.getProxy().unsafe().createServerInfo(server);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to parse virtual host for {}", serverAddress, e);
            return randomServerInfo;
        }
    }

    public int releasePacketQueue() {
        int freed = 0;
        if (packetsQueue != null) {
            for (Object o : packetsQueue) {
                if (ReferenceCountUtil.release(o)) {
                    freed++;
                }
            }
            packetsQueue = null;
        }
        return freed;
    }

    public void flushPacketQueue() {
        if (remoteChannel == null) {
            throw new IllegalStateException("Remote channel is null");
        }
        if (packetsQueue != null) {
            for (Object o : packetsQueue) {
                sendPacket(PacketFlow.SERVERBOUND, o);
            }
            packetsQueue = null;
        }
    }

    public void close() {
        releasePacketQueue();
        if (playerChannel != null) {
            playerChannel.close();
        }
        if (remoteChannel != null) {
            remoteChannel.close();
        }
    }
}
