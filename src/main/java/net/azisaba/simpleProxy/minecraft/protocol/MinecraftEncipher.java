package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;

public class MinecraftEncipher extends MessageToByteEncoder<ByteBuf> {
    private final CipherBase cipher;

    public MinecraftEncipher(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
        this.cipher.encipher(byteBuf, byteBuf2);
    }
}