package net.azisaba.simpleproxy.minecraft.network.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import net.azisaba.simpleproxy.api.ProxyServer;
import net.azisaba.simpleproxy.api.config.ListenerInfo;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.api.yaml.YamlArray;
import net.azisaba.simpleproxy.api.yaml.YamlObject;
import net.azisaba.simpleproxy.minecraft.network.Packet;
import net.azisaba.simpleproxy.minecraft.network.PacketListener;
import net.azisaba.simpleproxy.minecraft.network.login.ClientboundEncryptionRequestPacket;
import net.azisaba.simpleproxy.minecraft.network.listener.impl.HandshakePacketListenerImpl;
import net.azisaba.simpleproxy.minecraft.protocol.MinecraftDecipher;
import net.azisaba.simpleproxy.minecraft.protocol.MinecraftEncipher;
import net.azisaba.simpleproxy.minecraft.protocol.PacketFlow;
import net.azisaba.simpleproxy.minecraft.protocol.Protocol;
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
import java.util.concurrent.atomic.AtomicReference;

@ChannelHandler.Sharable
public class Connection extends ChannelDuplexHandler {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    private static final Random RANDOM = new Random();
    private final ListenerInfo originalListenerInfo;
    private final Mode mode;
    private final AtomicReference<PacketListener> listener = new AtomicReference<>(new HandshakePacketListenerImpl(this));
    private Channel playerChannel;
    private Channel remoteChannel;
    //private Protocol state = Protocol.HANDSHAKE;
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

    @NotNull
    public PacketListener getListener() {
        return Objects.requireNonNull(listener.get(), "listener not set");
    }

    public void setListener(@NotNull PacketListener listener) {
        this.listener.set(Objects.requireNonNull(listener, "listener"));
    }

    public void setState(@NotNull Protocol state) {
        if (ProxyServer.getProxy().getConfig().isDebug()) {
            LOGGER.info("Transitioning state of {} to {}", playerChannel.remoteAddress(), state);
        }
        //this.state = state;
        if (playerChannel != null) {
            playerChannel.attr(Protocol.PROTOCOL_KEY).set(state);
        }
        if (remoteChannel != null) {
            remoteChannel.attr(Protocol.PROTOCOL_KEY).set(state);
        }
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
        if (playerChannel != null) {
            return playerChannel.attr(Protocol.PROTOCOL_KEY).get();
        }
        if (remoteChannel != null) {
            return remoteChannel.attr(Protocol.PROTOCOL_KEY).get();
        }
        //return state;
        throw new RuntimeException("Protocol not set");
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
                .addBefore(PipelineNames.SPLITTER, PipelineNames.DECRYPT, new MinecraftDecipher(cipher1))
                .addBefore(PipelineNames.PREPENDER, PipelineNames.ENCRYPT, new MinecraftEncipher(cipher2));
        remoteChannel.pipeline()
                .addBefore(PipelineNames.SPLITTER, PipelineNames.DECRYPT, new MinecraftDecipher(cipher1))
                .addBefore(PipelineNames.PREPENDER, PipelineNames.ENCRYPT, new MinecraftEncipher(cipher2));
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

    public void detach() {
        if (playerChannel != null) {
            for (String name : PipelineNames.values()) {
                if (playerChannel.pipeline().names().contains(name)) {
                    playerChannel.pipeline().remove(name);
                }
            }
        }
        if (remoteChannel != null) {
            for (String name : PipelineNames.values()) {
                if (remoteChannel.pipeline().names().contains(name)) {
                    remoteChannel.pipeline().remove(name);
                }
            }
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

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (!(msg instanceof Packet<?>)) {
            ctx.fireChannelRead(msg);
            return;
        }
        Packet<?> packet = (Packet<?>) msg;
        genericsHack(packet, getListener());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!(msg instanceof Packet<?>)) {
            ctx.write(msg, promise);
            return;
        }
        Packet<?> packet = (Packet<?>) msg;
        genericsHack(packet, getListener());
    }

    @SuppressWarnings("unchecked")
    private <P extends PacketListener> void genericsHack(@NotNull Packet<P> packet, @NotNull PacketListener listener) {
        packet.handle((P) listener);
    }
}
