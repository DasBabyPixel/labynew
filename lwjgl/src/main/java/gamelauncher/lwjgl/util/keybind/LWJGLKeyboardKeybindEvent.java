/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.lwjgl.util.keybind;

import gamelauncher.engine.util.keybind.Keybind;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;

/**
 * @author DasBabyPixel
 */
public class LWJGLKeyboardKeybindEvent extends AbstractKeybindEvent implements KeyboardKeybindEvent {

    private final Type type;

    public LWJGLKeyboardKeybindEvent(Keybind keybind, Type type) {
        super(keybind);
        this.type = type;
    }

    @Override public Type type() {
        return type;
    }

    public static class Character extends LWJGLKeyboardKeybindEvent implements CharacterKeybindEvent {

        private final char character;

        public Character(Keybind keybind, Type type, char character) {
            super(keybind, type);
            this.character = character;
        }

        @Override public char character() {
            return character;
        }

        public static class Special extends Character implements CharacterKeybindEvent.Special {

            public Special(Keybind keybind, Type type, char character) {
                super(keybind, type, character);
            }
        }
    }
}
