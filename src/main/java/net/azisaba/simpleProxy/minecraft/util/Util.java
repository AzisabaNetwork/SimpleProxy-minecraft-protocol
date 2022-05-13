package net.azisaba.simpleProxy.minecraft.util;

import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Queue;

public class Util {
    @NotNull
    @SuppressWarnings("unchecked")
    public static Queue<Object> getQueue(ChannelInboundHandlerAdapter messageForwarder) {
        try {
            Field f = messageForwarder.getClass().getDeclaredField("queue");
            f.setAccessible(true);
            return (Queue<Object>) f.get(messageForwarder);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
