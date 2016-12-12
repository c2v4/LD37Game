package com.c2v4.waiter.entity.dynamic


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.entity.dynamic.Items.*
import com.c2v4.waiter.helper.*
import com.c2v4.waiter.helper.Direction.*
import com.c2v4.waiter.screen.GameScene
import javafx.scene.transform.Rotate

class Player {
    val startingX = 16
    val startingY = 2
    val SPEED = 0.25f
    val body: Body
    val gameScene: GameScene
    var direction: Direction = LEFT
    val moveListeners: Array<Mover>
    val instantListeners: Array<Mover>
    val inventory: MutableList<Item>
    var currentItem = 0
    var showInventory = true
    var money = 10f
    var moved = false
    val baseAnimation: Animation
    var baseAnimationCounter = 0f
    val top: TextureRegion
    var x = 0f
    var y = 0f
//    val maxInventoryItems = 10

    private val baseFrameLength = 0.015f

    constructor(world: World, gameScene: GameScene) {
        this.gameScene = gameScene
        baseAnimation = Animation(baseFrameLength, getBaseFrames())
        top = TextureRegion.split(playerTopTexture, 32, 32)[0][0]
        body = createBody(world)
        inventory = mutableListOf(
                Item(5, MARY_SEED),
                Item(20, WATERING_CAN),
                Item(5, HARVESTER)
        )
        instantListeners = arrayOf(
                Mover(Input.Keys.RIGHT_BRACKET, {
                    if (inventory.size > 0) {
                        currentItem = (currentItem + 1) % inventory.size
                    }
                }),
                Mover(Input.Keys.F8, {
                    gameScene.addPoliceman()
                }),
                Mover(Input.Keys.LEFT_BRACKET, {
                    currentItem--
                    if (currentItem < 0) currentItem = inventory.size - 1
                    if (currentItem < 0) currentItem = 0
                }),
                Mover(Input.Keys.W, {
                    if (gameScene.showShop) {
                        gameScene.shopPointer--
                        if (gameScene.shopPointer < 0) gameScene.shopPointer = Items.values().size - 1
                    }
                }),
                Mover(Input.Keys.S, {
                    if (gameScene.showShop) {
                        gameScene.shopPointer = (gameScene.shopPointer + 1) % Items.values().size
                    }
                }),
                Mover(Input.Keys.D, {
                    if (gameScene.showShop) {
                        val item = Items.values()[gameScene.shopPointer]
                        if (money >= item.getBuyPrice()) {
                            addItem(item, 1)
                            buySound.play()
                            money -= item.getBuyPrice()
                        }
                    }
                }),
                Mover(Input.Keys.A, {
                    if (gameScene.showShop) {
                        val item = Items.values()[gameScene.shopPointer]
                        val itemInInventory = inventory.filter { it.type == item }.firstOrNull()
                        if (itemInInventory != null) {
                            removeItemFromInventory(itemInInventory)
                            money += item.getSellPrice()
                            buySound.play()
                        }
                    }
                }),
                Mover(Input.Keys.X, { showInventory = !showInventory }),
                Mover(Input.Keys.P, { gameScene.showShop = !gameScene.showShop }),
                Mover(Input.Keys.Z, {
                    val item = inventory[currentItem]
                    if (item.type.action(gameScene)) {
                        removeItemFromInventory(item)
                    }
                }),
                Mover(Input.Keys.F7, { money += 100 })
        )
        moveListeners = arrayOf(
                Mover(Input.Keys.UP, { body.applyLinearImpulse(Vector2(0f, SPEED), Vector2(0f, 0f), true);direction = UP; moved = true }),
                Mover(Input.Keys.DOWN, { body.applyLinearImpulse(Vector2(0f, -SPEED), Vector2(0f, 0f), true);direction = DOWN;moved = true }),
                Mover(Input.Keys.RIGHT, { body.applyLinearImpulse(Vector2(SPEED, 0f), Vector2(0f, 0f), true);direction = RIGHT;moved = true }),
                Mover(Input.Keys.LEFT, { body.applyLinearImpulse(Vector2(-SPEED, 0f), Vector2(0f, 0f), true);direction = LEFT;moved = true }),
                Mover(Input.Keys.Z, {
                    val item = inventory[currentItem]
                    if (arrayOf(MARY_SEED, HARVESTER).contains(item.type))
                        if (item.type.action(gameScene)) {
                            removeItemFromInventory(item)
                        }
                })
        )
    }

    fun removeItemFromInventory(item: Item) {
        item.quantity--
        if (item.quantity < 1) {
            inventory.remove(item)
            if (currentItem >= inventory.size) {
                currentItem--
                if (currentItem < 0) currentItem = 0
            }
        }
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
        localBody.userData = this
        return localBody
    }

    fun rip() {
        gameScene.showRip = true
    }

    fun update(delta: Float) {
        body.setLinearVelocity(0f, 0f)
        moved = false
        baseAnimation.animationDuration
        if (!gameScene.showRip && !gameScene.showWin) {
            moveListeners.filter {
                Gdx.input.isKeyPressed(it.keyCode)
            }.forEach { it.action() }
            instantListeners.filter {
                Gdx.input.isKeyJustPressed(it.keyCode)
            }.forEach { it.action() }
        }
        if (moved) {
            baseAnimationCounter += delta
            baseAnimationCounter %= baseAnimation.animationDuration
        } else {
            baseAnimationCounter = 6 * baseFrameLength
        }
        x = body.position.x * PPM + 32 * startingX
        y = body.position.y * PPM + 32 * startingY + 32
    }

    fun getPoint(): Point2D {
        var x = Math.round(x / 32)
        var y = Math.round(y / 32)
        return Point2D(x, y)
    }

    fun getActionPoint(): Point2D {
        var x = Math.round(x / 32)
        var y = Math.round(y / 32)
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

    fun draw(batch: SpriteBatch) {
        val rotate: Float
        if (moved) {
            val velocityFromLocalPoint = body.linearVelocity
            rotate = MathUtils.atan2(velocityFromLocalPoint.y, velocityFromLocalPoint.x) * MathUtils.radiansToDegrees
        } else {
            when (direction) {
                UP -> rotate = 270f
                RIGHT -> rotate = 0f
                DOWN -> rotate = 90f
                LEFT -> rotate = 180f
            }
        }
//        val rotateTop = MathUtils.atan2(y+16-(Gdx.graphics.height-Gdx.input.y),x+16-Gdx.input.x)*MathUtils.radiansToDegrees
        batch.draw(baseAnimation.getKeyFrame(baseAnimationCounter), x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotate, true)
        batch.draw(top, x, y, 16f, 16f, 32f, 32f, 1.5f, 1.5f, rotate, true)
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

enum class Items(val action: (GameScene) -> Boolean, val texture: Texture, val price: Float) {
    MARY_SEED({ scene -> scene.plant(scene.player) }, marySeedTexture, 1f),
    WATERING_CAN({ scene -> scene.water(scene.player) }, wateringCanTexture, 0.15f),
    HARVESTER({ scene -> scene.harvest(scene.player) }, harvesterTexture, 1.5f),
    MARY({ false }, maryTexture, 9f),
    PICKAXE({ scene -> scene.mine(scene.player) }, pickaxeTexture, 14f),
    SOIL({ scene -> scene.placeGround(scene.player) }, soilTexture, 14f),
    LAMP({ scene -> scene.placeLamp(scene.player) }, lampTexture, 14f),
    GUN({ scene -> scene.shoot(scene.player) }, gunTexture, 14f),
    BULLET({ false }, bulletTexture, 3f);

    fun getSellPrice(): Float = Math.round(price * 0.9 * 100) / 100f
    fun getBuyPrice(): Float = Math.round(price * 1.2 * 100) / 100f

}