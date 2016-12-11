package com.c2v4.waiter.entity.dynamic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.helper.*
import com.c2v4.waiter.screen.GameScene

class Policeman{
    val startingX = 16
    val startingY = 3
    val gameScene:GameScene
    val baseAnimation:Animation
    val top:TextureRegion
    val body:Body
    val baseFrameLength = 0.015f
    var baseAnimationCounter = 0f
    var x=0f
    var y=0f
    var moved = false
    var direction:Direction= Direction.UP

    constructor(world: World, gameScene: GameScene) {
        this.gameScene = gameScene
        baseAnimation = Animation(baseFrameLength, getBaseFrames())
        top = TextureRegion.split(policemanTopTexture,32,32)[0][0]
        body = createBody(world)
    }

    private fun createBody(world: World): Body {
        val fixtureDef = FixtureDef()
        val circleShape = CircleShape()
        circleShape.radius = 15f / PPM
        circleShape.position = Vector2(startingX * 32 / PPM + 16f / PPM, startingY * 32 / PPM + 48f / PPM)
        fixtureDef.shape = circleShape
        fixtureDef.density = 1f
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.linearDamping = 10f
        bodyDef.fixedRotation = true
        val localBody = world.createBody(bodyDef)
        localBody.createFixture(fixtureDef)
        return localBody
    }

    fun update(delta: Float) {
        body.setLinearVelocity(0f, 0f)
        moved = false
        baseAnimation.animationDuration
        if (moved) {
            baseAnimationCounter += delta
            baseAnimationCounter %= baseAnimation.animationDuration
        } else {
            baseAnimationCounter = 6 * baseFrameLength
        }
        x = body.position.x * PPM + 32 * startingX
        y = body.position.y * PPM + 32 * startingY + 32
    }

    fun draw(batch: SpriteBatch) {
        val rotate: Float
        if (moved) {
            val velocityFromLocalPoint = body.linearVelocity
            rotate = MathUtils.atan2(velocityFromLocalPoint.y, velocityFromLocalPoint.x) * MathUtils.radiansToDegrees
        } else {
            when(direction){
                Direction.UP ->rotate = 270f
                Direction.RIGHT ->rotate = 0f
                Direction.DOWN ->rotate = 90f
                Direction.LEFT ->rotate = 180f
            }
        }
        val rotateTop = MathUtils.atan2(y-gameScene.player.y,x-gameScene.player.x)* MathUtils.radiansToDegrees
        batch.draw(baseAnimation.getKeyFrame(baseAnimationCounter), x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotate, true)
        batch.draw(top, x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotateTop+180, true)
    }
}