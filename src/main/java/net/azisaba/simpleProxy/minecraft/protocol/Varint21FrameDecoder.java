package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CorruptedFrameException;
import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.minecraft.network.BadPacketException;
import net.azisaba.simpleProxy.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Varint21FrameDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final CorruptedFrameException INVALID_LENGTH_EXCEPTION = new CorruptedFrameException("length wider than 21-bit. " + Packet.CACHED_EXCEPTION_SUFFIX);
    private Integer count = null;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }
        if (Packet.DEBUG) {
            if (count == null) {
                count = 0;
            }
            count++;
        }
        ByteBuf buf = (ByteBuf) msg;
        try {
            handleBuf(ctx, buf);
        } catch (Exception e) {
            if (Packet.DEBUG) {
                throw new RuntimeException("Failed to decode length-prefixed packet (#" + count + " packet)", e);
            } else {
                throw e;
            }
        } finally {
            if (!(buf instanceof EmptyByteBuf) && !buf.release()) {
                if (ProxyServer.getProxy().getConfig().isDebug()) {
                    LOGGER.error("Leak detected: {}", buf, new Throwable());
                } else {
                    LOGGER.error("Leak detected (turn on debug to see more): {}", buf);
                }
            }
        }
    }

    private void handleBuf(ChannelHandlerContext ctx, ByteBuf buf) {
        buf.markReaderIndex();
        byte[] bytes = new byte[3];
        for (int i = 0; i < bytes.length; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }
            bytes[i] = buf.readByte();
            if (bytes[i] >= 0) {
                int length = Packet.readVarInt(bytes);
                if (buf.readableBytes() >= length) {
                    ByteBuf packetBuf = buf.readBytes(length);
                    try {
                        ctx.fireChannelRead(packetBuf);
                    } finally {
                        if (!(packetBuf instanceof EmptyByteBuf) && packetBuf.refCnt() > 0 && !packetBuf.release()) {
                            if (ProxyServer.getProxy().getConfig().isDebug()) {
                                LOGGER.error("Leak detected: {}", packetBuf, new Throwable());
                            } else {
                                LOGGER.error("Leak detected (turn on debug to see more): {}", packetBuf);
                            }

                        }
                    }
                    return;
                }
                buf.resetReaderIndex();
                return;
            }
        }
        if (Packet.DEBUG) {
            throw new BadPacketException("length wider than 21-bit");
        }
        throw INVALID_LENGTH_EXCEPTION;
    }
}
