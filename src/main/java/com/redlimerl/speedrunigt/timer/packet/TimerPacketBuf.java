package com.redlimerl.speedrunigt.timer.packet;

import net.minecraft.util.Identifier;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * This is for make easier support multiple Minecraft versions
 */
public class TimerPacketBuf {
    private final DataOutputStream buf;

    public static TimerPacketBuf of(ByteArrayOutputStream buf) {
        return new TimerPacketBuf(new DataOutputStream(buf));
    }

    public static TimerPacketBuf of(DataOutputStream buf) {
        return new TimerPacketBuf(buf);
    }

    public static TimerPacketBuf create() {
        return TimerPacketBuf.of(new DataOutputStream(new ByteArrayOutputStream()));
    }

    private TimerPacketBuf(DataOutputStream buf) {
        this.buf = buf;
    }

    public DataOutputStream getBuffer() {
        return new DataOutputStream(this.buf);
    }

    public TimerPacketBuf copy() {
        return new TimerPacketBuf(
                new DataOutputStream(
                        this.buf
                )
        );
    }

    public void writeString(String string) {
        this.buf.writeInt(string.length());
        this.buf.writeUTF(string);
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
        this.writeString(identifier.toString());
    }

    public Identifier readIdentifier() {
        return new Identifier(this.readString());
    }

    public void release() {
        this.buf.close();
    }
}
