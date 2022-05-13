package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.minecraft.Minecraft;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DisconnectLogger extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    private final Connection connection;
    private final PacketFlow flow;

    public DisconnectLogger(@NotNull Connection connection, @NotNull PacketFlow flow) {
        this.connection = connection;
        this.flow = flow;
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (connection.getUsername() == null) return;
        String what = "Forwarder";
        if (flow == PacketFlow.SERVERBOUND) what = "Remote";
        Minecraft.unregisterConnection(connection);
        if (flow == PacketFlow.CLIENTBOUND) {
            LOGGER.info("{}: {} disconnected (from {}) ({} active connections)", what, connection.getUsername(), connection.getPlayerChannel().remoteAddress(), Minecraft.getConnections().size());
        }
        super.channelInactive(ctx);
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }
}
