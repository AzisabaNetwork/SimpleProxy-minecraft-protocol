package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import net.azisaba.simpleProxy.minecraft.packet.BadPacketException;
import net.azisaba.simpleProxy.minecraft.packet.Packet;

import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder {
    private static final CorruptedFrameException INVALID_LENGTH_EXCEPTION = new CorruptedFrameException("length wider than 21-bit");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        buf.markReaderIndex();
        byte[] bytes = new byte[3];
        for(int i = 0; i < bytes.length; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }
            bytes[i] = buf.readByte();
            if (bytes[i] >= 0) {
                int length = Packet.readVarInt(bytes);
                if (buf.readableBytes() >= length) {
                    list.add(buf.readBytes(length));
                    return;
                }
                buf.resetReaderIndex();
                return;
            }
        }
        for (Object o : list) {
            ByteBuf byteBuf = (ByteBuf) o;
            byteBuf.release(byteBuf.refCnt());
        }
        list.clear();
        if (Packet.DEBUG) throw new BadPacketException("length wider than 21-bit");
        throw INVALID_LENGTH_EXCEPTION;
    }
}
