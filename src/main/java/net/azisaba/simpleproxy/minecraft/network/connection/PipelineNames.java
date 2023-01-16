package net.azisaba.simpleproxy.minecraft.network.connection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class PipelineNames {
    private static final Set<String> VALUES = new ConcurrentSkipListSet<>();
    public static final String SPLITTER = register("splitter");
    public static final String DECODER = register("decoder");
    public static final String PACKET_HANDLER = register("handler");
    public static final String ENCODER = register("encoder");
    public static final String PREPENDER = register("prepender");
    public static final String DISCONNECT_LOGGER = register("disconnect_logger");
    public static final String DECRYPT = register("decrypt");
    public static final String ENCRYPT = register("encrypt");
    
    @Contract("_ -> param1")
    private static String register(@NotNull String name) {
        if (VALUES.contains(name)) {
            throw new IllegalStateException("Duplicate name: " + name);
        }
        VALUES.add(name);
        return name;
    }

    public static @NotNull String @NotNull [] values() {
        return VALUES.toArray(new String[0]);
    }
}
