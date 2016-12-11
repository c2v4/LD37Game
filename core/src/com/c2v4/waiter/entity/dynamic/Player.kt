package com.c2v4.waiter.entity.dynamic


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.entity.dynamic.Items.*
import com.c2v4.waiter.helper.*
import com.c2v4.waiter.helper.Direction.*
import com.c2v4.waiter.screen.GameScene

class Player {
    val startingX = 16
    val startingY = 2
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
    var money = 10f
//    val maxInventoryItems = 10

    constructor(world: World, gameScene: GameScene) {
        this.gameScene = gameScene
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
                Mover(Input.Keys.F7, { money+=1000 })
        )
        moveListeners = arrayOf(
                Mover(Input.Keys.UP, { body.applyForceToCenter(Vector2(0f, SPEED), true);direction = UP }),
                Mover(Input.Keys.DOWN, { body.applyForceToCenter(Vector2(0f, -SPEED), true);direction = DOWN }),
                Mover(Input.Keys.RIGHT, { body.applyForceToCenter(Vector2(SPEED, 0f), true);direction = RIGHT }),
                Mover(Input.Keys.LEFT, { body.applyForceToCenter(Vector2(-SPEED, 0f), true);direction = LEFT }),
                Mover(Input.Keys.Z, {
                    val item = inventory[currentItem]
                    if(arrayOf(MARY_SEED,HARVESTER).contains(item.type))
                    if (item.type.action(gameScene)) {
                        removeItemFromInventory(item)
                    }
                })
        )


    }

    private fun removeItemFromInventory(item: Item) {
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
        sprite.x = body.position.x * PPM + 32 * startingX
        sprite.y = body.position.y * PPM + 32 * startingY + 32
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

enum class Items(val action: (GameScene) -> Boolean, val texture: Texture, val price: Float) {
    MARY_SEED({ scene -> scene.plant(scene.player) }, marySeedTexture, 1f),
    WATERING_CAN({ scene -> scene.water(scene.player) }, wateringCanTexture, 0.15f),
    HARVESTER({ scene -> scene.harvest(scene.player) }, harvesterTexture, 1.5f),
    MARY({ false }, maryTexture, 9f),
    PICKAXE({ scene -> scene.mine(scene.player) }, pickaxeTexture, 14f),
    SOIL({ scene -> scene.placeGround(scene.player) }, soilTexture, 14f),
    LAMP({ scene -> scene.placeLamp(scene.player) }, lampTexture, 14f);

    fun getSellPrice(): Float = Math.round(price * 0.9 * 100) / 100f
    fun getBuyPrice(): Float = Math.round(price * 1.2 * 100) / 100f

}