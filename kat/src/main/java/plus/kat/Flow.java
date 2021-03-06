/*
 * Copyright 2022 Kat+ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plus.kat;

import plus.kat.anno.NotNull;

import plus.kat.crash.*;

/**
 * @author kraity
 * @since 0.0.1
 */
public interface Flow extends Flag, Appendable {
    /**
     * Returns to the {@link Job} of this flow
     *
     * @see Job
     * @since 0.0.2
     */
    @NotNull
    Job getJob();

    /**
     * add a byte value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addByte((byte) 32);
     *   flow.addByte((byte) 'k');
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addByte(
        byte b
    ) throws IOCrash;

    /**
     * add a {@code byte[]} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   byte[] data = new byte[]{32, 64, 128};
     *   flow.addBytes(data);
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addBytes(
        @NotNull byte[] data
    ) throws IOCrash;

    /**
     * add a {@code byte[]} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   byte[] data = new byte[]{32, 64, 128, 256, 512, 1024};
     *   flow.addBytes(data, 2, 3); // 128, 256, 512
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addBytes(
        @NotNull byte[] data, int offset, int length
    ) throws IOCrash;

    /**
     * add a short value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addShort((short) 32);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addShort(
        short num
    ) throws IOCrash;

    /**
     * add an int value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addInt(32);
     *   flow.addInt(64);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addInt(
        int num
    ) throws IOCrash;

    /**
     * add an int value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addInt(36, 1, 3); // 100
     *   flow.addInt(36, 2, 2); // 10
     *   flow.addInt(36, 3, 3); // 044
     *   flow.addInt(36, 4, 4); // 0024
     *   flow.addInt(-36, 1, 8); // 11011100
     *   flow.addInt(-36, 4, 12); // 0000ffffffdc
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addInt(
        int num, int shift
    ) throws IOCrash;

    /**
     * add an int value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addInt(36, 1); // 100100
     *   flow.addInt(36, 2); // 210
     *   flow.addInt(36, 3); // 44
     *   flow.addInt(36, 4); // 24
     *   flow.addInt(-36, 1); // 11111111111111111111111111011100
     *   flow.addInt(-36, 4); // ffffffdc
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     * @since 0.0.2
     */
    void addInt(
        int num, int shift, int length
    ) throws IOCrash;

    /**
     * add a long value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addLong(32L);
     *   flow.addLong(64L);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addLong(
        long num
    ) throws IOCrash;

    /**
     * add a long value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addLong(36L, 1); // 100100
     *   flow.addLong(36L, 2); // 210
     *   flow.addLong(36L, 3); // 44
     *   flow.addLong(36L, 4); // 24
     *   flow.addLong(-36L, 1); // 1111111111111111111111111111111111111111111111111111111111011100
     *   flow.addLong(-36L, 4); // ffffffffffffffdc
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addLong(
        long num, int shift
    ) throws IOCrash;

    /**
     * add a long value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addLong(36L, 1, 3); // 100
     *   flow.addLong(36L, 2, 2); // 10
     *   flow.addLong(36L, 3, 3); // 044
     *   flow.addLong(36L, 4, 4); // 0024
     *   flow.addLong(-36L, 1, 8); // 11011100
     *   flow.addLong(-36L, 4, 12); // 0000ffffffdc
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     * @since 0.0.2
     */
    void addLong(
        long num, int shift, int length
    ) throws IOCrash;

    /**
     * add a float value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addFloat(16F);
     *   flow.addFloat(32.64F);
     *   flow.addFloat(64.128F);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addFloat(
        float num
    ) throws IOCrash;

    /**
     * add a float value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addFloat(16F, true); // 0x42100000
     *   flow.addFloat(16F, false); // 42100000
     *
     *   flow.addFloat(16.32F, true); // 0x41828F5C
     *   flow.addFloat(16.32F, false); // 41828F5C
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addFloat(
        float num, boolean hint
    ) throws IOCrash;

    /**
     * add a double value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addDouble(16D);
     *   flow.addDouble(32.64D);
     *   flow.addDouble(64.128D);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addDouble(
        double num
    ) throws IOCrash;

    /**
     * add a double value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addDouble(16D, true); // 0x4030000000000000
     *   flow.addDouble(16D, false); // 4030000000000000
     *
     *   flow.addDouble(16.32D, true); // 0x403051EB851EB852
     *   flow.addDouble(16.32D, false); // 403051EB851EB852
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addDouble(
        double num, boolean hint
    ) throws IOCrash;

    /**
     * add a boolean value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addBoolean(true);
     *   flow.addBoolean(false);
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addBoolean(
        boolean b
    ) throws IOCrash;

    /**
     * add a char value to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addChar('k');
     *   flow.addChar('a');
     *   flow.addChar('t');
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void addChar(
        char c
    ) throws IOCrash;

    /**
     * add a {@code char[]} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   char[] data = new char[]{'k', 'a', 't'};
     *   flow.addChars(data);
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addChars(
        @NotNull char[] data
    ) throws IOCrash;

    /**
     * add a {@code byte[]} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   char[] data = new char[]{'k', 'a', 't', '.', 'p', 'l', 'u', 's'};
     *   flow.addChars(data, 1, 2); // 'a', 't'
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addChars(
        @NotNull char[] data, int offset, int length
    ) throws IOCrash;

    /**
     * add a {@link CharSequence} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addChars("kat.plus");
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addChars(
        @NotNull CharSequence data
    ) throws IOCrash;

    /**
     * add a {@link CharSequence} to this {@link Flow}
     *
     * <pre>{@code
     *   Flow flow = ...
     *   flow.addChars("kat.plus", 1, 2); // "at"
     * }</pre>
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void addChars(
        @NotNull CharSequence data, int offset, int length
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * <pre>{@code
     *   Flow flow = ...
     *
     *   // kat
     *   flow.emit((byte) '^'); // escape: ^^
     *   flow.emit((byte) 'k'); // not escaped
     *
     *   // json
     *   flow.emit((byte) '"'); // escape: \"
     *   flow.emit((byte) '\'); // escape: \\
     *   flow.emit((byte) 'k'); // not escaped
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void emit(
        byte b
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * <pre>{@code
     *   Flow flow = ...
     *
     *   // kat
     *   flow.emit('^'); // escape: ^^
     *   flow.emit('k'); // not escaped
     *
     *   // json
     *   flow.emit('"'); // escape: \"
     *   flow.emit('\'); // escape: \\
     *   flow.emit('k'); // not escaped
     * }</pre>
     *
     * @throws IOCrash If an I/O error occurs
     */
    void emit(
        char c
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void emit(
        @NotNull byte[] data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void emit(
        @NotNull byte[] data, int offset, int length
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void emit(
        @NotNull CharSequence data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow},
     * which will be escaped if it is a special character
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void emit(
        @NotNull CharSequence data, int offset, int length
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if it is non-ASCII
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void text(
        char data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if it is non-ASCII
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void text(
        @NotNull byte[] data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if it is non-ASCII
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void text(
        @NotNull byte[] data, int offset, int length
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if it is non-ASCII
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void text(
        @NotNull CharSequence data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if it is non-ASCII
     *
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     */
    void text(
        @NotNull CharSequence data, int offset, int length
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if flow uses {@link Flag#UNICODE}
     *
     * @return this {@link Flow}
     * @throws IOCrash If an I/O error occurs
     * @since 0.0.2
     */
    @Override
    Flow append(
        char c
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if flow uses {@link Flag#UNICODE}
     *
     * @return this {@link Flow}
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     * @since 0.0.2
     */
    @Override
    Flow append(
        CharSequence data
    ) throws IOCrash;

    /**
     * add a data to this {@link Flow} that will be escaped
     * if it is a special character, or will be escaped to Unicode if flow uses {@link Flag#UNICODE}
     *
     * @return this {@link Flow}
     * @throws IOCrash              If an I/O error occurs
     * @throws NullPointerException If the specified {@code data} is null
     * @since 0.0.2
     */
    @Override
    Flow append(
        CharSequence data, int start, int end
    ) throws IOCrash;
}
