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
package plus.kat.chain;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.crash.*;
import plus.kat.kernel.*;
import plus.kat.stream.*;
import plus.kat.utils.*;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author kraity
 * @since 0.0.1
 */
public class Value extends Chain {
    /**
     * default
     */
    public Value() {
        super();
    }

    /**
     * @param size the initial capacity
     */
    public Value(
        int size
    ) {
        super(size);
    }

    /**
     * @param data the initial byte array
     */
    public Value(
        @NotNull byte[] data
    ) {
        super(data);
        count = data.length;
    }

    /**
     * @param data specify the {@link Chain} to be mirrored
     */
    public Value(
        @NotNull Chain data
    ) {
        super(data);
    }

    /**
     * @param bucket the specified {@link Bucket} to be used
     */
    public Value(
        @Nullable Bucket bucket
    ) {
        super(bucket);
    }

    /**
     * @param data specify the {@link CharSequence} to be mirrored
     */
    public Value(
        @Nullable CharSequence data
    ) {
        super();
        if (data != null) chain(
            data, 0, data.length()
        );
    }

    /**
     * Returns {@code true} if, and only if, internal {@code byte[]} can be shared
     *
     * @see Chain#getValue()
     * @since 0.0.2
     */
    @Override
    public boolean isShared() {
        return bucket == null;
    }

    /**
     * Returns a {@link Value} of this {@link Value}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     */
    @NotNull
    @Override
    public Value subSequence(
        int start, int end
    ) {
        return new Value(
            copyBytes(start, end)
        );
    }

    /**
     * @throws ArrayIndexOutOfBoundsException if the index argument is negative
     */
    public void set(
        int i, byte b
    ) {
        if (i < value.length) {
            hash = 0;
            value[i] = b;
        }
    }

    /**
     * @param b the specified byte value
     */
    public void add(
        byte b
    ) {
        chain(b);
    }

    /**
     * @param b the specified byte array
     */
    public void add(
        byte[] b
    ) {
        if (b != null) {
            chain(
                b, 0, b.length
            );
        }
    }

    /**
     * @param c the specified char value
     */
    public void add(
        char c
    ) {
        chain(c);
    }

    /**
     * @param c the specified char array
     */
    public void add(
        char[] c
    ) {
        if (c != null) {
            chain(
                c, 0, c.length
            );
        }
    }

    /**
     * @param c the specified char array
     */
    public void add(
        CharSequence c
    ) {
        if (c != null) {
            chain(
                c, 0, c.length()
            );
        }
    }

    /**
     * @param in the specified {@link InputStream}
     * @since 0.0.3
     */
    public void add(
        InputStream in
    ) {
        if (in != null) {
            chain(in);
        }
    }

    /**
     * @param i the specified index
     */
    public void slip(
        int i
    ) {
        if (i >= 0 &&
            i <= value.length) {
            hash = 0;
            count = i;
        }
    }

    /**
     * Parses this {@code UTF8} {@link Value} as a {@code char}
     */
    public char toChar() {
        return Convert.toChar(
            value, count, '?'
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code byte}
     */
    public byte toByte() {
        return (byte) Convert.toInt(
            value, count, 10, 0
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code short}
     */
    public short toShort() {
        return (short) Convert.toInt(
            value, count, 10, 0
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code int}
     */
    public int toInt() {
        return Convert.toInt(
            value, count, 10, 0
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code int}
     */
    public int toInt(
        int def
    ) {
        return Convert.toInt(
            value, count, 10, def
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code int}
     *
     * @param radix the radix to be used while parsing {@link Value}
     */
    public int toInt(
        int def, int radix
    ) {
        if (radix < 2 || radix > 36) {
            return def;
        }
        return Convert.toInt(
            value, count, radix, def
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code long}
     */
    public long toLong() {
        return Convert.toLong(
            value, count, 10L, 0L
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code long}
     */
    public long toLong(
        long def
    ) {
        return Convert.toLong(
            value, count, 10L, def
        );
    }

    /**
     * Parses this {@link Value} as a signed decimal {@code long}
     *
     * @param radix the radix to be used while parsing {@link Value}
     */
    public long toLong(
        long def, long radix
    ) {
        if (radix < 2L || radix > 36L) {
            return def;
        }
        return Convert.toLong(
            value, count, radix, def
        );
    }

    /**
     * Parses this {@link Value} as a {@code float}
     */
    public float toFloat() {
        return Convert.toFloat(
            value, count, 0F
        );
    }

    /**
     * Parses this {@link Value} as a {@code float}
     */
    public float toFloat(
        float def
    ) {
        return Convert.toFloat(
            value, count, def
        );
    }

    /**
     * Parses this {@link Value} as a {@code double}
     */
    public double toDouble() {
        return Convert.toDouble(
            value, count, 0D
        );
    }

    /**
     * Parses this {@link Value} as a {@code double}
     */
    public double toDouble(
        double def
    ) {
        return Convert.toDouble(
            value, count, def
        );
    }

    /**
     * Parses this {@link Value} as a {@code boolean}
     */
    public boolean toBoolean() {
        return Convert.toBoolean(
            value, count, false
        );
    }

    /**
     * Parses this {@link Value} as a {@code boolean}
     */
    public boolean toBoolean(
        boolean def
    ) {
        return Convert.toBoolean(
            value, count, def
        );
    }

    /**
     * Parses this {@link Value} as a {@link BigDecimal}
     */
    @NotNull
    public BigDecimal toBigDecimal() {
        int size = count;
        if (size != 0) {
            byte[] it = value;
            char[] ch = new char[size];
            while (--size != -1) {
                ch[size] = (char) (
                    it[size] & 0xFF
                );
            }
            try {
                return new BigDecimal(ch);
            } catch (Exception e) {
                // Nothing
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Parses this {@link Value} as a {@link BigInteger}
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public BigInteger toBigInteger() {
        int size = count;
        if (size != 0) {
            try {
                return new BigInteger(
                    new String(
                        value, 0, 0, size
                    )
                );
            } catch (Exception e) {
                // Nothing
            }
        }
        return BigInteger.ZERO;
    }

    /**
     * Returns a SecretKeySpec, please check {@link #length()}
     *
     * @throws IllegalArgumentException If the algo is null
     */
    @NotNull
    public SecretKeySpec asSecretKeySpec(
        @NotNull String algo
    ) {
        return new SecretKeySpec(
            value, 0, count, algo
        );
    }

    /**
     * Returns a SecretKeySpec, please check {@code offset}, {@code algo} and {@code length}
     *
     * @throws IllegalArgumentException       If the algo is null or the offset out of range
     * @throws ArrayIndexOutOfBoundsException If the length is negative
     */
    @NotNull
    public SecretKeySpec asSecretKeySpec(
        int offset, int length, @NotNull String algo
    ) {
        return new SecretKeySpec(
            value, offset, length, algo
        );
    }

    /**
     * Returns a IvParameterSpec, please check {@link #length()}
     */
    @NotNull
    public IvParameterSpec asIvParameterSpec() {
        return new IvParameterSpec(
            value, 0, count
        );
    }

    /**
     * Returns a IvParameterSpec, please check {@code offset} and {@code length}
     *
     * @throws IllegalArgumentException       If the offset out of range
     * @throws ArrayIndexOutOfBoundsException If the length is negative
     */
    @NotNull
    public IvParameterSpec asIvParameterSpec(
        int offset, int length
    ) {
        return new IvParameterSpec(
            value, offset, length
        );
    }

    /**
     * @param b the {@code byte} to be compared
     */
    public static boolean esc(byte b) {
        switch (b) {
            case '^':
            case '(':
            case ')': {
                return true;
            }
        }
        return false;
    }

    /**
     * @see Value#Value(Bucket)
     */
    public static Value apply() {
        return new Value(
            $Bucket.INS
        );
    }

    /**
     * @author kraity
     * @since 0.0.1
     */
    private static class $Bucket extends AtomicReferenceArray<byte[]> implements Bucket {

        private static final int SIZE, LIMIT, SCALE;

        static {
            SIZE = Config.get(
                "kat.value.size", 8
            );
            LIMIT = Config.get(
                "kat.value.limit", 16
            );

            if (LIMIT < SIZE) {
                throw new Error(
                    "Bucket's size(" + SIZE + ") cannot be greater than the limit(" + LIMIT + ")"
                );
            }

            SCALE = Config.get(
                "kat.value.scale", 1024
            );
        }

        private static final $Bucket
            INS = new $Bucket();

        private $Bucket() {
            super(SIZE);
        }

        @NotNull
        @Override
        public byte[] alloc(
            @NotNull byte[] it, int len, int min
        ) {
            byte[] data;
            int i = min / SCALE;

            if (i < SIZE) {
                data = getAndSet(i, null);
                if (data == null ||
                    data.length < min) {
                    data = new byte[(i + 1) * SCALE - 1];
                }
            } else {
                if (i < LIMIT) {
                    data = new byte[(i + 1) * SCALE - 1];
                } else {
                    throw new RunCrash(
                        "Unexpectedly, Exceeding range '" + LIMIT * SCALE + "' in value"
                    );
                }
            }

            if (it.length != 0) {
                System.arraycopy(
                    it, 0, data, 0, len
                );

                int k = it.length / SCALE;
                if (k < SIZE) {
                    set(k, it);
                }
            }

            return data;
        }

        @Override
        public void push(
            @NotNull byte[] it
        ) {
            int i = it.length / SCALE;
            if (i < SIZE) {
                set(i, it);
            }
        }

        @Nullable
        @Override
        public byte[] revert(
            @NotNull byte[] it
        ) {
            int i = it.length / SCALE;
            if (i == 0) {
                return it;
            }

            if (i < SIZE) {
                set(i, it);
            }

            return getAndSet(0, null);
        }
    }
}
