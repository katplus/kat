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
package plus.kat.reflex;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import plus.kat.*;
import plus.kat.anno.*;
import plus.kat.chain.*;
import plus.kat.crash.*;
import plus.kat.entity.*;
import plus.kat.utils.Casting;
import plus.kat.utils.Config;
import plus.kat.utils.KatMap;

/**
 * @author kraity
 * @since 0.0.1
 */
public class ReflexWorker<E> extends KatMap<Object, Setter<E, ?>> implements Worker<E> {

    private final Class<E> klass;
    private final CharSequence space;

    private Node<E> head, tail;
    private int flags;
    private final Constructor<E> builder;

    private static final boolean SPARE_POJO =
        Config.get("kat.spare.pojo", false);

    /**
     * @throws SecurityException     If the {@link Constructor#setAccessible(boolean)} is denied
     * @throws NoSuchMethodException If the klass has no parameterless constructor
     */
    public ReflexWorker(
        @NotNull Class<E> klass,
        @NotNull Supplier supplier
    ) throws NoSuchMethodException {
        this(klass, supplier, klass.getDeclaredConstructor());
    }

    /**
     * @throws SecurityException If the {@link Constructor#setAccessible(boolean)} is denied
     */
    public ReflexWorker(
        @NotNull Class<E> klass,
        @NotNull Supplier supplier,
        @NotNull Constructor<E> constructor
    ) {
        this.klass = klass;
        builder = constructor;
        builder.setAccessible(true);

        Embed embed = klass.getAnnotation(Embed.class);
        if (embed != null) {
            flags = embed.claim();
        }

        register(klass.getDeclaredFields(), supplier);
        register(klass.getMethods(), supplier);
        space = supplier.register(embed, klass, this);
    }

    /**
     * @throws SecurityException     If the {@link Constructor#setAccessible(boolean)} is denied
     * @throws NoSuchMethodException If the klass has no parameterless constructor
     */
    public ReflexWorker(
        @Nullable Embed embed,
        @NotNull Class<E> klass,
        @NotNull Supplier supplier
    ) throws NoSuchMethodException {
        this.klass = klass;
        builder = klass.getDeclaredConstructor();
        builder.setAccessible(true);
        if (embed != null) {
            flags = embed.claim();
        }

        register(klass.getDeclaredFields(), supplier);
        register(klass.getMethods(), supplier);
        space = supplier.register(embed, klass, this);
    }

    @Override
    @Nullable
    public E apply(
        @NotNull Alias alias
    ) throws Crash {
        try {
            return builder.newInstance();
        } catch (Exception e) {
            throw new Crash(e);
        }
    }

    @Override
    @Nullable
    public Setter<E, ?> setter(
        @NotNull int index,
        @NotNull Alias alias
    ) {
        return get(
            alias.isEmpty() ? index : alias
        );
    }

    @NotNull
    @Override
    public CharSequence getSpace() {
        return space;
    }

    @Override
    public boolean accept(
        @NotNull Class<?> clazz
    ) {
        return clazz.isAssignableFrom(klass);
    }

    @Nullable
    @Override
    public Boolean getFlag() {
        return Boolean.TRUE;
    }

    @NotNull
    @Override
    public Class<E> getType() {
        return klass;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public E cast(
        @NotNull Supplier supplier,
        @Nullable Object data
    ) {
        if (data == null) {
            return null;
        }

        Class<?> clazz = data.getClass();
        if (klass.isAssignableFrom(clazz)) {
            return (E) data;
        }

        if (data instanceof Map) try {
            // source
            Map<?, ?> map = (Map<?, ?>) data;

            // create ins
            E entity = builder.newInstance();

            // foreach
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                // key
                Object key = entry.getKey();
                if (key == null) {
                    continue;
                }

                // try lookup
                Setter<E, ?> setter = get(key);
                if (setter == null) {
                    continue;
                }

                // get class specified
                Class<?> klass = setter.getKlass();

                // get spare specified
                Spare<?> spare = supplier.lookup(klass);
                if (spare == null) {
                    continue;
                }

                setter.onAccept(
                    entity, spare.cast(
                        supplier, entry.getValue()
                    )
                );
            }

            return entity;
        } catch (Exception e) {
            return null;
        }

        if (data instanceof CharSequence) {
            return Casting.cast(
                this, (CharSequence) data, supplier
            );
        }

        return null;
    }

    @Override
    public void write(
        @NotNull Chan chan,
        @NotNull Object value
    ) throws IOCrash {
        Node<E> node = head;
        while (node != null) {
            chan.set(
                node.key, node.getCoder(), node.onApply(value)
            );
            node = node.next;
        }
    }

    private void register(
        @NotNull Field[] fields,
        @NotNull Supplier supplier
    ) {
        boolean REF = (flags & Embed.INDEX) != 0;
        for (Field field : fields) {
            Expose expose = field.getAnnotation(Expose.class);
            if (expose == null) continue;

            field.setAccessible(true);
            ReflexField<E> reflex =
                new ReflexField<>(
                    field, expose, supplier
                );

            int h = reflex.getHash();
            // check use index
            if (REF && h > -1) put(
                h, reflex.clone()
            );

            String[] keys = expose.value();
            if (keys.length == 0) {
                String name = field.getName();
                if (expose.export()) {
                    // register getter
                    register(name, reflex);
                    // register setter
                    put(name, reflex.clone());
                } else put(
                    name, reflex
                );
            } else {
                // register only the first alias
                if (expose.export()) {
                    register(keys[0], reflex);
                }

                for (String alias : keys) {
                    // check empty
                    if (!alias.isEmpty()) put(
                        alias, reflex.clone()
                    );
                }
            }
        }
    }

    private void register(
        @NotNull Method[] methods,
        @NotNull Supplier supplier
    ) {
        boolean REF = (flags & Embed.INDEX) != 0;
        boolean POJO = (flags & Embed.POJO) != 0 || SPARE_POJO;

        for (Method method : methods) {
            int count = method.getParameterCount();
            if (count > 1) continue;

            ReflexMethod<E> reflex;
            Expose expose = method.getAnnotation(Expose.class);

            // via Expose
            if (expose != null) {
                // have aliases
                String[] keys = expose.value();
                if (keys.length != 0) {
                    method.setAccessible(true);
                    reflex = new ReflexMethod<>(
                        method, expose, supplier
                    );

                    if (count != 0) {
                        int h = reflex.getHash();
                        // check use index
                        if (REF && h > -1) {
                            // register setter
                            put(h, reflex);
                        }

                        for (String alias : keys) {
                            // check empty
                            if (!alias.isEmpty()) put(
                                alias, reflex.clone()
                            );
                        }
                    } else {
                        // register all aliases
                        for (int i = 0; i < keys.length; i++) {
                            register(
                                keys[i], i == 0 ? reflex : reflex.clone()
                            );
                        }
                    }
                    continue;
                }

                // empty alias and use index
                else if (REF && count == 1) {
                    int h = expose.index();
                    // check use index
                    if (h > -1) {
                        method.setAccessible(true);
                        reflex = new ReflexMethod<>(
                            method, expose, supplier
                        );

                        // register setter
                        put(h, reflex);
                        // use index only
                        continue;
                    }
                }

                // directly via POJO
            }

            // check if via POJO
            else if (!POJO) {
                continue;
            }

            String key = method.getName();
            int i = 0, l = key.length();
            if (l < 4) {
                continue;
            }

            char ch = key.charAt(i++);
            if (ch == 's') {
                if (count == 0 ||
                    key.charAt(i++) != 'e' ||
                    key.charAt(i++) != 't') {
                    continue;
                }
            } else if (ch == 'g') {
                if (count != 0 ||
                    key.charAt(i++) != 'e' ||
                    key.charAt(i++) != 't') {
                    continue;
                }
                if (l == 8 &&
                    key.charAt(i) == 'C' &&
                    key.charAt(i + 1) == 'l' &&
                    key.charAt(i + 2) == 'a' &&
                    key.charAt(i + 3) == 's' &&
                    key.charAt(i + 4) == 's') {
                    continue;
                }
            } else if (ch == 'i') {
                if (count != 0 ||
                    key.charAt(i++) != 's') {
                    continue;
                }
            } else {
                continue;
            }

            ch = key.charAt(i);
            if (ch < 'A' || 'Z' < ch) {
                continue;
            }

            byte[] k = new byte[l - i];
            k[0] = (byte) (ch + 0x20);

            for (int o = 1; ++i < l; ) {
                k[o++] = (byte) key.charAt(i);
            }

            Alias alias = new Alias(k);
            method.setAccessible(true);

            if (expose == null) {
                reflex = new ReflexMethod<>(method);
            } else {
                reflex = new ReflexMethod<>(
                    method, expose, supplier
                );
            }

            if (count != 0) {
                // register setter
                put(alias, reflex);
            } else {
                // register getter
                register(
                    alias, reflex
                );
            }
        }
    }

    private void register(
        @NotNull CharSequence key,
        @NotNull Node<E> node
    ) {
        node.key = key;
        int hash = node.getHash();
        if (tail == null) {
            head = node;
            tail = node;
        } else if (hash < 0) {
            tail.next = node;
            tail = node;
        } else {
            Node<E> m = head;
            Node<E> n = null;

            int wgt;
            while (true) {
                wgt = m.getHash();
                if (wgt < 0) {
                    node.next = m;
                    if (m == head) {
                        head = node;
                    }
                    break;
                }

                if (wgt > hash) {
                    if (n == null) {
                        head = node;
                    } else {
                        n.next = node;
                    }
                    node.next = m;
                    break;
                } else {
                    n = m;
                    m = m.next;
                    if (m == null) {
                        tail = node;
                        n.next = node;
                        break;
                    }
                }
            }
        }
    }

    /**
     * @author kraity
     * @since 0.0.2
     */
    static abstract class Node<E>
        extends Entry<E, Node<E>>
        implements Getter<E, Object> {

        CharSequence key;
        Node<E> next;

        public Node() {
            super(0);
        }

        public Node(
            int hash
        ) {
            super(hash);
        }
    }
}
