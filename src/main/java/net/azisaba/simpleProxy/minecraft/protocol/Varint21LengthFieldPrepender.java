package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.azisaba.simpleProxy.minecraft.network.Packet;

@ChannelHandler.Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
    private static final int MAX_BYTES = 3;
    public static final Varint21LengthFieldPrepender INSTANCE = new Varint21LengthFieldPrepender();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf in, ByteBuf out) {
        int i = in.readableBytes();
        int size = Packet.getVarIntSize(i);
        if (size > MAX_BYTES) {
            throw new IllegalArgumentException("unable to fit " + i + " into " + MAX_BYTES);
        } else {
            out.ensureWritable(size + i);
            Packet.writeVarInt(out, i);
            out.writeBytes(in, in.readerIndex(), i);
        }
    }
}
