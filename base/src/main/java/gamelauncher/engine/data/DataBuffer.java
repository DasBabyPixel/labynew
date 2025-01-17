/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.engine.data;

import com.google.common.base.Charsets;
import de.dasbabypixel.annotations.Api;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DataBuffer {
    private final @NotNull ByteMemory memory;
    private int readerIndex;
    private int writerIndex;

    public DataBuffer(@NotNull ByteMemory memory) {
        this.memory = memory;
    }

    /**
     * @return the reader index
     */
    @Api public int readerIndex() {
        return readerIndex;
    }

    /**
     * Sets the reader index
     *
     * @return the old reader index
     */
    @Api public int readerIndex(int readerIndex) {
        int old = this.readerIndex;
        this.readerIndex = readerIndex;
        return old;
    }

    /**
     * @return the writer index
     */
    @Api public int writerIndex() {
        return writerIndex;
    }

    /**
     * Sets the writer index
     *
     * @return the old writer index
     */
    @Api public int writerIndex(int writerIndex) {
        int old = this.writerIndex;
        this.writerIndex = writerIndex;
        return old;
    }

    /**
     * @return the number of readable bytes
     */
    @Api public int readableBytes() {
        return this.writerIndex - this.readerIndex;
    }

    /**
     * @return the number of writable bytes
     */
    @Api public int writableBytes() {
        return this.memory.capacity() - this.writerIndex;
    }

    /**
     * Resets the reader and writer index
     */
    @Api public void clear() {
        this.readerIndex = 0;
        this.writerIndex = 0;
    }

    /**
     * Increases the {@code readerIndex} by {@code length}<br>
     * The same as {@link #increaseReaderIndex(int)}
     *
     * @return the old reader index
     */
    @Api public int skipBytes(int length) {
        return readerIndex(readerIndex() + length);
    }

    /**
     * Increases the {@code readerIndex} by {@code length}
     *
     * @return the old reader index
     */
    @Api public int increaseReaderIndex(int length) {
        return readerIndex(readerIndex() + length);
    }

    /**
     * Increases the {@code writerIndex} by {@code length}
     *
     * @return the old writer index
     */
    @Api public int increaseWriterIndex(int length) {
        return writerIndex(writerIndex() + length);
    }

    @Api public void writeByte(byte value) {
        memory.setByte(increaseWriterIndex(DataUtil.BYTES_BYTE), value);
    }

    @Api public void writeShort(short value) {
        memory.setShort(increaseWriterIndex(DataUtil.BYTES_SHORT), value);
    }

    @Api public <T extends DataSerializable> void writeList(@NotNull List<T> list) {
        writeInt(list.size());
        for (DataSerializable object : list) {
            write(object);
        }
    }

    @Api public <T extends DataSerializable> void readList(@NotNull List<T> list, @NotNull Supplier<T> instanceCreator) {
        list.clear();
        int size = readInt();
        for (int i = 0; i < size; i++) {
            T o = instanceCreator.get();
            read(o);
            list.add(o);
        }
    }

    @Api public void writeInt(int value) {
        memory.setInt(increaseWriterIndex(DataUtil.BYTES_INT), value);
    }

    @Api public void writeLong(long value) {
        memory.setLong(increaseWriterIndex(DataUtil.BYTES_LONG), value);
    }

    @Api public void writeFloat(float value) {
        memory.setFloat(increaseWriterIndex(DataUtil.BYTES_FLOAT), value);
    }

    @Api public void writeDouble(double value) {
        memory.setDouble(increaseWriterIndex(DataUtil.BYTES_DOUBLE), value);
    }

    /**
     * Writes the given byte array to this {@link DataBuffer}
     */
    @Api public void writeBytes(byte @NotNull [] value) {
        writeInt(value.length);
        writeBytes(value, 0, value.length);
    }

    /**
     * Writes the given byte array to this {@link DataBuffer} from offset
     * {@code offset} with length {@code length}
     */
    @Api public void writeBytes(byte @NotNull [] value, int offset, int length) {
        memory.setBytes(increaseWriterIndex(length), value, offset, length);
    }

    /**
     * Writes a string to this {@link DataBuffer}
     */
    @Api public void writeString(@NotNull String string) {
        byte[] data = string.getBytes(Charsets.UTF_8);
        writeBytes(data);
    }

    /**
     * Writes a {@link DataSerializable} at the current {@link #writerIndex()}
     */
    @Api public void write(@NotNull DataSerializable object) {
        object.write(this);
    }

    @Api public void writeNullable(@Nullable DataSerializable object) {
        writeNull(object);
        if (object != null) write(object);
    }

    @Api public <T extends DataSerializable> @Nullable T read(@NotNull Supplier<@NotNull T> instanceCreator) {
        T instance = instanceCreator.get();
        read(instance);
        return instance;
    }

    @Api public <T extends DataSerializable> @Nullable T readNullable(@NotNull Supplier<@NotNull T> instanceCreator) {
        if (readNull()) return null;
        T instance = instanceCreator.get();
        read(instance);
        return instance;
    }

    @Api public void writeNullableString(@Nullable String string) {
        writeNull(string);
        if (string != null) writeString(string);
    }

    @Api public @Nullable String readNullableString() {
        if (readNull()) return null;
        return readString();
    }

    /**
     * @return the byte at the current {@link #readerIndex()}
     */
    @Api public byte readByte() {
        return memory.getByte(increaseReaderIndex(DataUtil.BYTES_BYTE));
    }

    /**
     * @return the short at the current {@link #readerIndex()}
     */
    @Api public short readShort() {
        return memory.getShort(increaseReaderIndex(DataUtil.BYTES_SHORT));
    }

    /**
     * @return the int at the current {@link #readerIndex()}
     */
    @Api public int readInt() {
        return memory.getInt(increaseReaderIndex(DataUtil.BYTES_INT));
    }

    /**
     * @return the long at the current {@link #readerIndex()}
     */
    @Api public long readLong() {
        return memory.getLong(increaseReaderIndex(DataUtil.BYTES_LONG));
    }

    /**
     * @return the float at the current {@link #readerIndex()}
     */
    @Api public float readFloat() {
        return memory.getFloat(increaseReaderIndex(DataUtil.BYTES_FLOAT));
    }

    /**
     * @return the double at the current {@link #readerIndex()}
     */
    @Api public double readDouble() {
        return memory.getDouble(increaseReaderIndex(DataUtil.BYTES_DOUBLE));
    }

    /**
     * @return a read byte array from this {@link DataBuffer}
     */
    @Api public byte @NotNull [] readBytes() {
        int length = readInt();
        byte[] data = new byte[length];
        readBytes(data, 0, length);
        return data;
    }

    /**
     * Reads a string from this {@link DataBuffer}
     *
     * @return the read string
     */
    @Api public @NotNull String readString() {
        return new String(readBytes(), Charsets.UTF_8);
    }

    /**
     * Reads a {@link DataSerializable} from the current {@link #readerIndex()}
     */
    @Api public void read(@NotNull DataSerializable object) {
        object.read(this);
    }

    /**
     * Reads {@code length} bytes from this {@link DataBuffer} into the specified
     * byte array at position {@code offset}
     */
    @Api public void readBytes(byte @NotNull [] value, int offset, int length) {
        memory.getBytes(increaseReaderIndex(length), value, offset, length);
    }

    /**
     * @return the memory of this buffer
     */
    @Api public @NotNull ByteMemory memory() {
        return memory;
    }

    private void writeNull(@Nullable Object object) {
        writeByte((byte) (object == null ? 0 : 1));
    }

    private boolean readNull() {
        byte b = readByte();
        if (b == 0) return true;
        if (b != 1) throw new IllegalStateException("Wrong data at this position");
        return false;
    }
}
