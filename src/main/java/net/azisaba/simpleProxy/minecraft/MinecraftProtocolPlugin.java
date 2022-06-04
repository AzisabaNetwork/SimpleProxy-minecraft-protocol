package net.azisaba.simpleProxy.minecraft;

import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.Protocol;
import net.azisaba.simpleProxy.api.event.EventHandler;
import net.azisaba.simpleProxy.api.event.connection.ConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionActiveEvent;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.proxy.ProxyInitializeEvent;
import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.minecraft.network.connection.Connection;
import net.azisaba.simpleProxy.minecraft.network.connection.Mode;
import net.azisaba.simpleProxy.minecraft.network.connection.PipelineNames;
import net.azisaba.simpleProxy.minecraft.protocol.DisconnectLogger;
import net.azisaba.simpleProxy.minecraft.protocol.DummyMessageForwarder;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftPacketDecoder;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftPacketEncoder;
import net.azisaba.simpleProxy.minecraft.protocol.NoopExceptionHandler;
import net.azisaba.simpleProxy.minecraft.protocol.PacketFlow;
import net.azisaba.simpleProxy.minecraft.protocol.Varint21FrameDecoder;
import net.azisaba.simpleProxy.minecraft.protocol.Varint21LengthFieldPrepender;

import java.util.Objects;

public class MinecraftProtocolPlugin extends Plugin {
    @EventHandler
    public void onProxyInitialize(ProxyInitializeEvent e) {
        getLogger().info("Generating keypair");
        Minecraft.getKeyPair();
        getLogger().info("Done!");
    }

    @EventHandler
    public void onConnectionInit(ConnectionInitEvent e) {
        if (shouldHandle(e.getListenerInfo())) {
            e.getChannel()
                    .attr(net.azisaba.simpleProxy.minecraft.protocol.Protocol.PROTOCOL_KEY)
                    .set(net.azisaba.simpleProxy.minecraft.protocol.Protocol.HANDSHAKE);
            Mode mode = Objects.requireNonNull(Mode.getMode(e.getListenerInfo().getType()), "mode should never be null");
            getLogger().debug("Registering Minecraft packet handler for {} (Forwarder)", e.getChannel());
            Connection connection = new Connection(e.getListenerInfo(), mode, e.getChannel(), null);
            e.getChannel().pipeline()
                    .addLast(new DummyMessageForwarder(connection))
                    .addLast(PipelineNames.SPLITTER, new Varint21FrameDecoder())
                    .addLast(PipelineNames.DECODER, new MinecraftPacketDecoder(PacketFlow.SERVERBOUND, PacketFlow.CLIENTBOUND, connection))
                    .addLast(PipelineNames.PACKET_HANDLER, connection)
                    .addLast(PipelineNames.ENCODER, new MinecraftPacketEncoder(PacketFlow.SERVERBOUND, PacketFlow.CLIENTBOUND, connection))
                    .addLast(PipelineNames.PREPENDER, Varint21LengthFieldPrepender.INSTANCE)
                    .addLast(PipelineNames.DISCONNECT_LOGGER, new DisconnectLogger(connection, PacketFlow.CLIENTBOUND))
                    .addLast(NoopExceptionHandler.INSTANCE);
        }
    }

    @EventHandler
    public void onRemoteConnectionInit(RemoteConnectionInitEvent e) {
        if (shouldHandle(e.getListenerInfo())) {
            DisconnectLogger disconnectLogger = e.getSourceChannel().pipeline().get(DisconnectLogger.class);
            if (disconnectLogger == null) {
                // disconnected?
                return;
            }
            Connection connection = disconnectLogger.getConnection();
            getLogger().debug("Registering Minecraft packet handler for {} (Remote)", e.getChannel());
            connection.setRemoteChannel(e.getChannel());
            e.getChannel().pipeline()
                    .addLast(PipelineNames.SPLITTER, new Varint21FrameDecoder())
                    .addLast(PipelineNames.DECODER, new MinecraftPacketDecoder(PacketFlow.CLIENTBOUND, PacketFlow.SERVERBOUND, connection))
                    .addLast(PipelineNames.PACKET_HANDLER, connection)
                    .addLast(PipelineNames.ENCODER, new MinecraftPacketEncoder(PacketFlow.CLIENTBOUND, PacketFlow.SERVERBOUND, connection))
                    .addLast(PipelineNames.PREPENDER, Varint21LengthFieldPrepender.INSTANCE)
                    .addLast(PipelineNames.DISCONNECT_LOGGER, new DisconnectLogger(connection, PacketFlow.SERVERBOUND))
                    .addLast(NoopExceptionHandler.INSTANCE);
        }
    }

    @EventHandler
    public void onRemoteConnectionActivation(RemoteConnectionActiveEvent e) {
        if (shouldHandle(e.getListenerInfo()) && e.getSourceChannel().isActive()) {
            if (Mode.getMode(e.getListenerInfo().getType()) == Mode.VIRTUAL_HOSTS_ONLY) {
                return;
            }
            e.getSourceChannel()
                    .pipeline()
                    .get(DisconnectLogger.class)
                    .getConnection()
                    .flushPacketQueue();
        }
    }

    private boolean shouldHandle(ListenerInfo info) {
        return info.getProtocol() == Protocol.TCP && Mode.getMode(info.getType()) != null;
    }
}
