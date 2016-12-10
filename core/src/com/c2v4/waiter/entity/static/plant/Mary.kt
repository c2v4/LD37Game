package com.c2v4.waiter.entity.static.plant

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.c2v4.waiter.helper.marryTextures

class Mary : Plant {

    var growthPhase = 0
    var currentGrowth = 0
    var currentSprite: Sprite? = Sprite(marryTextures[growthPhase])
    private val timeForPhase = (120 * 30)

    override fun fullGrown(): Boolean = growthPhase == marryTextures.size-1

    override fun getImage(): Texture =
            marryTextures[growthPhase]

    override fun getSprite(): Sprite? = currentSprite

    override fun water() {
        currentGrowth += 60 * 20
        changeGrowthPhase()
    }


    private fun changeGrowthPhase() {
        val i = currentGrowth / timeForPhase
        if (growthPhase != i) {
            growthPhase = i
            if(growthPhase< marryTextures.size){
                currentSprite = Sprite(marryTextures[growthPhase])
            }
        }
        if (growthPhase >= marryTextures.size) {
            growthPhase = marryTextures.size - 1
            currentGrowth = timeForPhase * marryTextures.size
        }
    }

    override fun grow(value: Int) {
        currentGrowth += value
        changeGrowthPhase()
    }
}

