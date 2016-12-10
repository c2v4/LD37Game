package com.c2v4.waiter.entity.static.plant

import com.badlogic.gdx.graphics.Texture
import com.c2v4.waiter.helper.AssetManager
import com.c2v4.waiter.helper.marryTextures

class Mary : Plant {
    var growthPhase = 0

    override fun getImage(): Texture =
            marryTextures[growthPhase]

}

