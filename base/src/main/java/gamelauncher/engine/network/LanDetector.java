/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.engine.network;

public interface LanDetector {

    interface ClientHandler {
        void handle(NetworkAddress address);
    }
}
