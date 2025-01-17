/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.engine.gui.guis;

import de.dasbabypixel.annotations.Api;
import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.Gui;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.text.Component;

/**
 * The {@link MainScreenGui} when the {@link GameLauncher} is started
 *
 * @author DasBabyPixel
 */
@SuppressWarnings("NewApi")
public interface MainScreenGui extends Gui {

    interface Second extends Gui {

        class Simple extends ParentableAbstractGui implements MainScreenGui, Second {
            public Simple(GameLauncher launcher) {
                super(launcher);
            }

            @Override public void onOpen0() throws GameException {
                launcher().frame().fullscreen().value(true);

                ColorGui colorGui = launcher().guiManager().createGui(ColorGui.class);
                colorGui.xProperty().bind(xProperty());
                colorGui.yProperty().bind(yProperty());
                colorGui.widthProperty().bind(widthProperty());
                colorGui.heightProperty().bind(heightProperty());

                colorGui.color().set(1, 0.5F, 0F, 1);
                addGUI(colorGui);

                TextureGui textureGui = launcher().guiManager().createGui(TextureGui.class);
                textureGui.texture().uploadAsync(launcher().resourceLoader().resource(launcher().embedFileSystem().getPath("pixel64x64.png")).newResourceStream());
                textureGui.heightProperty().bind(heightProperty());
                textureGui.widthProperty().bind(heightProperty());
                textureGui.xProperty().bind(xProperty().add(widthProperty().subtract(textureGui.widthProperty()).divide(2D)));
                textureGui.yProperty().bind(yProperty());
                addGUI(textureGui);

                ScrollGui sg = launcher().guiManager().createGui(ScrollGui.class);

                ParentableAbstractGui g = new ParentableAbstractGui(launcher()) {
                    @Override public void onOpen0() throws GameException {
                        for (int i = 0; i < 50; i++) {
                            ButtonGui button = launcher().guiManager().createGui(ButtonGui.class);
                            ((ButtonGui.Simple.TextForeground) button.foreground().value()).textGui().text().value(Component.text("Number " + (i + 1)));
                            button.widthProperty().number(1000);
                            button.heightProperty().number(50);
                            button.xProperty().bind(xProperty());
                            button.yProperty().bind(yProperty().add(i * 55));
                            button.onButtonPressed(event -> launcher().guiManager().openGuiByClass(MainScreenGui.class));
                            addGUI(button);
                        }
                        width(1000);
                        height(50 * 55 - 5);
                    }
                };
                sg.gui().value(g);
                sg.widthProperty().bind(widthProperty());
                sg.heightProperty().bind(heightProperty().subtract(100));
                sg.xProperty().bind(xProperty());
                sg.yProperty().bind(yProperty().add(100));
                addGUI(sg);

                launcher().keyboardVisible(true);
            }

            @Override protected void onClose0() throws GameException {
                launcher().frame().fullscreen().value(false);
                launcher().keyboardVisible(false);
            }
        }
    }

    @Api
    class Simple extends ParentableAbstractGui implements MainScreenGui {

        public Simple(GameLauncher launcher) {
            super(launcher);
        }

        @Override public void onOpen0() throws GameException {

            ColorGui colorGui = launcher().guiManager().createGui(ColorGui.class);
            colorGui.xProperty().bind(xProperty());
            colorGui.yProperty().bind(yProperty());
            colorGui.widthProperty().bind(widthProperty());
            colorGui.heightProperty().bind(heightProperty());

            colorGui.color().set(1, 0.5F, 0F, 1);
            addGUI(colorGui);

            TextureGui textureGui = launcher().guiManager().createGui(TextureGui.class);
            textureGui.texture().uploadAsync(launcher().resourceLoader().resource(launcher().embedFileSystem().getPath("pixel64x64.png")).newResourceStream());
            textureGui.heightProperty().bind(heightProperty());
            textureGui.widthProperty().bind(heightProperty());
            textureGui.xProperty().bind(xProperty().add(widthProperty().subtract(textureGui.widthProperty()).divide(2D)));
            textureGui.yProperty().bind(yProperty());
            addGUI(textureGui);

            ButtonGui button = launcher().guiManager().createGui(ButtonGui.class);
            ((ButtonGui.Simple.TextForeground) button.foreground().value()).textGui().text().value(Component.text("pVAVG Map"));
            button.xProperty().bind(xProperty());
            button.yProperty().bind(yProperty().add(heightProperty().subtract(button.heightProperty()).divide(2)));
            button.widthProperty().bind(widthProperty());
            button.heightProperty().bind(heightProperty().divide(2));
            button.onButtonPressed(event -> launcher().guiManager().openGuiByClass(Second.class));
            addGUI(button);
        }
    }
}
