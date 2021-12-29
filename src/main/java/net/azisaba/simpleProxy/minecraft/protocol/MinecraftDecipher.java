package net.azisaba.simpleProxy.minecraft.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.util.List;

public class MinecraftDecipher extends MessageToMessageDecoder<ByteBuf> {
    private final CipherBase cipher;

    public MinecraftDecipher(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(this.cipher.decipher(channelHandlerContext, byteBuf));
    }
}