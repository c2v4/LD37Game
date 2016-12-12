package com.c2v4.waiter.helper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

val marryTextures = listOf(
        Texture("entities/plant/mary/1.png"),
        Texture("entities/plant/mary/2.png"),
        Texture("entities/plant/mary/3.png"),
        Texture("entities/plant/mary/4.png"),
        Texture("entities/plant/mary/5.png"),
        Texture("entities/plant/mary/6.png")
)
val addPoliceSound = Gdx.audio.newSound(Gdx.files.internal("sounds/addpolice.wav"))
val harvestSound = Gdx.audio.newSound(Gdx.files.internal("sounds/harvest.wav"))
val mineSound = Gdx.audio.newSound(Gdx.files.internal("sounds/mine.wav"))
val placeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/place.wav"))
val plantSound = Gdx.audio.newSound(Gdx.files.internal("sounds/plant.wav"))
val ripSound = Gdx.audio.newSound(Gdx.files.internal("sounds/rip.wav"))
val shotSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shot.wav"))
val waterSound = Gdx.audio.newSound(Gdx.files.internal("sounds/water.wav"))
val winSound = Gdx.audio.newSound(Gdx.files.internal("sounds/win.wav"))
val buySound = Gdx.audio.newSound(Gdx.files.internal("sounds/buy.wav"))


val ripFontFile = Gdx.files.internal("ilits.ttf")
val winFontFile = Gdx.files.internal("win.ttf")

val playerBaseTexture = Texture("entities/player/base/base.png")
val playerTopTexture = Texture("entities/player/base/top.png")
val policemanTopTexture = Texture("entities/policeman/policeTop.png")

val emptyTexture = Texture("entities/plant/empty.png")
val wateringCanTexture = Texture("items/watering-can.png")
val marySeedTexture = Texture("items/mary-seed.png")
val selectorTexture = Texture("items/selector.png")
val harvesterTexture = Texture("items/harvester.png")
val maryTexture = Texture("items/mary.png")
val pickaxeTexture = Texture("items/pickaxe.png")
val soilTexture = Texture("items/soil.png")
val lampTexture = Texture("items/lamp.png")
val bulletTexture = Texture("items/bullet.png")
val gunTexture = Texture("items/gun.png")


fun getBaseFrames(): com.badlogic.gdx.utils.Array<out TextureRegion>? {
    val array = com.badlogic.gdx.utils.Array<TextureRegion>(30)
    val arrayOfTextureRegions = TextureRegion.split(playerBaseTexture, 32, 32)[0]
    arrayOfTextureRegions.forEach { textureRegion -> array.add(textureRegion) }
    return array
}

class AssetManager {
}