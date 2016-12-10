package com.c2v4.waiter.entity.static

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.c2v4.waiter.entity.static.plant.Plant
import com.c2v4.waiter.helper.emptyTexture

class Ground(val x: Int, val y: Int) {
    var plant: Plant? = null
    var sprite:Sprite

    fun draw(batch: Batch) {
        val toDraw = plant?.getSprite()?: sprite
        toDraw.x = x*32f
        toDraw.y = y*32f
        toDraw.draw(batch)
    }

    fun water() {
        plant?.water()
    }

    fun grow(value:Int) {
        plant?.grow(value)
    }

    init {
        sprite = Sprite(emptyTexture)
        sprite.x = x*32f
        sprite.y = y*32f
    }
}