package com.c2v4.waiter.entity.dynamic


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.helper.*
import com.c2v4.waiter.helper.Direction.*
import com.c2v4.waiter.screen.GameScene
import javafx.scene.Scene

class Player {
    val startingX=16
    val startingY=2
    val SPEED = 2f
    val sprite = Sprite(Texture("entities/player.png"))
    val body: Body
    val gameScene: GameScene
    var direction: Direction = LEFT
    val moveListeners: Array<Mover>
    val instantListeners: Array<Mover>
    val inventory: MutableList<Item>
    var currentItem = 0
    var showInventory = true
    var money = 0
    val maxInventoryItems = 10

    constructor(world: World, gameScene: GameScene) {
        this.gameScene = gameScene
        body = createBody(world)
        inventory = mutableListOf(
                Item(5, Items.MARY_SEED),
                Item(20, Items.WATERING_CAN),
                Item(5, Items.HARVESTER),
                Item(9999, Items.PICKAXE)
        )
        instantListeners = arrayOf(
                Mover(Input.Keys.RIGHT_BRACKET, {
                    currentItem = (currentItem + 1) % inventory.size
                }),
                Mover(Input.Keys.LEFT_BRACKET, {
                    currentItem--
                    if (currentItem < 0) currentItem = inventory.size - 1
                }),
                Mover(Input.Keys.X, { showInventory = !showInventory }),
                Mover(Input.Keys.P, { gameScene.showShop = !gameScene.showShop }),
                Mover(Input.Keys.Z, {
                    val item = inventory[currentItem]
                    if (item.type.action(gameScene)) {
                        item.quantity--
                        if (item.quantity<1){
                            inventory.remove(item)
                            if (currentItem>=inventory.size){
                                currentItem--
                            }
                        }
                    }
                }
                )
        )
        moveListeners = arrayOf(
                Mover(Input.Keys.UP, { body.applyForceToCenter(Vector2(0f, SPEED), true);direction = UP }),
                Mover(Input.Keys.DOWN, { body.applyForceToCenter(Vector2(0f, -SPEED), true);direction = DOWN }),
                Mover(Input.Keys.RIGHT, { body.applyForceToCenter(Vector2(SPEED, 0f), true);direction = RIGHT }),
                Mover(Input.Keys.LEFT, { body.applyForceToCenter(Vector2(-SPEED, 0f), true);direction = LEFT })
        )


    }

    private fun createBody(world: World): Body {
        val fixtureDef = FixtureDef()
        val circleShape = CircleShape()
        circleShape.radius = 15f / PPM
        circleShape.position = Vector2(startingX*32/PPM +16f / PPM,startingY*32/PPM + 48f / PPM)
        fixtureDef.shape = circleShape
        fixtureDef.density = 1f
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.linearDamping = 5f
        bodyDef.fixedRotation = true
        val localBody = world.createBody(bodyDef)
        localBody.createFixture(fixtureDef)
        return localBody
    }

    fun update() {
        moveListeners.filter {
            Gdx.input.isKeyPressed(it.keyCode)
        }.forEach { it.action() }
        instantListeners.filter {
            Gdx.input.isKeyJustPressed(it.keyCode)
        }.forEach { it.action() }
        sprite.x = body.position.x * PPM +32*startingX
        sprite.y = body.position.y * PPM +32*startingY+32
    }

    fun getActionPoint(): Point2D {
        var x = Math.round(sprite.x / 32)
        var y = Math.round(sprite.y / 32)
        when (direction) {
            UP -> y++
            DOWN -> y--
            LEFT -> x--
            RIGHT -> x++
        }
        return Point2D(x, y)
    }

    fun addItem(itemType: Items, quantity: Int) {
        val item = inventory.filter { it.type == itemType }.firstOrNull()
        if (item == null) {
            inventory.add(Item(quantity, itemType))
        } else {
            item.quantity += quantity
        }
    }
}

data class Mover(
        val keyCode: Int,
        val action: () -> Unit
)

data class Item(
        var quantity: Int = 0,
        val type: Items
)

enum class Items(val action: (GameScene) -> Boolean, val texture: Texture) {
    MARY_SEED({ scene -> scene.plant(scene.player) }, marySeedTexture),
    WATERING_CAN({ scene -> scene.water(scene.player) }, wateringCanTexture),
    HARVESTER({ scene -> scene.harvest(scene.player) }, harvesterTexture),
    MARY({ false }, maryTexture),
    PICKAXE({ scene -> scene.mine(scene.player)}, pickaxeTexture)
}