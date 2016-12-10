package com.c2v4.waiter

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.c2v4.waiter.screen.GameScene



val WIDTH = 1024f
val HEIGHT = 768f

class WaiterGame : Game() {
    override fun create() {
        setScreen(GameScene())
    }

    override fun render() {
        screen.render(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        screen.hide()
        screen.dispose()
    }
}

