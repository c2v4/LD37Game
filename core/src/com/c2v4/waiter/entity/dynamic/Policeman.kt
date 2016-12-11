package com.c2v4.waiter.entity.dynamic

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.helper.*
import com.c2v4.waiter.screen.GameScene

class Policeman {
    val SPEED = 0.15f
    val startingX = 16
    val startingY = 3
    val gameScene: GameScene
    val baseAnimation: Animation
    val top: TextureRegion
    val body: Body
    val baseFrameLength = 0.015f
    var baseAnimationCounter = 0f
    var x = 0f
    var y = 0f
    var moved = false
    var aStar:List<Point2D> = listOf()
    var direction: Direction = Direction.UP

    constructor(world: World, gameScene: GameScene) {
        this.gameScene = gameScene
        baseAnimation = Animation(baseFrameLength, getBaseFrames())
        top = TextureRegion.split(policemanTopTexture, 32, 32)[0][0]
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
        x = body.position.x * PPM + 32 * startingX
        y = body.position.y * PPM + 32 * startingY + 32
        moved = false
        aStar = aStar()
        move()
        baseAnimation.animationDuration
        if (moved) {
            baseAnimationCounter += delta
            baseAnimationCounter %= baseAnimation.animationDuration
        } else {
            baseAnimationCounter = 6 * baseFrameLength
        }
    }

    private fun move(): Unit {
        if (aStar.isNotEmpty()){
//            val policeX = Math.round((x) / 32)
//            val policeY = Math.round((y) / 32)
            val xdiff = aStar[0].x*32-x
            val ydiff = aStar[0].y*32-y
            if(xdiff>0){
                body.applyLinearImpulse(Vector2(SPEED, 0f), Vector2(0f, 0f), true);direction = Direction.RIGHT;moved = true
            }else{
                if (xdiff<0){
                    body.applyLinearImpulse(Vector2(-SPEED, 0f), Vector2(0f, 0f), true);direction = Direction.LEFT;moved = true
                }
            }
            if(ydiff>0){
                body.applyLinearImpulse(Vector2(0f, SPEED), Vector2(0f, 0f), true);direction = Direction.UP; moved = true
            }else{
                if (ydiff<0){
                    body.applyLinearImpulse(Vector2(0f, -SPEED), Vector2(0f, 0f), true);direction = Direction.DOWN;moved = true
                }
            }
        }
    }

    fun draw(batch: SpriteBatch) {
        val rotate: Float
        if (moved) {
            val velocityFromLocalPoint = body.linearVelocity
            rotate = MathUtils.atan2(velocityFromLocalPoint.y, velocityFromLocalPoint.x) * MathUtils.radiansToDegrees
        } else {
            when (direction) {
                Direction.UP -> rotate = 270f
                Direction.RIGHT -> rotate = 0f
                Direction.DOWN -> rotate = 90f
                Direction.LEFT -> rotate = 180f
            }
        }
        val rotateTop = MathUtils.atan2(y - gameScene.player.y, x - gameScene.player.x) * MathUtils.radiansToDegrees
        batch.draw(baseAnimation.getKeyFrame(baseAnimationCounter), x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotate, true)
        batch.draw(top, x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotateTop + 180, true)
    }

    fun aStar(): List<Point2D> {
        val policeX = Math.round((x) / 32)
        val policeY = Math.round((y) / 32)
        val playerX = Math.round((gameScene.player.x) / 32)
        val playerY = Math.round((gameScene.player.y) / 32)
        val playerPoint = Point2D(playerX,playerY)
        val closedSet = mutableSetOf<Point2D>()
        val openSet = mutableMapOf<Point2D, MutableList<Point2D>>()
        openSet.put(Point2D(policeX, policeY), mutableListOf<Point2D>())
        while (openSet.isNotEmpty()) {
            for ((x1, y1) in openSet.keys.toSet()) {
                val point2D = Point2D(x1, y1)
                val currentPath: MutableList<Point2D> = openSet[point2D] ?: mutableListOf()
                if (openSet.containsKey(point2D)) {
                    openSet.remove(point2D)
                }
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i == 0 && j == 0) continue
                        val currentPoint = Point2D(x1 + i, y1 + j)
                        if (gameScene.bodies.keys.contains(currentPoint) or
                                closedSet.contains(currentPoint) or
                                openSet.contains(currentPoint)) continue
                        val list = currentPath.toMutableList()
                        list.add(currentPoint)
                        if(currentPoint==playerPoint){
                            return list
                        }
                        openSet.put(currentPoint, list)
                    }
                }
                closedSet.add(point2D)
            }
        }
        return listOf()
    }

}
