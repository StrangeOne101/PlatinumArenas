package com.strangeone101.platinumarenas.buffers;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Contains a ByteBuffer with some additional methods to make it easier to use

 */
public class SmartReader {

    private final ByteBuffer buffer;

    public SmartReader(byte[] bytes) {
        this.buffer = ByteBuffer.wrap(bytes);
        this.buffer.position(0);
    }

    public byte get() {
        if (buffer.remaining() < 1) { //So older arenas don't throw exceptions but just have blank data instead
            return 0;
        }
        return buffer.get();
    }

    public int getInt() {
        if (buffer.remaining() < 4) { //So older arenas don't throw exceptions but just have blank data instead
            return 0;
        }
        return buffer.getInt();
    }

    public long getLong() {
        if (buffer.remaining() < 8) { //So older arenas don't throw exceptions but just have blank data instead
            return 0;
        }
        return buffer.getLong();
    }

    public short getShort() {
        if (buffer.remaining() < 2) { //So older arenas don't throw exceptions but just have blank data instead
            return 0;
        }
        return buffer.getShort();
    }

    public double getDouble() {
        if (buffer.remaining() < 8) { //So older arenas don't throw exceptions but just have blank data instead
            return 0;
        }
        return buffer.getDouble();
    }

    public UUID getUUID() {
        if (buffer.remaining() < 16) { //So older arenas don't throw exceptions but just have blank data instead
            return new UUID(0, 0);
        }
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public byte[] getBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buffer.get();
        }
        return bytes;
    }

    public byte[] getByteArray() {
        int length = getInt();
        return getBytes(length);
    }

    public String getString() {
        return new String(getByteArray());
    }

    public byte[] array() {
        return buffer.array();
    }

    public int position() {
        return buffer.position();
    }

    public void position(int position) {
        buffer.position(position);
    }

    public int remaining() {
        return buffer.remaining();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public void flip() {
        buffer.flip();
    }

    public void clear() {
        buffer.clear();
    }

}
