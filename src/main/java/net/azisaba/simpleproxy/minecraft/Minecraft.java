package net.azisaba.simpleproxy.minecraft;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.azisaba.simpleproxy.minecraft.network.connection.Connection;
import net.azisaba.simpleproxy.minecraft.util.EncryptionUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.KeyPair;
import java.util.Set;

public class Minecraft {
    private static final ObjectSet<Connection> connections = new ObjectArraySet<>();
    private static KeyPair keyPair;

    @NotNull
    public static KeyPair getKeyPair() {
        if (keyPair == null) keyPair = EncryptionUtils.createRsaKeyPair(1024);
        return keyPair;
    }

    public static void addConnection(@NotNull Connection connection) {
        connections.add(connection);
    }

    public static void unregisterConnection(@NotNull Connection connection) {
        connections.remove(connection);
    }

    @Contract(pure = true)
    @NotNull
    public static Set<Connection> getConnections() {
        return ObjectSets.unmodifiable(connections);
    }
}
