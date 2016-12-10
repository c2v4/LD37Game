package com.c2v4.waiter.entity.static

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.c2v4.waiter.entity.static.plant.Plant
import com.c2v4.waiter.helper.emptyTexture

class Ground {
    var plant: Plant? = null
    var sprite:Sprite

    constructor( x: Int,  y: Int){
        sprite = Sprite(emptyTexture)
        sprite.x = x*32f
        sprite.y = y*32f
    }

    fun draw(batch: Batch) {
        sprite.texture = plant?.getImage()?: emptyTexture
        sprite.draw(batch)
    }
}