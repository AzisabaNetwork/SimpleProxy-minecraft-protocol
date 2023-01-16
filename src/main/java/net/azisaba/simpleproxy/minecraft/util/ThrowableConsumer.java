package net.azisaba.simpleproxy.minecraft.util;

public interface ThrowableConsumer<T> {
    void accept(T t) throws Exception;
}
