package net.azisaba.simpleProxy.minecraft.protocol;

public class PacketCompressionDecoder {
    private final int threshold;

    public PacketCompressionDecoder(int threshold) {
        this.threshold = threshold;
    }
}
