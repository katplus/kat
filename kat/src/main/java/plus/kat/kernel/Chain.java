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
package plus.kat.kernel;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.nio.charset.Charset;

import plus.kat.crash.*;
import plus.kat.stream.*;

import static plus.kat.stream.Binary.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author kraity
 * @since 0.0.1
 */
public abstract class Chain implements CharSequence, Comparable<CharSequence> {

    protected int count;
    protected byte[] value;

    protected int hash;
    protected Bucket bucket;

    /**
     * empty bytes
     */
    public static final byte[]
        EMPTY_BYTES = {};

    /**
     * empty chars
     */
    public static final char[]
        EMPTY_CHARS = {};

    /**
     * default
     */
    public Chain() {
        value = EMPTY_BYTES;
    }

    /**
     * @param size the initial capacity
     */
    public Chain(
        int size
    ) {
        value = size > 0 ? new byte[size] : EMPTY_BYTES;
    }

    /**
     * @param data the initial byte array
     */
    public Chain(
        @Nullable byte[] data
    ) {
        value = data == null ? EMPTY_BYTES : data;
    }

    /**
     * @param data specify the {@link Chain} to be mirrored
     */
    public Chain(
        @Nullable Chain data
    ) {
        if (data == null) {
            value = EMPTY_BYTES;
        } else {
            value = data.copyBytes();
            count = value.length;
        }
    }

    /**
     * @param bucket the specified {@link Bucket} to be used
     */
    public Chain(
        @Nullable Bucket bucket
    ) {
        value = EMPTY_BYTES;
        this.bucket = bucket;
    }

    /**
     * Returns a hash code for this {@link Chain}
     * <p>
     * {@link Chain} is similar to {@link String#hashCode()} when {@code byte[]} is ascii codes
     *
     * @return a hash code value for this {@link Chain}
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        if (hash == 0 && count != 0) {
            byte[] v = value;
            int h = v[0], l = count;
            for (int i = 1; i < l; i++) {
                h = 31 * h + v[i];
            }
            hash = h;
        }
        return hash;
    }

    /**
     * Only supports ASCII code comparison
     * <p>
     * Compares a {@link Chain} or {@link CharSequence} to this {@link Chain} to determine if their contents are equal
     *
     * @param o the {@link Object} to compare this {@link Chain} against
     */
    @Override
    public boolean equals(
        @Nullable Object o
    ) {
        if (this == o) {
            return true;
        }

        if (o instanceof Chain) {
            Chain c = (Chain) o;
            int range = c.count;
            if (count == range) {
                byte[] it = value;
                byte[] dest = c.value;
                for (int i = 0; i < range; i++) {
                    if (it[i] != dest[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        if (o instanceof CharSequence) {
            CharSequence c = (CharSequence) o;
            int range = c.length();
            if (count == range) {
                char ch;
                byte[] it = value;
                for (int i = 0; i < range; i++) {
                    ch = (char) (it[i] & 0xFF);
                    if (ch != c.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param o the {@link CharSequence} to be compared
     * @see String#compareTo(String)
     */
    @Override
    public int compareTo(
        @NotNull CharSequence o
    ) {
        if (this == o) {
            return 0;
        }

        int len1, len2, diff,
            limit = Math.min(
                len1 = count,
                len2 = o.length()
            );

        byte[] it = value;
        for (int i = 0; i < limit; i++) {
            diff = (it[i] & 0xFF) - (
                o.charAt(i) & 0xFFFF
            );
            if (diff != 0) {
                return diff;
            }
        }

        return len1 - len2;
    }

    /**
     * Compares the internal {@code byte[]} and specified {@code byte}
     *
     * @param b the byte value to be compared
     */
    public boolean is(
        byte b
    ) {
        return count == 1 && value[0] == b;
    }

    /**
     * Compares the internal UTF-8 {@code byte[]} and specified {@code char}
     *
     * @param c the char value to be compared
     * @since 0.0.2 supports UTF-8
     */
    public boolean is(
        char c
    ) {
        int l = count;
        byte[] it = value;

        // U+0000 ~ U+007F
        if (c < 0x80) {
            if (l != 1) {
                return false;
            }

            return it[0] == (byte) c;
        }

        // U+0080 ~ U+07FF
        else if (c < 0x800) {
            if (l != 2) {
                return false;
            }

            return it[0] == (byte) ((c >> 6) | 0xC0)
                && it[1] == (byte) ((c & 0x3F) | 0x80);
        }

        // U+10000 ~ U+10FFFF
        // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
        else if (c >= 0xD800 && c <= 0xDFFF) {
            return false;
        }

        // U+0800 ~ U+FFFF
        else {
            if (l != 3) {
                return false;
            }

            return it[0] == (byte) ((c >> 12) | 0xE0)
                && it[1] == (byte) (((c >> 6) & 0x3F) | 0x80)
                && it[2] == (byte) ((c & 0x3F) | 0x80);
        }
    }

    /**
     * Compares specified index of internal {@code byte[]} and specified {@code byte}
     *
     * @param i the specified index
     * @param b the byte value to be compared
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative
     */
    public boolean is(
        int i, byte b
    ) {
        return i < count && value[i] == b;
    }

    /**
     * Compares specified index of internal UTF-8 {@code byte[]} and specified {@code char}
     *
     * @param i the specified index
     * @param c the byte value to be compared
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative
     * @since 0.0.2 supports UTF-8
     */
    public boolean is(
        int i, char c
    ) {
        int l = count;
        if (i >= l) {
            return false;
        }

        int o = 0;
        byte[] it = value;

        for (int k = 0; k < l; o++) {
            if (i == o) {
                // U+0000 ~ U+007F
                if (c < 0x80) {
                    return it[k] == (byte) c;
                }

                // U+0080 ~ U+07FF
                else if (c < 0x800) {
                    if (k + 2 > l) {
                        return false;
                    }

                    return it[k] == (byte) ((c >> 6) | 0xC0)
                        && it[k + 1] == (byte) ((c & 0x3F) | 0x80);
                }

                // U+10000 ~ U+10FFFF
                // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
                else if (c >= 0xD800 && c <= 0xDFFF) {
                    if (k + 2 >= l ||
                        c > 0xDBFF) {
                        return false;
                    }

                    byte b2 = it[k + 1];
                    byte b3 = it[k + 2];
                    return c == (char) (
                        ((0xD8 | (it[k] & 0x03)) << 8) |
                            ((((b2 - 0x10 >> 2)) & 0x0F) << 4) |
                            (((b2 & 0x03) << 2) | ((b3 >> 4) & 0x03))
                    );
                }

                // U+0800 ~ U+FFFF
                else {
                    if (k + 3 > l) {
                        return false;
                    }

                    return it[k] == (byte) ((c >> 12) | 0xE0)
                        && it[k + 1] == (byte) (((c >> 6) & 0x3F) | 0x80)
                        && it[k + 2] == (byte) ((c & 0x3F) | 0x80);
                }
            }

            // get byte
            byte b = it[k];

            // U+0000 ~ U+007F
            // 0xxxxxxx
            if (b >= 0) {
                k++;
            }

            // U+0080 ~ U+07FF
            // 110xxxxx 10xxxxxx
            else if ((b >> 5) == -2) {
                k += 2;
            }

            // U+0800 ~ U+FFFF
            // 1110xxxx 10xxxxxx 10xxxxxx
            else if ((b >> 4) == -2) {
                k += 3;
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            else if ((b >> 3) == -2) {
                if (i != ++o) {
                    k += 4;
                    continue;
                }

                if (k + 3 >= l ||
                    c < 0xDC00 ||
                    c > 0xDFFF) {
                    return false;
                }

                byte b3 = it[k + 2];
                byte b4 = it[k + 3];
                return c == (char) (
                    ((0xDC | ((b3 >> 2) & 0x03)) << 8) |
                        ((((b3 & 0x3) << 2) | ((b4 >> 4) & 0x03)) << 4) | (b4 & 0x0F)
                );
            }

            // beyond the current range
            else {
                return false;
            }
        }

        return false;
    }

    /**
     * Compares the internal {@code byte[]} and specified {@code byte[]}
     *
     * @param b the {@code byte[]} to compare this {@link Chain} against
     * @since 0.0.2
     */
    public boolean is(
        @Nullable byte[] b
    ) {
        if (b == null) {
            return false;
        }

        int range = b.length;
        if (count == range) {
            byte[] it = value;
            for (int i = 0; i < range; i++) {
                if (it[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Compares the internal UTF-8 {@code byte[]} and specified {@link CharSequence}
     *
     * @param ch the {@link CharSequence} to compare this {@link Chain} against
     * @since 0.0.2 supports UTF-8
     */
    public boolean is(
        @Nullable CharSequence ch
    ) {
        if (ch == null) {
            return false;
        }

        int l = count;
        int r = ch.length();

        int i = 0, j = 0;
        byte[] it = value;

        for (; i < l && j < r; j++) {
            // get char
            char c = ch.charAt(j);

            // U+0000 ~ U+007F
            if (c < 0x80) {
                if (it[i++] != (byte) c) {
                    return false;
                }
            }

            // U+0080 ~ U+07FF
            else if (c < 0x800) {
                if (i + 2 > l) {
                    return false;
                }

                if (it[i++] != (byte) ((c >> 6) | 0xC0) ||
                    it[i++] != (byte) ((c & 0x3F) | 0x80)) {
                    return false;
                }
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (c >= 0xD800 && c <= 0xDFFF) {
                if (i + 4 > l) {
                    return false;
                }

                if (++j >= r) {
                    return false;
                }

                char d = ch.charAt(j);
                if (d < 0xDC00 || d > 0xDFFF) {
                    return false;
                }

                int u = (c << 10) + d - 0x35F_DC00;
                if (it[i++] != (byte) ((u >> 18) | 0xF0) ||
                    it[i++] != (byte) (((u >> 12) & 0x3F) | 0x80) ||
                    it[i++] != (byte) (((u >> 6) & 0x3F) | 0x80) ||
                    it[i++] != (byte) ((u & 0x3F) | 0x80)) {
                    return false;
                }
            }

            // U+0800 ~ U+FFFF
            else {
                if (i + 3 > l) {
                    return false;
                }

                if (it[i++] != (byte) ((c >> 12) | 0xE0) ||
                    it[i++] != (byte) (((c >> 6) & 0x3F) | 0x80) ||
                    it[i++] != (byte) ((c & 0x3F) | 0x80)) {
                    return false;
                }
            }
        }

        return i == l && j == r;
    }

    /**
     * Unsafe method
     *
     * @param i the index of the byte value
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative or not less than the length of this
     */
    public byte at(int i) {
        return value[i];
    }

    /**
     * @param i the index of the byte value
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative
     */
    public byte get(int i) {
        return i < value.length ? value[i] : -1;
    }

    /**
     * @param i   index
     * @param def default
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative
     */
    public byte get(int i, byte def) {
        return i < value.length ? value[i] : def;
    }

    /**
     * @param i the index of the byte value
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative or out of range {@code count}
     */
    public byte byteAt(int i) {
        if (i < count) {
            return value[i];
        }

        throw new ArrayIndexOutOfBoundsException(
            "Index " + i + " out of bounds for length " + count
        );
    }

    /**
     * @param i the index of the char value
     * @throws ArrayIndexOutOfBoundsException if the {@code index} argument is negative or out of range {@code count}
     */
    @Override
    public char charAt(int i) {
        if (i < count) {
            return (char) (value[i] & 0xFF);
        }

        throw new ArrayIndexOutOfBoundsException(
            "Index " + i + " out of bounds for length " + count
        );
    }

    /**
     * Returns the length of this {@link Chain}
     *
     * @return the length of the sequence of characters represented by this object
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * Returns the length of internal byte array
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Returns {@code true} if, and only if, {@link #length()} is {@code 0}
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns {@code false} if and only if {@code value[]} has an element {@code byte > 32 or byte< 0}
     */
    public boolean isBlank() {
        int i = 0, l = count;
        byte[] it = value;
        while (i < l) {
            byte b = it[i++];
            // Ascii code > 32, other code < 0
            if (b > 32 || b < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if, and only if, {@link #length()} is {@code 0}
     */
    public boolean isNotEmpty() {
        return count != 0;
    }

    /**
     * Returns {@code true} if and only if {@code value[]} has an element {@code byte > 32 or byte< 0}
     */
    public boolean isNotBlank() {
        int i = 0, l = count;
        byte[] it = value;
        while (i < l) {
            byte b = it[i++];
            // Ascii code > 32, other code < 0
            if (b > 32 || b < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if, and only if, {@link #length()} is {@code 1}
     */
    public boolean isSole() {
        return count == 1;
    }

    /**
     * Returns {@code true} if, and only if, internal {@code byte[]} can be shared
     *
     * @see Chain#getValue()
     * @see Chain#copyBytes()
     * @since 0.0.2
     */
    public boolean isShared() {
        return false;
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the prefix
     * @see String#startsWith(String)
     */
    public boolean startWith(
        @NotNull CharSequence c
    ) {
        int l = c.length();
        if (count < l) {
            return false;
        }

        char ch;
        byte[] it = value;

        for (int i = 0; i < l; i++) {
            ch = (char) (
                it[i] & 0xFF
            );
            if (ch != c.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the suffix
     * @see String#endsWith(String)
     */
    public boolean endsWith(
        @NotNull CharSequence c
    ) {
        int l = c.length();
        int k = count - l;
        if (k < 0) {
            return false;
        }

        char ch;
        byte[] it = value;

        for (int i = 0; i < l; i++, k++) {
            ch = (char) (
                it[k] & 0xFF
            );
            if (ch != c.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the first element only if {@link #length()} is not {@code 0}, otherwise returns {@code -1}
     */
    public byte head() {
        if (count == 0) {
            return -1;
        }
        return value[0];
    }

    /**
     * Check the specified {@code data} comparison with the first element
     *
     * @param b data
     */
    public boolean head(
        byte b
    ) {
        if (count == 0) {
            return false;
        }
        return value[0] == b;
    }

    /**
     * Returns the last element only if {@link #length()} is not {@code 0}, otherwise returns {@code -1}
     */
    public byte tail() {
        int i = count - 1;
        if (i < 0) {
            return -1;
        }
        return value[i];
    }

    /**
     * Check the specified {@code data} comparison with the last element
     *
     * @param b data
     */
    public boolean tail(
        byte b
    ) {
        int i = count - 1;
        if (i < 0) {
            return false;
        }
        return value[i] == b;
    }

    /**
     * Returns the last element only if {@link #length()} is {@code 1}, otherwise returns {@code 0}
     */
    public byte sole() {
        if (count != 1) {
            return -1;
        }
        return value[0];
    }

    /**
     * @param b the specified byte value
     * @see String#indexOf(int)
     */
    public int indexOf(byte b) {
        int max = count;
        byte[] it = value;
        for (int o = 0; o < max; o++) {
            if (it[o] == b) {
                return o;
            }
        }
        return -1;
    }

    /**
     * @param b the specified int value
     * @see Chain#indexOf(byte)
     */
    public int indexOf(int b) {
        return indexOf((byte) b);
    }

    /**
     * @param b the specified byte value
     * @param o the index to start the search from
     * @see String#indexOf(int, int)
     */
    public int indexOf(
        byte b, int o
    ) {
        int max = count;
        if (o < 0) {
            o = 0;
        } else if (o >= max) {
            return -1;
        }

        byte[] it = value;
        for (; o < max; o++) {
            if (it[o] == b) {
                return o;
            }
        }
        return -1;
    }

    /**
     * @param b the specified int value
     * @param o the index to start the search from
     * @see Chain#indexOf(byte, int)
     */
    public int indexOf(
        int b, int o
    ) {
        return indexOf((byte) b, o);
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the specified {@link CharSequence}
     * @see String#indexOf(String)
     */
    public int indexOf(
        @NotNull CharSequence c
    ) {
        return indexOf(c, 0);
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the specified {@link CharSequence}
     * @param o the index from which to start the search
     * @throws ArrayIndexOutOfBoundsException if the offset argument is negative
     * @see String#indexOf(String, int)
     */
    public int indexOf(
        @NotNull CharSequence c, int o
    ) {
        int len = c.length();
        if (len == 0) {
            return 0;
        }
        if (count == 0 ||
            o >= count) {
            return -1;
        }

        int lim = count - len;
        if (lim < 0) {
            return -1;
        }

        char ch = c.charAt(0);
        if (ch > 0xFF) {
            return -1;
        }

        byte fir = (byte) ch;
        byte[] it = value;

        for (; o <= lim; o++) {
            if (it[o] != fir) {
                continue;
            }

            char ot;
            int o1 = o, o2 = 0;
            while (++o2 < len) {
                ot = (char) (it[++o1] & 0xFF);
                if (ot != c.charAt(o2)) break;
            }
            if (o2 == len) {
                return o;
            }
        }

        return -1;
    }

    /**
     * @param b the specified byte value
     * @see String#lastIndexOf(int)
     */
    public int lastIndexOf(byte b) {
        int o = count - 1;
        byte[] it = value;
        for (; o >= 0; o--) {
            if (it[o] == b) {
                return o;
            }
        }
        return -1;
    }

    /**
     * @param b the specified int value
     * @see Chain#lastIndexOf(byte)
     */
    public int lastIndexOf(int b) {
        return lastIndexOf((byte) b);
    }

    /**
     * @param b the specified byte value
     * @param o the index from which to start the search
     * @see String#lastIndexOf(int, int)
     */
    public int lastIndexOf(
        byte b, int o
    ) {
        if (o >= count) {
            o = count - 1;
        }
        byte[] it = value;
        for (; o >= 0; o--) {
            if (it[o] == b) {
                return o;
            }
        }
        return -1;
    }

    /**
     * @param b the specified int value
     * @param o the index from which to start the search
     * @see Chain#lastIndexOf(byte, int)
     */
    public int lastIndexOf(
        int b, int o
    ) {
        return lastIndexOf((byte) b, o);
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the specified {@link CharSequence}
     * @see String#lastIndexOf(String)
     */
    public int lastIndexOf(
        @NotNull CharSequence c
    ) {
        return lastIndexOf(c, count);
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the specified {@link CharSequence}
     * @param f the index from which to start the search
     * @see String#lastIndexOf(String, int)
     */
    public int lastIndexOf(
        @NotNull CharSequence c, int f
    ) {
        int len = c.length(),
            r = count - len;
        if (f > r) {
            f = r;
        }

        if (f < 0) {
            return -1;
        }
        if (len == 0) {
            return f;
        }

        char ch = c.charAt(0);
        if (ch > 0xFF) {
            return -1;
        }

        byte fir = (byte) ch;
        byte[] it = value;

        char ot;
        for (; f >= 0; --f) {
            if (it[f] != fir) {
                continue;
            }

            int o1 = f, o2 = 0;
            while (++o2 < len) {
                ot = (char) (
                    it[++o1] & 0xFF
                );
                if (ot != c.charAt(o2)) break;
            }
            if (o2 == len) {
                return f;
            }
        }

        return -1;
    }

    /**
     * @param b the byte value to search for
     * @see Chain#indexOf(byte)
     */
    public boolean contains(byte b) {
        return indexOf(b) != -1;
    }

    /**
     * @param b the int value to search for
     * @see Chain#indexOf(byte)
     */
    public boolean contains(int b) {
        return indexOf((byte) b) != -1;
    }

    /**
     * Only supports ASCII code comparison
     *
     * @param c the {@link CharSequence} to search for
     * @see Chain#indexOf(CharSequence)
     * @see String#contains(CharSequence)
     */
    public boolean contains(
        @NotNull CharSequence c
    ) {
        return indexOf(c, 0) != -1;
    }

    /**
     * Returns the internal {@code byte[]}
     *
     * @throws RunCrash If the internal value cannot be shared
     * @since 0.0.2
     */
    @NotNull
    public byte[] getValue() {
        if (isShared()) {
            return value;
        }

        throw new RunCrash(
            "Unexpectedly, the internal value cannot be shared"
        );
    }

    /**
     * @param index the start index
     * @param dst   the specified {@code dst}
     * @since 0.0.3
     */
    public int getBytes(
        int index, byte[] dst
    ) {
        int length = count - index;
        if (length <= 0) {
            return -1;
        }

        if (length > dst.length) {
            length = dst.length;
        }

        System.arraycopy(
            value, index, dst, 0, length
        );
        return length;
    }

    /**
     * @param index the start index
     * @param dst   the specified {@code dst}
     * @since 0.0.3
     */
    public int getBytes(
        int index, byte[] dst, int dstIndex, int length
    ) {
        int len = count - index;
        if (len <= 0) {
            return -1;
        }

        int cap = dst.length - dstIndex;
        if (cap <= 0) {
            return 0;
        }

        if (cap < length) {
            length = cap;
        }

        if (len < length) {
            length = len;
        }

        System.arraycopy(
            value, index, dst, dstIndex, length
        );
        return length;
    }

    /**
     * copy the internal UTF-8 {@code byte[]} to {@code char[]}
     *
     * @since 0.0.2 supports UTF-8
     */
    @NotNull
    public char[] copyChars() {
        if (count != 0) {
            return Strings.toChars(
                value, 0, count
            );
        }
        return EMPTY_CHARS;
    }

    /**
     * copy the internal UTF-8 {@code byte[]} to {@code char[]}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @throws IndexOutOfBoundsException If the start is negative or the end out of range
     * @since 0.0.2
     */
    @NotNull
    public char[] copyChars(
        int start, int end
    ) {
        int length = end - start;
        if (start < 0 || length < 0 || end >= count) {
            throw new IndexOutOfBoundsException(
                "Index start " + start + " < 0 or end >= start or end " + end + " >= " + count
            );
        }

        if (length != 0) {
            return Strings.toChars(
                value, start, end
            );
        }

        return EMPTY_CHARS;
    }

    /**
     * copy the internal UTF-8 {@code byte[]}
     */
    @NotNull
    public byte[] copyBytes() {
        if (count != 0) {
            byte[] copy = new byte[count];
            System.arraycopy(
                value, 0, copy, 0, count
            );
            return copy;
        }
        return EMPTY_BYTES;
    }

    /**
     * copy the internal UTF-8 {@code byte[]}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @throws IndexOutOfBoundsException If the start is negative or the end out of range
     */
    @NotNull
    public byte[] copyBytes(
        int start, int end
    ) {
        int length = end - start;
        if (start < 0 || length < 0 || end >= count) {
            throw new IndexOutOfBoundsException(
                "Index start " + start + " < 0 or end >= start or end " + end + " >= " + count
            );
        }

        if (length != 0) {
            byte[] copy = new byte[length];
            System.arraycopy(
                value, start, copy, 0, length
            );
            return copy;
        }

        return EMPTY_BYTES;
    }

    /**
     * Writes to the {@link OutputStream} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see OutputStream#write(byte[], int, int)
     * @since 0.0.2
     */
    public void update(
        @NotNull OutputStream s
    ) throws IOException {
        s.write(
            value, 0, count
        );
    }

    /**
     * Writes to the {@link OutputStream} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IndexOutOfBoundsException If the offset is negative or the length out of range
     * @see OutputStream#write(byte[], int, int)
     * @since 0.0.2
     */
    public void update(
        @NotNull OutputStream s, int o, int l
    ) throws IOException {
        s.write(
            value, o, l
        );
    }

    /**
     * Updates the {@link Mac} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see Mac#update(byte[], int, int)
     */
    public void update(
        @NotNull Mac m
    ) {
        m.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Mac} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Mac#update(byte[], int, int)
     */
    public void update(
        @NotNull Mac m, int o, int l
    ) {
        m.update(
            value, o, l
        );
    }

    /**
     * Updates the {@link Signature} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see Signature#update(byte[], int, int)
     */
    public void update(
        @NotNull Signature s
    ) throws SignatureException {
        s.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Signature} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Signature#update(byte[], int, int)
     */
    public void update(
        @NotNull Signature s, int o, int l
    ) throws SignatureException {
        s.update(
            value, o, l
        );
    }

    /**
     * Updates the {@link MessageDigest} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see MessageDigest#update(byte[], int, int)
     */
    public void update(
        @NotNull MessageDigest m
    ) {
        m.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link MessageDigest} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see MessageDigest#update(byte[], int, int)
     */
    public void update(
        @NotNull MessageDigest m, int o, int l
    ) {
        m.update(
            value, o, l
        );
    }

    /**
     * Updates the {@link Cipher} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see Cipher#update(byte[], int, int)
     */
    @Nullable
    public byte[] update(
        @NotNull Cipher c
    ) {
        return c.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Cipher} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Cipher#update(byte[], int, int)
     */
    @Nullable
    public byte[] update(
        @NotNull Cipher c, int o, int l
    ) {
        return c.update(
            value, o, l
        );
    }

    /**
     * Completes the {@link Cipher} using the internal {@code byte[]} of this {@link Chain}
     *
     * @see Cipher#doFinal(byte[], int, int)
     */
    @Nullable
    public byte[] doFinal(
        @NotNull Cipher c
    ) throws IllegalBlockSizeException, BadPaddingException {
        return c.doFinal(
            value, 0, count
        );
    }

    /**
     * Completes the {@link Cipher} using the internal {@code byte[]} of this {@link Chain}
     *
     * @param o the specified offset
     * @param l the specified length
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Cipher#doFinal(byte[], int, int)
     */
    @Nullable
    public byte[] doFinal(
        @NotNull Cipher c, int o, int l
    ) throws IllegalBlockSizeException, BadPaddingException {
        return c.doFinal(
            value, o, l
        );
    }

    /**
     * Returns a lowercase {@code MD5} of this {@link Chain}
     *
     * @throws IllegalStateException If unsupport the MD5
     */
    @NotNull
    public String digest() {
        try {
            return digest(
                "MD5", 0, count
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "Unexpectedly, MD5 unsupported", e
            );
        }
    }

    /**
     * Returns a lowercase message digest of this {@link Chain}
     *
     * @param algo the name of the algorithm requested
     * @throws NoSuchAlgorithmException If no implementation supports the specified algorithm
     * @see MessageDigest
     * @see Binary#toLower(byte[])
     * @see Chain#digest(String, int, int)
     */
    @NotNull
    public String digest(
        @NotNull String algo
    ) throws NoSuchAlgorithmException {
        return digest(
            algo, 0, count
        );
    }

    /**
     * Returns a lowercase message digest of this {@link Chain}
     *
     * @param algo the name of the algorithm requested
     * @param o    the specified offset
     * @param l    the specified length
     * @throws IllegalArgumentException If the length out of range
     * @throws NoSuchAlgorithmException If no implementation supports the specified algorithm
     * @see MessageDigest
     * @see Binary#toLower(byte[])
     */
    @NotNull
    public String digest(
        @NotNull String algo, int o, int l
    ) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest
            .getInstance(algo);

        md.update(
            value, o, l
        );

        return toLower(md.digest());
    }

    /**
     * Returns a {@code REC4648|Basic} encoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] toBase() {
        return Base64.REC4648.INS.encode(
            value, 0, count
        );
    }

    /**
     * Returns a {@code REC4648|Basic} encoded String of {@link Chain}
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toBase64() {
        byte[] d = toBase();
        return new String(
            d, 0, 0, d.length
        );
    }

    /**
     * Returns a {@code REC4648|Basic} decoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] fromBase() {
        return Base64.REC4648.INS.decode(
            value, 0, count
        );
    }

    /**
     * Returns a {@code RFC4648_SAFE|URL/Filename Safe} encoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] toSafe() {
        return Base64.RFC4648_SAFE.INS.encode(
            value, 0, count
        );
    }

    /**
     * Returns a {@code RFC4648_SAFE|URL/Filename Safe} encoded String of {@link Chain}
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toSafe64() {
        byte[] d = toSafe();
        return new String(
            d, 0, 0, d.length
        );
    }

    /**
     * Returns a {@code RFC4648_SAFE|URL/Filename Safe} decoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] fromSafe() {
        return Base64.RFC4648_SAFE.INS.decode(
            value, 0, count
        );
    }

    /**
     * Returns a {@code RFC2045|Mime} encoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] toMime() {
        return Base64.RFC2045.INS.encode(
            value, 0, count
        );
    }

    /**
     * Returns a {@code RFC2045|Mime} encoded String of {@link Chain}
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toMime64() {
        byte[] d = toMime();
        return new String(
            d, 0, 0, d.length
        );
    }

    /**
     * Returns a {@code RFC2045|Mime} decoded byte array of {@link Chain}
     */
    @NotNull
    public byte[] fromMime() {
        return Base64.RFC2045.INS.decode(
            value, 0, count
        );
    }

    /**
     * Returns a {@link Reader} of this {@link Chain}
     *
     * @see Reader
     */
    @NotNull
    public Reader reader() {
        return new Reader(
            this, 0, count
        );
    }

    /**
     * Returns a {@link Reader} of this {@link Chain}
     *
     * @throws IllegalStateException if the {@code index} argument is negative or the length out of range
     * @see Reader
     */
    @NotNull
    public Reader reader(
        int index, int length
    ) {
        if (index < 0) {
            throw new IllegalStateException(
                "The 'index' argument is negative"
            );
        }

        int offset = index + length;
        if (offset > count) {
            throw new IllegalStateException(
                "The 'length' argument is ouf of range"
            );
        }

        return new Reader(
            this, index, offset
        );
    }

    /**
     * Returns an ASCII {@link String} of this {@link Chain}
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String string() {
        if (count == 0) {
            return "";
        }
        return new String(
            value, 0, 0, count
        );
    }

    /**
     * Returns an ASCII {@link String} of this {@link Chain}
     *
     * @param b the beginning index, inclusive
     * @throws IndexOutOfBoundsException if the beginIndex is negative
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String string(int b) {
        int l = count - b;
        if (l <= 0) {
            return "";
        }
        return new String(
            value, 0, b, l
        );
    }

    /**
     * Returns an ASCII {@link String} of this {@link Chain}
     *
     * @param b the beginning index, inclusive
     * @param e the ending index, exclusive
     * @throws IndexOutOfBoundsException if the beginIndex is negative
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String string(int b, int e) {
        int l = e - b;
        if (l <= 0 || e > count) {
            return "";
        }
        return new String(
            value, 0, b, l
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Chain} as a {@link String}
     */
    @NotNull
    @Override
    public String toString() {
        if (count == 0) {
            return "";
        }

        return new String(
            value, 0, count, UTF_8
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Chain} as a {@link String}
     *
     * @param b the beginning index, inclusive
     * @param e the ending index, exclusive
     * @throws IndexOutOfBoundsException if the beginIndex is negative
     */
    @NotNull
    public String toString(
        int b, int e
    ) {
        int l = e - b;
        if (l <= 0 || e > count) {
            return "";
        }

        return new String(
            value, b, l, UTF_8
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Chain} as a {@link String}
     *
     * @param c charset
     */
    @NotNull
    public String toString(
        @NotNull Charset c
    ) {
        if (count == 0) {
            return "";
        }

        return new String(
            value, 0, count, c
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Chain} as a {@link String}
     *
     * @param c charset
     * @param b the beginning index, inclusive
     * @param e the ending index, exclusive
     * @throws IndexOutOfBoundsException if the beginIndex is negative
     */
    @NotNull
    public String toString(
        @NotNull Charset c, int b, int e
    ) {
        int l = e - b;
        if (l <= 0 || e > count) {
            return "";
        }

        return new String(
            value, b, l, c
        );
    }

    /**
     * @param c the specified {@link Chain}
     */
    protected void chain(
        @NotNull Chain c
    ) {
        if (c.count == 1) {
            grow(count + 1);
            hash = 0;
            value[count++] = c.value[0];
        }

        // multiple
        else if (c.count != 0) {
            grow(count + c.count);
            System.arraycopy(
                c.value, 0,
                value, count, c.count
            );
            hash = 0;
            count += c.count;
        }
    }

    /**
     * @param in the specified {@link InputStream}
     * @since 0.0.3
     */
    protected void chain(
        @NotNull InputStream in
    ) {
        try {
            chain(in, 128);
        } catch (Exception e) {
            // Nothing
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // Nothing
            }
        }
    }

    /**
     * @param in the specified {@link InputStream}
     * @throws IOException If an I/O error occurs
     * @since 0.0.3
     */
    protected void chain(
        @NotNull InputStream in, int range
    ) throws IOException {
        int cap, length;
        byte[] it = value;

        while (true) {
            cap = it.length - count;
            if (cap < range) {
                grow(count + range);
                it = value;
                cap = it.length - count;
            }

            length = in.read(
                it, count, cap
            );

            if (length == -1) {
                break;
            }
            count += length;
        }
    }

    /**
     * @param b the specified byte value
     */
    protected void chain(
        byte b
    ) {
        byte[] it = value;
        if (count != it.length) {
            hash = 0;
            it[count++] = b;
        } else {
            grow(count + 1);
            hash = 0;
            value[count++] = b;
        }
    }

    /**
     * @param b the specified byte array
     */
    protected void chain(
        @NotNull byte[] b, int i, int l
    ) {
        if (l != 0) {
            grow(count + l);
            System.arraycopy(
                b, i, value, count, l
            );
            hash = 0;
            count += l;
        }
    }

    /**
     * @param c the specified char value
     */
    protected void chain(
        char c
    ) {
        // U+0000 ~ U+007F
        if (c < 0x80) {
            grow(count + 1);
            hash = 0;
            value[count++] = (byte) c;
        }

        // U+0080 ~ U+07FF
        else if (c < 0x800) {
            grow(count + 2);
            hash = 0;
            value[count++] = (byte) ((c >> 6) | 0xC0);
            value[count++] = (byte) ((c & 0x3F) | 0x80);
        }

        // U+10000 ~ U+10FFFF
        // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
        else if (c >= 0xD800 && c <= 0xDFFF) {
            // crippled surrogate pair
            grow(count + 1);
            hash = 0;
            value[count++] = '?';
        }

        // U+0800 ~ U+FFFF
        else {
            grow(count + 3);
            hash = 0;
            value[count++] = (byte) ((c >> 12) | 0xE0);
            value[count++] = (byte) (((c >> 6) & 0x3F) | 0x80);
            value[count++] = (byte) ((c & 0x3F) | 0x80);
        }
    }

    /**
     * @param c the specified char array
     */
    protected void chain(
        @NotNull char[] c, int i, int l
    ) {
        int k = i + l;
        grow(count + l);

        while (i < k) {
            // get char
            char d = c[i++];

            // U+0000 ~ U+007F
            if (d < 0x80) {
                grow(count + 1);
                hash = 0;
                value[count++] = (byte) d;
            }

            // U+0080 ~ U+07FF
            else if (d < 0x800) {
                grow(count + 2);
                hash = 0;
                value[count++] = (byte) ((d >> 6) | 0xC0);
                value[count++] = (byte) ((d & 0x3F) | 0x80);
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (d >= 0xD800 && d <= 0xDFFF) {
                if (i >= k) {
                    grow(count + 1);
                    hash = 0;
                    value[count++] = '?';
                    break;
                }

                char f = c[i++];
                if (f < 0xDC00 || f > 0xDFFF) {
                    grow(count + 1);
                    hash = 0;
                    value[count++] = '?';
                    continue;
                }

                grow(count + 4);
                hash = 0;
                int u = (d << 10) + f - 0x35F_DC00;
                value[count++] = (byte) ((u >> 18) | 0xF0);
                value[count++] = (byte) (((u >> 12) & 0x3F) | 0x80);
                value[count++] = (byte) (((u >> 6) & 0x3F) | 0x80);
                value[count++] = (byte) ((u & 0x3F) | 0x80);
            }

            // U+0800 ~ U+FFFF
            else {
                grow(count + 3);
                hash = 0;
                value[count++] = (byte) ((d >> 12) | 0xE0);
                value[count++] = (byte) (((d >> 6) & 0x3F) | 0x80);
                value[count++] = (byte) ((d & 0x3F) | 0x80);
            }
        }
    }

    /**
     * @param c the specified char array
     */
    protected void chain(
        @NotNull CharSequence c, int i, int l
    ) {
        int k = i + l;
        grow(count + l);

        while (i < k) {
            // get char
            char d = c.charAt(i++);

            // U+0000 ~ U+007F
            if (d < 0x80) {
                grow(count + 1);
                hash = 0;
                value[count++] = (byte) d;
            }

            // U+0080 ~ U+07FF
            else if (d < 0x800) {
                grow(count + 2);
                hash = 0;
                value[count++] = (byte) ((d >> 6) | 0xC0);
                value[count++] = (byte) ((d & 0x3F) | 0x80);
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (d >= 0xD800 && d <= 0xDFFF) {
                if (i >= k) {
                    grow(count + 1);
                    hash = 0;
                    value[count++] = '?';
                    break;
                }

                char f = c.charAt(i++);
                if (f < 0xDC00 || f > 0xDFFF) {
                    grow(count + 1);
                    hash = 0;
                    value[count++] = '?';
                    continue;
                }

                grow(count + 4);
                hash = 0;
                int u = (d << 10) + f - 0x35F_DC00;
                value[count++] = (byte) ((u >> 18) | 0xF0);
                value[count++] = (byte) (((u >> 12) & 0x3F) | 0x80);
                value[count++] = (byte) (((u >> 6) & 0x3F) | 0x80);
                value[count++] = (byte) ((u & 0x3F) | 0x80);
            }

            // U+0800 ~ U+FFFF
            else {
                grow(count + 3);
                hash = 0;
                value[count++] = (byte) ((d >> 12) | 0xE0);
                value[count++] = (byte) (((d >> 6) & 0x3F) | 0x80);
                value[count++] = (byte) ((d & 0x3F) | 0x80);
            }
        }
    }

    /**
     * Unsafe method
     *
     * @param b the beginning index
     * @param e the ending index
     */
    protected void swop(
        int b, int e
    ) {
        byte v;
        hash = 0;

        while (b < e) {
            v = value[e];
            value[e--] = value[b];
            value[b++] = v;
        }
    }

    /**
     * @param min the specified minimum size
     */
    protected void grow(
        int min
    ) {
        if (min > value.length) {
            if (bucket == null) {
                int cap = value.length +
                    (value.length >> 1);
                if (cap < min) cap = min;

                byte[] result = new byte[cap];
                System.arraycopy(
                    value, 0, result, 0, count
                );
                value = result;
            } else {
                value = bucket.alloc(
                    value, count, min
                );
            }
        }
    }

    /**
     * clean this {@link Chain}
     */
    protected void clean() {
        hash = 0;
        count = 0;
    }

    /**
     * clear this {@link Chain}
     */
    protected void clear() {
        this.clean();
        if (bucket == null) {
            value = EMPTY_BYTES;
        } else {
            byte[] it = bucket.revert(value);
            value = it != null ? it : EMPTY_BYTES;
        }
    }

    /**
     * close this {@link Chain}
     */
    protected void close() {
        this.clean();
        Bucket bt = bucket;
        if (bt != null) {
            bucket = null;
            if (value.length != 0) {
                bt.push(value);
            }
        }
        value = EMPTY_BYTES;
    }

    /**
     * @author Kraity
     * @since 0.0.1
     */
    public static class Reader implements plus.kat.stream.Reader {

        private int i, l;
        private byte[] b;

        /**
         * @param c the specified {@link Chain}
         * @param i the start index of the {@link Chain}
         */
        private Reader(
            @NotNull Chain c, int i, int l
        ) {
            this.i = i;
            this.l = l;
            this.b = c.value;
        }

        /**
         * Read a byte and cursor switch to next
         *
         * @throws NullPointerException If this has been closed
         */
        public byte read() {
            return b[i++];
        }

        /**
         * Check {@link Reader} for readable bytes
         *
         * @throws NullPointerException If this has been closed
         */
        public boolean also() {
            return i < l;
        }

        /**
         * @throws IllegalStateException if the {@code offset} argument is negative
         */
        public void slip(
            int index
        ) {
            if (index < 0) {
                throw new IllegalStateException(
                    "The 'offset' argument is negative"
                );
            }
            this.i = index;
        }

        /**
         * close this {@link Reader}
         */
        public void close() {
            l = 0;
            b = null;
        }
    }
}
