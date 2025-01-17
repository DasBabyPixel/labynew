/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.engine.network.packet;

import de.dasbabypixel.annotations.Api;
import gamelauncher.engine.util.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author DasBabyPixel
 */
public class PacketRegistry {
    private static final Logger logger = Logger.logger();
    private final Map<Class<? extends Packet>, Entry<? extends Packet>> entryMap = new ConcurrentHashMap<>();
    private final Map<Integer, Class<? extends Packet>> classById = new ConcurrentHashMap<>();

    /**
     * Registers a packet
     */
    public final <T extends Packet> void register(Class<T> clazz, Supplier<T> constructor) {
        Entry<T> entry = new Entry<>(constructor);
        entryMap.put(clazz, entry);
        int id = constructor.get().key().hashCode();
        classById.put(id, clazz);
        logger.debugf("Registeded packet: %s - %s", id, clazz);
    }

    /**
     * Unregisters a packet
     */
    @Api public final void unregister(Class<? extends Packet> clazz) throws PacketNotRegisteredException {
        if (!entryMap.containsKey(clazz)) {
            throw new PacketNotRegisteredException(clazz.getName());
        }
        classById.remove(entryMap.remove(clazz).constructor.get().key().hashCode());
    }

    /**
     * @return an empty packet instance of the specified type
     */
    public final <T extends Packet> T createPacket(Class<T> clazz) throws PacketNotRegisteredException {
        if (entryMap.containsKey(clazz)) {
            return clazz.cast(entryMap.get(clazz).constructor.get());
        }
        throw new PacketNotRegisteredException(clazz.getName());
    }

    /**
     * @return the packet type for the specified id
     */
    public final Class<? extends Packet> getPacketType(int id) throws PacketNotRegisteredException {
        if (classById.containsKey(id)) {
            return classById.get(id);
        }
        throw new PacketNotRegisteredException(Integer.toString(id));
    }

    public void ensureRegistered(Class<? extends Packet> clazz) throws PacketNotRegisteredException {
        if (!entryMap.containsKey(clazz)) throw new PacketNotRegisteredException(clazz.getName());
    }

    private static class Entry<T> {

        /**
         * The constructor of the packet
         */
        public final Supplier<T> constructor;

        public Entry(Supplier<T> constructor) {
            this.constructor = constructor;
        }
    }
}
