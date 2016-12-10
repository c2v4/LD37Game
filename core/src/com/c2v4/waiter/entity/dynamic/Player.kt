package com.c2v4.waiter.entity.dynamic


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.helper.Direction
import com.c2v4.waiter.helper.Direction.*
import com.c2v4.waiter.helper.PPM
import com.c2v4.waiter.screen.GameScene

class Player{
    val SPEED = 2f
    val sprite = Sprite(Texture("entities/player.png"))
    val body:Body
    val gameScene:GameScene
    var direction:Direction = LEFT
    val moveListeners:Array<Mover>

    constructor( world: World,gameScene: GameScene) {
        this.gameScene = gameScene
        body = createBody(world)
        moveListeners = arrayOf(
                Mover(Input.Keys.UP, { body.applyForceToCenter(Vector2(0f, SPEED), true);direction=UP }),
                Mover(Input.Keys.DOWN, { body.applyForceToCenter(Vector2(0f, -SPEED), true);direction=DOWN }),
                Mover(Input.Keys.RIGHT, { body.applyForceToCenter(Vector2( SPEED,0f), true);direction=RIGHT }),
                Mover(Input.Keys.LEFT, { body.applyForceToCenter(Vector2( -SPEED,0f), true);direction=LEFT }),
                Mover(Input.Keys.Z, { gameScene.plant(this) })
        )

    }

    private fun createBody(world: World):Body {
        val fixtureDef = FixtureDef()
        val circleShape = CircleShape()
        circleShape.radius = 15f / PPM
        circleShape.position = Vector2(16f / PPM, 16f / PPM)
        fixtureDef.shape = circleShape
        fixtureDef.density = 1f
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.linearDamping = 4f
        bodyDef.fixedRotation = true
        val localBody = world.createBody(bodyDef)
        localBody.createFixture(fixtureDef)
        return localBody
    }

    fun update() {
        moveListeners.filter {
            Gdx.input.isKeyPressed(it.keyCode)
        }.forEach { it.action() }
        sprite.x=body.position.x* PPM
        sprite.y=body.position.y* PPM
    }
}
data class Mover(
        val keyCode:Int,
        val action: () -> Unit
)