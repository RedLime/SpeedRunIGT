package com.redlimerl.speedrunigt.timer.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

/**
 * This is for make easier support multiple Minecraft versions
 */
public class TimerPacketBuf {
    private final PacketByteBuf buf;

    public static TimerPacketBuf of(PacketByteBuf buf) {
        return new TimerPacketBuf(buf);
    }

    public static TimerPacketBuf create() {
        return TimerPacketBuf.of(new PacketByteBuf(Unpooled.buffer()));
    }

    private TimerPacketBuf(PacketByteBuf buf) {
        this.buf = buf;
    }

    public PacketByteBuf getBuffer() {
        return new PacketByteBuf(this.buf);
    }

    public TimerPacketBuf copy() {
        return new TimerPacketBuf(
                new PacketByteBuf(
                        this.buf.copy()
                )
        );
    }

    public void writeString(String string) {
        this.buf.writeInt(string.length());
        this.buf.writeString(string);
    }

    public String readString() {
        int length = this.buf.readInt();
        return this.buf.readString(length);
    }

    public void writeLong(long l) {
        this.buf.writeLong(l);
    }

    public long readLong() {
        return this.buf.readLong();
    }

    public void writeInt(int l) {
        this.buf.writeInt(l);
    }

    public int readInt() {
        return this.buf.readInt();
    }

    public void writeBoolean(boolean b) {
        this.buf.writeBoolean(b);
    }

    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    public void writeIdentifier(Identifier identifier) {
        this.buf.writeIdentifier(identifier);
    }

    public Identifier readIdentifier() {
        return this.buf.readIdentifier();
    }

    public void release() {
        this.buf.release();
    }
}
