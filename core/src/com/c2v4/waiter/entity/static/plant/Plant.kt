package com.c2v4.waiter.entity.static.plant

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite


interface Plant {
    fun getImage(): Texture
    fun water()
    fun grow(value: Int)
    fun getSprite(): Sprite?
    fun fullGrown(): Boolean
}