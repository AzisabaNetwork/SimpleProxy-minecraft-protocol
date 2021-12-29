package net.azisaba.simpleProxy.minecraft.protocol;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.azisaba.simpleProxy.minecraft.packet.Packet;
import net.azisaba.simpleProxy.minecraft.packet.handshaking.ServerboundHandshakePacket;
import net.azisaba.simpleProxy.minecraft.packet.login.ClientboundLoginDisconnectPacket;
import net.azisaba.simpleProxy.minecraft.packet.login.ServerboundLoginStartPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public enum Protocol {
    HANDSHAKE {
        {
            register(
                    PacketFlow.SERVERBOUND,
                    ServerboundHandshakePacket.class,
                    ServerboundHandshakePacket::new,
                    map(0, 0x00)
            );
        }
    },
    STATUS,
    LOGIN {
        {
            register(
                    PacketFlow.CLIENTBOUND,
                    ClientboundLoginDisconnectPacket.class,
                    ClientboundLoginDisconnectPacket::new,
                    map(0, 0x00)
            );
            register(
                    PacketFlow.SERVERBOUND,
                    ServerboundLoginStartPacket.class,
                    ServerboundLoginStartPacket::new,
                    map(0, 0x00)
            );
        }
    },
    PLAY,
    ;

    // Packet flow, protocol version, protocol data
    private final Map<PacketFlow, Map<Integer, ProtocolData>> packets = new Object2ObjectOpenHashMap<>();

    Protocol() {
        packets.put(PacketFlow.CLIENTBOUND, new Int2ObjectOpenHashMap<>());
        packets.put(PacketFlow.SERVERBOUND, new Int2ObjectOpenHashMap<>());
    }

    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    ProtocolMapping map(int pv, int id) {
        return new ProtocolMapping(pv, id);
    }

    <T extends Packet> void register(@NotNull PacketFlow flow,
                                     @NotNull Class<T> packetClass,
                                     @NotNull Supplier<T> packetConstructor,
                                     @NotNull ProtocolMapping@NotNull... mappings) {
        for (ProtocolMapping mapping : mappings) {
            ProtocolData protocolData = packets.get(flow).computeIfAbsent(mapping.pv, k -> new ProtocolData(mapping.pv));
            protocolData.packets.put(mapping.id, new PacketData<>(mapping.id, packetClass, packetConstructor));
            protocolData.packetIds.put(packetClass, mapping.id);
        }
    }

    @NotNull
    public ProtocolData getPackets(@NotNull PacketFlow flow, int protocolVersion) {
        Map<Integer, ProtocolData> map = packets.get(flow);
        if (map.containsKey(protocolVersion)) return map.get(protocolVersion);
        int currentPV = Integer.MIN_VALUE;
        for (int p : map.keySet()) {
            if (p <= protocolVersion && p >= currentPV) {
                currentPV = p;
            }
        }
        ProtocolData data = map.get(currentPV);
        Objects.requireNonNull(data, "No protocol data defined for " + protocolVersion);
        return data;
    }

    public static class ProtocolData {
        private final int protocolVersion;
        private final Map<Integer, PacketData<?>> packets = new Int2ObjectOpenHashMap<>();
        private final Map<Class<?>, Integer> packetIds = new Object2ObjectOpenHashMap<>();

        private ProtocolData(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public int getProtocolVersion() {
            return protocolVersion;
        }

        @NotNull
        public Map<Integer, PacketData<?>> getPackets() {
            return packets;
        }

        @Nullable
        public PacketData<?> getPacket(int id) {
            return packets.get(id);
        }

        public int getPacketId(@NotNull Class<?> clazz) {
            return packetIds.getOrDefault(clazz, -1);
        }
    }

    public static class PacketData<P extends Packet> {
        private final int packetId;
        private final Class<P> packetClass;
        private final Supplier<P> packetConstructor;

        private PacketData(int packetId,
                           @NotNull Class<P> packetClass,
                           @NotNull Supplier<P> packetConstructor) {
            this.packetId = packetId;
            this.packetClass = packetClass;
            this.packetConstructor = packetConstructor;
        }

        public int getPacketId() {
            return packetId;
        }

        @NotNull
        public Class<P> getPacketClass() {
            return packetClass;
        }

        @NotNull
        public Supplier<P> getPacketConstructor() {
            return packetConstructor;
        }
    }

    private static class ProtocolMapping {
        private final int pv;
        private final int id;

        private ProtocolMapping(int pv, int id) {
            this.pv = pv;
            this.id = id;
        }
    }
}
