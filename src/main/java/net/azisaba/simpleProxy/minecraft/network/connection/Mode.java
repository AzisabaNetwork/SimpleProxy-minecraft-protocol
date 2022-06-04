package net.azisaba.simpleProxy.minecraft.network.connection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Mode {
    VIRTUAL_HOSTS_ONLY, // virtual hosts only, and nothing else
    NORMAL, // without encryption
    FULL, // with encryption / authentication
    ;

    @Contract(value = "null -> null", pure = true)
    @Nullable
    public static Mode getMode(@Nullable String s) {
        if (s == null) {
            return null;
        }
        if (s.equalsIgnoreCase("minecraft-full")) {
            return FULL;
        }
        if (s.equalsIgnoreCase("minecraft")) {
            return NORMAL;
        }
        if (s.equalsIgnoreCase("minecraft-vhosts-only")) {
            return VIRTUAL_HOSTS_ONLY;
        }
        return null;
    }
}
