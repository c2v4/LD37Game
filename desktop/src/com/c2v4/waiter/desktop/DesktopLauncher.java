package com.c2v4.waiter.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.c2v4.waiter.WaiterGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS=30;
        config.height = 768;
        config.width = 1024;
        config.resizable = false;
        config.useGL30 = true;
        config.title = "Mr Farmer";
        new LwjglApplication(new WaiterGame(), config);
    }
}
