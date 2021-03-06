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
package plus.kat.stream;

import plus.kat.anno.NotNull;

/**
 * @author kraity
 * @since 0.0.1
 */
public interface Convert {
    /**
     * Parses the {@code UTF8} {@code byte[]} as a {@code char}
     */
    static char toChar(
        @NotNull byte[] it, int len, char def
    ) {
        if (len == 0) {
            return def;
        }

        // 0xxxxxxx
        if (len == 1) {
            return (char) (it[0] & 0xFF);
        }

        // 110xxxxx 10xxxxxx
        if (len == 2) {
            return (char) (((it[0] & 0x1F) << 6) | (it[1] & 0x3F));
        }

        // 1110xxxx 10xxxxxx 10xxxxxx
        if (len == 3) {
            return (char) (((it[0] & 0xF) << 12) | ((it[1] & 0x3F) << 6) | (it[2] & 0x3F));
        }

        return def;
    }

    /**
     * Parses the {@code byte[]} as a signed decimal {@code int}
     *
     * @param rad the radix to be used while parsing {@code byte[]}
     */
    static int toInt(
        @NotNull byte[] it, int len, int rad, int def
    ) {
        if (len == 0) {
            return def;
        }

        if (len == 1) {
            int dig = it[0];
            if (dig < 58) {
                dig -= 48;
            } else if (dig < 91) {
                dig -= 55;
            } else {
                dig -= 87;
            }
            if (dig < rad
                && dig > -1) {
                return dig;
            }
            return def;
        }

        byte bit = 0;
        int lim = -Integer.MAX_VALUE;
        boolean neg = false;

        switch (it[0]) {
            case '-': {
                neg = true;
                bit = 1;
                lim = Integer.MIN_VALUE;
                break;
            }
            case '0': {
                switch (it[1]) {
                    case 'x': {
                        bit = 2;
                        rad = 16;
                        break;
                    }
                    case 'b': {
                        bit = 2;
                        rad = 2;
                        break;
                    }
                    default: {
                        bit = 1;
                        rad = 8;
                    }
                }
                while (bit < len &&
                    it[bit] == 48) bit++;
            }
        }

        int num = 0;
        int mul = lim / rad;

        while (bit < len) {
            int dig = it[bit++];
            if (dig < 58) {
                dig -= 48;
            } else if (dig < 91) {
                dig -= 55;
            } else {
                dig -= 87;
            }
            if (dig < 0 ||
                num < mul ||
                dig >= rad) {
                return def;
            }
            num *= rad;
            if (num < lim + dig) {
                return def;
            }
            num -= dig;
        }

        return neg ? num : -num;
    }

    /**
     * Parses the {@link CharSequence} as a signed decimal {@code int}
     *
     * @param rad the radix to be used while parsing {@link CharSequence}
     * @see #toInt(byte[], int, int, int)
     */
    static int toInt(
        @NotNull CharSequence it, int len, int rad, int def
    ) {
        if (len == 0) {
            return def;
        }

        if (len == 1) {
            int dig = it.charAt(0);
            if (dig < 58) {
                dig -= 48;
            } else if (dig < 91) {
                dig -= 55;
            } else {
                dig -= 87;
            }
            if (dig < rad
                && dig > -1) {
                return dig;
            }
            return def;
        }

        byte bit = 0;
        int lim = -Integer.MAX_VALUE;
        boolean neg = false;

        switch (it.charAt(0)) {
            case '-': {
                neg = true;
                bit = 1;
                lim = Integer.MIN_VALUE;
                break;
            }
            case '0': {
                switch (it.charAt(1)) {
                    case 'x': {
                        bit = 2;
                        rad = 16;
                        break;
                    }
                    case 'b': {
                        bit = 2;
                        rad = 2;
                        break;
                    }
                    default: {
                        bit = 1;
                        rad = 8;
                    }
                }
                while (bit < len &&
                    it.charAt(bit) == 48) bit++;
            }
        }

        int num = 0;
        int mul = lim / rad;

        while (bit < len) {
            int dig = it.charAt(bit++);
            if (dig < 58) {
                dig -= 48;
            } else if (dig < 91) {
                dig -= 55;
            } else {
                dig -= 87;
            }
            if (dig < 0 ||
                num < mul ||
                dig >= rad) {
                return def;
            }
            num *= rad;
            if (num < lim + dig) {
                return def;
            }
            num -= dig;
        }

        return neg ? num : -num;
    }

    /**
     * Parses the {@code byte[]} as a signed decimal {@code long}
     *
     * @param rad radix the radix to be used while parsing {@code byte[]}
     */
    static long toLong(
        @NotNull byte[] it, int len, long rad, long def
    ) {
        if (len == 0) {
            return def;
        }

        if (len == 1) {
            long dig = it[0];
            if (dig < 58L) {
                dig -= 48L;
            } else if (dig < 91L) {
                dig -= 55L;
            } else {
                dig -= 87L;
            }
            if (dig < rad
                && dig > -1L) {
                return dig;
            }
            return def;
        }

        byte bit = 0;
        long lim = -Long.MAX_VALUE;
        boolean neg = false;

        switch (it[0]) {
            case '-': {
                neg = true;
                bit = 1;
                lim = Long.MIN_VALUE;
                break;
            }
            case '0': {
                switch (it[1]) {
                    case 'x': {
                        bit = 2;
                        rad = 16L;
                        break;
                    }
                    case 'b': {
                        bit = 2;
                        rad = 2L;
                        break;
                    }
                    default: {
                        bit = 1;
                        rad = 8L;
                    }
                }
                while (bit < len &&
                    it[bit] == 48) bit++;
            }
        }

        long num = 0L;
        long mul = lim / rad;

        while (bit < len) {
            long dig = it[bit++];
            if (dig < 58L) {
                dig -= 48L;
            } else if (dig < 91L) {
                dig -= 55L;
            } else {
                dig -= 87L;
            }
            if (dig < 0L ||
                num < mul ||
                dig >= rad) {
                return def;
            }
            num *= rad;
            if (num < lim + dig) {
                return def;
            }
            num -= dig;
        }

        return neg ? num : -num;
    }

    /**
     * Parses the {@link CharSequence} as a signed decimal {@code long}
     *
     * @param rad radix the radix to be used while parsing {@link CharSequence}
     * @see #toLong(byte[], int, long, long)
     */
    static long toLong(
        @NotNull CharSequence it, int len, long rad, long def
    ) {
        if (len == 0) {
            return def;
        }

        if (len == 1) {
            long dig = it.charAt(0);
            if (dig < 58L) {
                dig -= 48L;
            } else if (dig < 91L) {
                dig -= 55L;
            } else {
                dig -= 87L;
            }
            if (dig < rad
                && dig > -1L) {
                return dig;
            }
            return def;
        }

        byte bit = 0;
        long lim = -Long.MAX_VALUE;
        boolean neg = false;

        switch (it.charAt(0)) {
            case '-': {
                neg = true;
                bit = 1;
                lim = Long.MIN_VALUE;
                break;
            }
            case '0': {
                switch (it.charAt(1)) {
                    case 'x': {
                        bit = 2;
                        rad = 16L;
                        break;
                    }
                    case 'b': {
                        bit = 2;
                        rad = 2L;
                        break;
                    }
                    default: {
                        bit = 1;
                        rad = 8L;
                    }
                }
                while (bit < len &&
                    it.charAt(bit) == 48) bit++;
            }
        }

        long num = 0L;
        long mul = lim / rad;

        while (bit < len) {
            long dig = it.charAt(bit++);
            if (dig < 58L) {
                dig -= 48L;
            } else if (dig < 91L) {
                dig -= 55L;
            } else {
                dig -= 87L;
            }
            if (dig < 0L ||
                num < mul ||
                dig >= rad) {
                return def;
            }
            num *= rad;
            if (num < lim + dig) {
                return def;
            }
            num -= dig;
        }

        return neg ? num : -num;
    }

    /**
     * Parses the {@code byte[]} as a {@code float}
     */
    @SuppressWarnings("deprecation")
    static float toFloat(
        @NotNull byte[] it, int len, float def
    ) {
        switch (len) {
            case 0: {
                return def;
            }
            case 1: {
                int dig = it[0];
                if (dig < 58) {
                    dig -= 48;
                } else if (dig < 91) {
                    dig -= 55;
                } else {
                    dig -= 87;
                }
                if (dig < 10 && dig > -1) {
                    return dig;
                }
                return def;
            }
            case 10: {
                if (it[0] == '0' &&
                    it[1] == 'x') {
                    int bits = it[2];
                    for (int i = 3; i < 10; i++) {
                        int dig = it[i];
                        if (dig < 58) {
                            dig -= 48;
                        } else if (dig < 91) {
                            dig -= 55;
                        } else {
                            dig -= 87;
                        }
                        if (dig < 0 || dig > 15) {
                            return def;
                        } else {
                            bits <<= 4;
                            bits |= dig;
                        }
                    }
                    return Float.intBitsToFloat(bits);
                }
            }
        }

        try {
            return Float.parseFloat(
                new String(
                    it, 0, 0, len
                )
            );
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Parses the {@code byte[]} as a {@code double}
     */
    @SuppressWarnings("deprecation")
    static double toDouble(
        @NotNull byte[] it, int len, double def
    ) {
        switch (len) {
            case 0: {
                return def;
            }
            case 1: {
                int dig = it[0];
                if (dig < 58) {
                    dig -= 48;
                } else if (dig < 91) {
                    dig -= 55;
                } else {
                    dig -= 87;
                }
                if (dig < 10 && dig > -1) {
                    return dig;
                }
                return def;
            }
            case 18: {
                if (it[0] == '0' &&
                    it[1] == 'x') {
                    long bits = it[2];
                    for (int i = 3; i < 18; i++) {
                        long dig = it[i];
                        if (dig < 58) {
                            dig -= 48;
                        } else if (dig < 91) {
                            dig -= 55;
                        } else {
                            dig -= 87;
                        }
                        if (dig < 0 || dig > 15) {
                            return def;
                        } else {
                            bits <<= 4;
                            bits |= dig;
                        }
                    }
                    return Double.longBitsToDouble(bits);
                }
            }
        }

        try {
            return Double.parseDouble(
                new String(
                    it, 0, 0, len
                )
            );
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Parses the {@code byte[]} as a {@code boolean}
     */
    static boolean toBoolean(
        @NotNull byte[] it, int len, boolean def
    ) {
        switch (len) {
            case 1: {
                return it[0] == '1';
            }
            case 4: {
                if (it[0] == 't') {
                    return it[1] == 'r'
                        && it[2] == 'u'
                        && it[3] == 'e';
                }

                if (it[0] == 'T') {
                    return it[1] == 'R'
                        && it[2] == 'U'
                        && it[3] == 'E';
                }
            }
        }

        return def;
    }

    /**
     * Parses the {@link CharSequence} as a {@code boolean}
     */
    static boolean toBoolean(
        @NotNull CharSequence it, int len, boolean def
    ) {
        switch (len) {
            case 1: {
                return it.charAt(0) == '1';
            }
            case 4: {
                if (it.charAt(0) == 't') {
                    return it.charAt(1) == 'r'
                        && it.charAt(2) == 'u'
                        && it.charAt(3) == 'e';
                }

                if (it.charAt(0) == 'T') {
                    return it.charAt(1) == 'R'
                        && it.charAt(2) == 'U'
                        && it.charAt(3) == 'E';
                }
            }
        }

        return def;
    }
}
