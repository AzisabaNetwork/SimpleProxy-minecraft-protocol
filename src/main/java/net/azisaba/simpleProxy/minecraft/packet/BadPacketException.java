package net.azisaba.simpleProxy.minecraft.packet;

import org.jetbrains.annotations.Nullable;

public class BadPacketException extends RuntimeException {
    public BadPacketException(@Nullable String message) {
        super(message);
    }

    public BadPacketException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
