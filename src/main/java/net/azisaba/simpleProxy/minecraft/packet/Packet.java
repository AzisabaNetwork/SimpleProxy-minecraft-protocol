package net.azisaba.simpleProxy.minecraft.packet;

import io.netty.buffer.ByteBuf;
import net.azisaba.simpleProxy.minecraft.connection.Connection;
import net.azisaba.simpleProxy.minecraft.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public abstract class Packet {
    public static final boolean DEBUG = Boolean.getBoolean("minecraft-protocol.debug-packet");
    public static final String CACHED_EXCEPTION_SUFFIX = "Launch proxy with -Dminecraft-protocol.debug-packet=true to see more.";
    private static final BadPacketException VARINT_NO_MORE_BYTES_EXCEPTION = new BadPacketException("No more bytes reading VarInt. " + CACHED_EXCEPTION_SUFFIX);
    private static final BadPacketException VARINT_OVERSIZED_EXCEPTION = new BadPacketException("VarInt too big. " + CACHED_EXCEPTION_SUFFIX);
    private static final BadPacketException STRING_TOO_LONG_EXCEPTION = new BadPacketException("String longer than allowed. " + CACHED_EXCEPTION_SUFFIX);
    private static final BadPacketException STRING_TOO_MANY_BYTES_EXCEPTION = new BadPacketException("String had more bytes than allowed. " + CACHED_EXCEPTION_SUFFIX);

    public void read(@NotNull ByteBuf buf) {
        throw new UnsupportedOperationException("Packet must implement read method");
    }

    public void read(@NotNull ByteBuf buf, @NotNull PacketFlow flow, int protocolVersion) {
        read(buf);
    }

    public void write(@NotNull ByteBuf buf) {
        throw new UnsupportedOperationException("Packet must implement write method");
    }

    public void write(@NotNull ByteBuf buf, @NotNull PacketFlow flow, int protocolVersion) {
        write(buf);
    }

    public abstract void handle(@NotNull Connection connection);

    /* Utility methods */

    @NotNull
    public static String readString(@NotNull ByteBuf buf) {
        return readString(buf, Short.MAX_VALUE);
    }

    @NotNull
    public static String readString(@NotNull ByteBuf buf, int maxLen) {
        int len = readVarInt(buf);
        if (len > maxLen * 4) {
            if (DEBUG) {
                throw new BadPacketException("String had more bytes than allowed (" + len + " > " + (maxLen * 4) + ")");
            }
            throw STRING_TOO_MANY_BYTES_EXCEPTION;
        }
        byte[] b = new byte[len];
        buf.readBytes(b);
        String s = new String(b, StandardCharsets.UTF_8);
        if (s.length() > maxLen) {
            if (DEBUG) {
                throw new BadPacketException("String longer than allowed (" + s.length() + " > " + maxLen + ")");
            }
            throw STRING_TOO_LONG_EXCEPTION;
        }
        return s;
    }

    public static void writeString(@NotNull ByteBuf buf, @NotNull String value) {
        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, b.length);
        buf.writeBytes(b);
    }

    public static void writeString(@NotNull ByteBuf buf, @NotNull String value, int maxLen) {
        if (value.length() > maxLen) {
            if (DEBUG) {
                throw new BadPacketException("String longer than allowed (" + value.length() + " > " + maxLen + ")");
            }
            throw STRING_TOO_LONG_EXCEPTION;
        }
        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, b.length);
        buf.writeBytes(b);
    }

    public static int getVarIntSize(int i) {
        for(int size = 1; size < 5; ++size) {
            if ((i & -1 << size * 7) == 0) {
                return size;
            }
        }
        return 5;
    }

    public static int readVarInt(@NotNull ByteBuf buf) {
        return readVarInt(buf, 5);
    }

    public static int readVarInt(@NotNull ByteBuf buf, int maxSize) {
        int result = 0;
        int bytes = 0;
        byte in;
        do {
            if (buf.readableBytes() == 0) {
                if (DEBUG) {
                    throw new BadPacketException("No more bytes reading VarInt");
                }
                throw VARINT_NO_MORE_BYTES_EXCEPTION;
            }
            in = buf.readByte();
            result |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > maxSize) {
                if (DEBUG) {
                    throw new BadPacketException("VarInt too big (" + bytes + " > " + maxSize + ")");
                }
                throw VARINT_OVERSIZED_EXCEPTION;
            }
        } while ((in & 0x80) == 0x80);
        return result;
    }

    public static int readVarInt(byte[] buf) {
        return readVarInt(buf, 5);
    }

    public static int readVarInt(byte[] buf, int maxSize) {
        int idx = 0;
        int result = 0;
        int bytes = 0;
        byte in;
        do {
            if (idx >= buf.length - 1) {
                if (DEBUG) {
                    throw new BadPacketException("No more bytes reading VarInt");
                }
                throw VARINT_NO_MORE_BYTES_EXCEPTION;
            }
            in = buf[idx++];
            result |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > maxSize) {
                if (DEBUG) {
                    throw new BadPacketException("VarInt too big (" + bytes + " > " + maxSize + ")");
                }
                throw VARINT_OVERSIZED_EXCEPTION;
            }
        } while ((in & 0x80) == 0x80);
        return result;
    }

    public static void writeVarInt(@NotNull ByteBuf buf, int value) {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            buf.writeByte(part);
        } while (value != 0);
    }
}
