package net.azisaba.simpleProxy.minecraft;

import net.azisaba.simpleProxy.api.config.Protocol;
import net.azisaba.simpleProxy.api.event.EventHandler;
import net.azisaba.simpleProxy.api.event.connection.ConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.proxy.ProxyInitializeEvent;
import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.connection.Mode;
import net.azisaba.simpleProxy.minecraft.protocol.DisconnectLogger;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftPacketDecoder;
import net.azisaba.simpleProxy.minecraft.protocol.MinecraftPacketEncoder;
import net.azisaba.simpleProxy.minecraft.protocol.NoopExceptionHandler;
import net.azisaba.simpleProxy.minecraft.protocol.PacketFlow;
import net.azisaba.simpleProxy.minecraft.protocol.Varint21FrameDecoder;
import net.azisaba.simpleProxy.minecraft.protocol.Varint21LengthFieldPrepender;

public class MinecraftProtocolPlugin extends Plugin {
    @EventHandler
    public void onProxyInitialize(ProxyInitializeEvent e) {
        getLogger().info("Generating keypair");
        Minecraft.getKeyPair();
        getLogger().info("Done!");
    }

    @EventHandler
    public void onConnectionInit(ConnectionInitEvent e) {
        if (e.getListenerInfo().getProtocol() == Protocol.TCP && ("minecraft".equals(e.getListenerInfo().getType()) || "minecraft-full".equals(e.getListenerInfo().getType()))) {
            Mode mode = "minecraft-full".equals(e.getListenerInfo().getType()) ? Mode.FULL : Mode.NORMAL;
            getLogger().debug("Registering Minecraft packet handler for {} (Forwarder)", e.getChannel());
            Connection connection = new Connection(mode, e.getChannel(), null);
            e.getChannel().pipeline()
                    .addLast("splitter", new Varint21FrameDecoder())
                    .addLast("decoder", new MinecraftPacketDecoder(PacketFlow.SERVERBOUND, PacketFlow.CLIENTBOUND, connection))
                    .addLast("encoder", new MinecraftPacketEncoder(PacketFlow.SERVERBOUND, PacketFlow.CLIENTBOUND, connection))
                    .addLast("prepender", Varint21LengthFieldPrepender.INSTANCE)
                    .addLast("disconnect_logger", new DisconnectLogger(connection, PacketFlow.CLIENTBOUND))
                    .addLast(NoopExceptionHandler.INSTANCE);
        }
    }

    @EventHandler
    public void onRemoteConnectionInit(RemoteConnectionInitEvent e) {
        if (e.getListenerInfo().getProtocol() == Protocol.TCP && ("minecraft".equals(e.getListenerInfo().getType()) || "minecraft-full".equals(e.getListenerInfo().getType()))) {
            getLogger().debug("Registering Minecraft packet handler for {} (Remote)", e.getChannel());
            Connection connection = e.getSourceChannel().pipeline().get(MinecraftPacketDecoder.class).getConnection();
            connection.setRemoteChannel(e.getChannel());
            e.getChannel().pipeline()
                    .addLast("splitter", new Varint21FrameDecoder())
                    .addLast("decoder", new MinecraftPacketDecoder(PacketFlow.CLIENTBOUND, PacketFlow.SERVERBOUND, connection))
                    .addLast("encoder", new MinecraftPacketEncoder(PacketFlow.CLIENTBOUND, PacketFlow.SERVERBOUND, connection))
                    .addLast("prepender", Varint21LengthFieldPrepender.INSTANCE)
                    .addLast("disconnect_logger", new DisconnectLogger(connection, PacketFlow.SERVERBOUND))
                    .addLast(NoopExceptionHandler.INSTANCE);
        }
    }
}
