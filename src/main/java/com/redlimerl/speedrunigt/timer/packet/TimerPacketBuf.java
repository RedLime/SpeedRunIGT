package com.redlimerl.speedrunigt.timer.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

/**
 * This is for make easier support multiple Minecraft versions
 */
public class TimerPacketBuf {
    private final FriendlyByteBuf buf;

    public static TimerPacketBuf of(FriendlyByteBuf buf) {
        return new TimerPacketBuf(buf);
    }

    public static TimerPacketBuf create() {
        return TimerPacketBuf.of(new FriendlyByteBuf(Unpooled.buffer()));
    }

    private TimerPacketBuf(FriendlyByteBuf buf) {
        this.buf = buf;
    }

    public FriendlyByteBuf getBuffer() {
        return new FriendlyByteBuf(this.buf);
    }

    public TimerPacketBuf copy() {
        return new TimerPacketBuf(
                new FriendlyByteBuf(
                        this.buf.copy()
                )
        );
    }

    public void writeString(String string) {
        this.buf.writeUtf(string);
    }

    public String readString() {
        return this.buf.readUtf(Short.MAX_VALUE);
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
