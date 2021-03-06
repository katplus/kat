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
package plus.kat.spare;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.*;
import plus.kat.chain.*;
import plus.kat.crash.*;
import plus.kat.stream.*;

/**
 * @author kraity
 * @since 0.0.1
 */
public class IntegerSpare extends NumberSpare<Integer> {

    public static final IntegerSpare
        INSTANCE = new IntegerSpare();

    @NotNull
    @Override
    public Space getSpace() {
        return Space.$i;
    }

    @Override
    public boolean accept(
        @NotNull Class<?> klass
    ) {
        return klass == int.class
            || klass == Integer.class
            || klass == Number.class
            || klass == Object.class;
    }

    @NotNull
    @Override
    public Class<Integer> getType() {
        return int.class;
    }

    @NotNull
    @Override
    public Integer cast(
        @NotNull Supplier supplier,
        @Nullable Object data
    ) {
        if (data instanceof Integer) {
            return (Integer) data;
        }

        if (data instanceof Number) {
            return ((Number) data).intValue();
        }

        if (data instanceof Boolean) {
            return ((boolean) data) ? 1 : 0;
        }

        if (data instanceof CharSequence) {
            CharSequence num = (CharSequence) data;
            return Convert.toInt(
                num, num.length(), 10, 0
            );
        }

        return 0;
    }

    @NotNull
    @Override
    public Integer read(
        @NotNull Flag flag,
        @NotNull Alias alias
    ) {
        return alias.toInt();
    }

    @NotNull
    @Override
    public Integer read(
        @NotNull Flag flag,
        @NotNull Value value
    ) {
        return value.toInt();
    }

    @Override
    public void write(
        @NotNull Flow flow,
        @NotNull Object value
    ) throws IOCrash {
        flow.addInt(
            (int) value
        );
    }
}
