package net.azisaba.simpleproxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class CipherBase {
    private final Cipher cipher;
    private byte[] heapIn = new byte[0];
    private byte[] heapOut = new byte[0];

    public CipherBase(@NotNull Cipher cipher) {
        this.cipher = cipher;
    }

    private byte[] bufToByte(ByteBuf buf) {
        int i = buf.readableBytes();
        if (this.heapIn.length < i) this.heapIn = new byte[i];
        buf.readBytes(this.heapIn, 0, i);
        return this.heapIn;
    }

    protected ByteBuf decipher(ChannelHandlerContext ctx, ByteBuf buf) throws ShortBufferException {
        int i = buf.readableBytes();
        byte[] bytes = this.bufToByte(buf);
        ByteBuf out = ctx.alloc().heapBuffer(this.cipher.getOutputSize(i));
        out.writerIndex(this.cipher.update(bytes, 0, i, out.array(), out.arrayOffset()));
        return out;
    }

    protected void encipher(ByteBuf in, ByteBuf out) throws ShortBufferException {
        int i = in.readableBytes();
        byte[] bytes = this.bufToByte(in);
        int i2 = this.cipher.getOutputSize(i);
        if (this.heapOut.length < i2) {
            this.heapOut = new byte[i2];
        }

        out.writeBytes(this.heapOut, 0, this.cipher.update(bytes, 0, i, this.heapOut));
    }
}