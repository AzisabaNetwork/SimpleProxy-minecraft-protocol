package net.azisaba.simpleProxy.minecraft.packet.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;
import net.azisaba.simpleProxy.minecraft.Minecraft;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.connection.Mode;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ServerboundLoginStartPacket extends Packet {
    private static final Logger LOGGER = LogManager.getLogger("minecraft-protocol");
    public String username;

    @Override
    public void read(@NotNull ByteBuf buf) {
        username = readString(buf, 16);
    }

    @Override
    public void write(@NotNull ByteBuf buf) {
        writeString(buf, username, 16);
    }

    @Override
    public void handle(@NotNull Connection connection) {
        connection.setUsername(username);
        Minecraft.addConnection(connection);
        LOGGER.info("{} is trying to login from {} ({} active connections)", username, connection.getPlayerChannel().remoteAddress(), Minecraft.getConnections().size());
        if (connection.getMode() == Mode.NORMAL) {
            ChannelPipeline pipeline = connection.getPlayerChannel().pipeline();
            pipeline.remove("splitter");
            pipeline.remove("decoder");
            pipeline.remove("encoder");
            pipeline.remove("prepender");
            pipeline = connection.getRemoteChannel().pipeline();
            pipeline.remove("splitter");
            pipeline.remove("decoder");
            pipeline.remove("encoder");
            pipeline.remove("prepender");
        }
    }
}
