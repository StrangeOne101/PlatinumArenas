package com.strangeone101.platinumarenas.buffers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

public class SmartWriter {

    private ByteArrayDataOutput out = ByteStreams.newDataOutput();

    public void writeInt(int i) {
        out.writeInt(i);
    }

    public void writeShort(short s) {
        out.writeShort(s);
    }

    public void writeShort(int s) {
        out.writeShort(s);
    }

    public void writeUUID(UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public void writeByteArray(byte[] bytes) {
        out.writeInt(bytes.length);
        for (byte b : bytes) {
            out.writeByte(b);
        }
    }

    public void writeString(String s) {
        if (s == null) {
            writeByteArray(new byte[0]);
            return;
        }
        writeByteArray(s.getBytes());
    }

    public void writeDouble(double d) {
        out.writeDouble(d);
    }

    public void writeFloat(float f) {
        out.writeFloat(f);
    }

    public void writeFloat(double f) {
        out.writeFloat((float) f);
    }

    public void writeLong(long l) {
        out.writeLong(l);
    }

    public void writeByte(byte b) {
        out.writeByte(b);
    }

    public void writeByte(int b) {
        out.writeByte(b);
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
