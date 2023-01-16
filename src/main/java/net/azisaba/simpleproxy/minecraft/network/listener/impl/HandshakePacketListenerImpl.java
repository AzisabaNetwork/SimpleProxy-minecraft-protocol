package net.azisaba.simpleproxy.minecraft.network.listener.impl;

import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleproxy.api.ProxyServer;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.minecraft.network.connection.Mode;
import net.azisaba.simpleproxy.minecraft.network.listener.HandshakePacketListener;
import net.azisaba.simpleproxy.minecraft.network.connection.Connection;
import net.azisaba.simpleproxy.minecraft.network.handshaking.ServerboundHandshakePacket;
import net.azisaba.simpleproxy.minecraft.protocol.Protocol;
import org.jetbrains.annotations.NotNull;

public class HandshakePacketListenerImpl implements HandshakePacketListener {
    private final Connection connection;

    public HandshakePacketListenerImpl(@NotNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(@NotNull ServerboundHandshakePacket packet) {
        if (connection.getPlayerChannel().pipeline().names().contains("message_forwarder")) {
            connection.releasePacketQueue();
        } else {
            // this means the user cannot have servers and virtual-hosts at same time

            ServerInfo serverInfo = connection.getTargetServerForVirtualHost(packet.getServerAddress());
            if (serverInfo == null) {
                // no server to connect, disconnect the client.
                connection.close();
                return;
            }

            ChannelInboundHandlerAdapter messageForwarder =
                    ProxyServer.getProxy()
                            .unsafe()
                            .createMessageForwarder(connection.getPlayerChannel(), connection.getOriginalListenerInfo(), serverInfo);
            connection.getPlayerChannel().pipeline().addBefore("splitter", "message_forwarder", messageForwarder);
            try {
                messageForwarder.getClass().getMethod("activate").invoke(messageForwarder);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        if (connection.getMode() == Mode.VIRTUAL_HOSTS_ONLY) {
            connection.detach();
            return;
        }
        connection.setProtocolVersion(packet.getProtocolVersion());
        connection.setState(Protocol.values()[packet.getNextState()]);
        connection.setListener(new LoginPacketListenerImpl(connection));
    }
}
