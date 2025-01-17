/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.netty.standalone.packet.s2c;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketClientDisconnected extends Packet {
    public int id;

    public PacketClientDisconnected() {
        super("client_disconnected");
    }

    public PacketClientDisconnected(int id) {
        this();
        this.id = id;
    }

    @Override protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
    }

    @Override protected void read0(DataBuffer buffer) {
        id = buffer.readInt();
    }
}
