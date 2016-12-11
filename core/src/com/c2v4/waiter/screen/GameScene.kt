package com.c2v4.waiter.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.c2v4.waiter.HEIGHT
import com.c2v4.waiter.WIDTH
import com.c2v4.waiter.entity.dynamic.Player
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.entity.dynamic.Item
import com.c2v4.waiter.entity.dynamic.Items
import com.c2v4.waiter.entity.dynamic.Policeman
import com.c2v4.waiter.entity.static.Ground
import com.c2v4.waiter.entity.static.plant.Mary
import com.c2v4.waiter.helper.*
import com.sun.org.apache.xpath.internal.operations.Bool
import java.lang.Math.abs
import java.lang.Math.round
import java.util.*


class GameScene : Screen {
    internal val batch = SpriteBatch()
    internal val camera = OrthographicCamera(WIDTH / PPM, HEIGHT / PPM)
    val shapeRenderer = ShapeRenderer()

    internal var player: Player

    var showShop = false
    var shopPointer = 0
    val map = TmxMapLoader().load("maps/map.tmx")
    val tiledMapRenderer = OrthogonalTiledMapRenderer(map, 1 / PPM)

    val world = World(Vector2(0f, 0f), false)
    var debugRenderer: Box2DDebugRenderer = Box2DDebugRenderer()
    val font = BitmapFont()

    val grounds = mutableMapOf<Point2D, Ground>()
    val bodies = mutableMapOf<Point2D, Body>()
    val lamps = mutableMapOf<Point2D, Sprite>()
    val police = mutableListOf<Policeman>()
    private val random = Random()

    constructor() {
        Box2D.init()
        player = Player(world, this)
        camera.translate(Gdx.graphics.width / 2 / PPM, Gdx.graphics.height / 2 / PPM)
        initializeMap()
        police.add(Policeman(world, this))
    }

    private fun initializeMap() {
        val ground = map.layers.get("ground") as TiledMapTileLayer
        val walls = map.layers.get("wall") as TiledMapTileLayer
        val invisibleWalls = map.layers.get("invisible-wall") as TiledMapTileLayer
        val lampLayer = map.layers["lamp"]
        lampLayer.objects.filter { it is EllipseMapObject }.forEach {
            val ellipseMapObject = it as EllipseMapObject
            val point2D = Point2D(round(ellipseMapObject.ellipse.x / 32), round(ellipseMapObject.ellipse.y / 32))
            placeLampAt(point2D)
        }
        map.layers.remove(lampLayer)
        (0..ground.width)
                .forEach { x ->
                    (0..ground.height)
                            .forEach { y ->
                                if (ground.getCell(x, y) != null) {
                                    initializeGround(x, y)
                                }
                                if (walls.getCell(x, y) != null) {
                                    createBlock(x, y)
                                }
                                if (invisibleWalls.getCell(x, y) != null) {
                                    createBlock(x, y)
                                }
                            }
                }
    }

    private fun placeLampAt(point2D: Point2D) {
        val sprite = Sprite(lampTexture)
        sprite.x = point2D.x * 32f
        sprite.y = point2D.y * 32f
        lamps.put(point2D, sprite)
    }

    private fun initializeGround(x: Int, y: Int) {
        createBlock(x, y)
        grounds.put(Point2D(x, y), Ground(x, y))
    }

    private fun createBlock(x: Int, y: Int) {
        val fixtureDef = FixtureDef()
        val polygonShape = PolygonShape()
        polygonShape.setAsBox(16 / PPM, 16 / PPM)
        fixtureDef.shape = polygonShape
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position.x = 32 / PPM * x + 16 / PPM
        bodyDef.position.y = 32 / PPM * y + 16 / PPM
        val createdBody = world.createBody(bodyDef)
        createdBody.createFixture(fixtureDef)
        bodies.put(Point2D(x, y), createdBody)
    }

    override fun show() {

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT)
        update(delta)
        camera.update()
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()
        batch.begin()
        renderLamps()
        player.draw(batch)
        renderPolice()
        renderPlants()
        renderInventory()
        renderShop()
        batch.end()
//        debugRenderer.render(world, camera.combined.cpy())
    }

    private fun renderPolice() {
        police.forEach { it.draw(batch) }
    }

    private fun renderLamps() {
        lamps.values.forEach {
            it.draw(batch)
        }
    }


    private fun renderShop() {
        if (showShop) {
            val items = Items.values()
            var selectedWidth = 104f
            font.draw(batch, String.format("%1$.2f", player.money), 960f, 744f)
            (shopPointer - 2..shopPointer + 2).map {
                if (it < 0) {
                    items.size + it
                } else {
                    if (it >= items.size) {
                        (items.size + it) % items.size
                    } else {
                        it
                    }
                }
            }.forEachIndexed { i, it ->
                val sprite = Sprite(items[it].texture)
                sprite.x = 480f
                sprite.y = 600f - i * 70
                sprite.setScale(2f)
                sprite.draw(batch)
                font.color = Color.ROYAL
                font.draw(batch, "${player.inventory
                        .filter { iterator -> iterator.type == items[it] }
                        .map(Item::quantity)
                        .firstOrNull() ?: 0}", sprite.x - 20, sprite.y + 20)
                font.color = Color.LIME
                font.draw(batch, "${items[it].getBuyPrice()}", sprite.x - 20, sprite.y + 40)
                font.color = Color.SCARLET
                font.draw(batch, "${items[it].getSellPrice()}", sprite.x + 20, sprite.y + 40)

                font.color = Color.WHITE
                val draw = font.draw(batch, items[it].name.replace('_', ' '), sprite.x - 20, sprite.y)
                if (i == 2) {
                    selectedWidth = draw.width
                }
            }

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.CYAN
            val widthWithSomething: Float
            if ((selectedWidth + 24f) < 76f) {
                widthWithSomething = 76f
            }
            else {
                widthWithSomething = selectedWidth + 24f
            }
            shapeRenderer.rect(454f, 440f, widthWithSomething, 70f)
            shapeRenderer.end()

        }
    }

    private fun renderPlants() {
        grounds.values.forEach { it.draw(batch) }
    }

    private fun renderInventory() {
        if (player.showInventory) {
            (0..player.inventory.size - 1).forEach {
                val item = player.inventory[it]
                val sprite = Sprite(item.type.texture)
                sprite.x = 32f + 70 * it
                sprite.y = 32f
                sprite.setScale(2f)
                sprite.draw(batch)
                font.draw(batch, "${item.quantity}", sprite.x, sprite.y)
            }
            if (!player.inventory.isEmpty()) {
                val selector = Sprite(selectorTexture)
                selector.setScale(2f)
                selector.x = 32f + 70 * player.currentItem
                selector.y = 32f
                selector.draw(batch)
            }
        }
    }


    private fun update(delta: Float) {
        world.step(1f / 30, 6, 2)
        player.update(delta)
        police.forEach { it.update(delta) }
        grounds.values.filter { it.plant != null }.forEach {
            it.grow(getGrowValue(Point2D(it.x, it.y)))
        }
    }

    private fun getGrowValue(point2D: Point2D): Int {
        val sum = lamps.keys.map { 2 * (5 - (abs(it.x - point2D.x) + abs(it.y - point2D.y))) }.filter { it > 0 }.sum()
        val chance = random.nextInt(sum + 1)
        return chance
    }

    override fun pause() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun hide() {
    }

    override fun resume() {
    }

    override fun dispose() {
        batch.dispose()
    }

    fun plant(player: Player): Boolean {
        val ground = grounds[player.getActionPoint()]
        if (ground != null && ground.plant == null) {
            ground.plant = Mary()
            return true
        }
        return false
    }

    fun water(player: Player): Boolean {
        val ground = grounds.get(player.getActionPoint())
        if (ground != null && ground.plant != null) {
            ground.water()
            return true
        }
        return false
    }

    fun harvest(player: Player): Boolean {
        val ground = grounds.get(player.getActionPoint())

        val fullGrown = ground?.plant?.fullGrown()
        if (ground != null && ground.plant != null && fullGrown != null && fullGrown) {
            ground.plant = null
            player.addItem(Items.MARY, 1)
            return true
        }
        return false
    }

    fun mine(player: Player): Boolean {
        val actionPoint = player.getActionPoint()
        val wall = getWall(actionPoint)
        val floorCell = map.tileSets.getTile(11)
        if (wall != null) {
            val background = map.layers["background"] as TiledMapTileLayer
            val walls = map.layers.get("wall") as TiledMapTileLayer
            walls.setCell(actionPoint.x, actionPoint.y, null)
            val cell = TiledMapTileLayer.Cell()
            cell.tile = floorCell
            background.setCell(actionPoint.x, actionPoint.y, cell)

            world.destroyBody(bodies[actionPoint])
            bodies.remove(actionPoint)
            val wallCell = TiledMapTileLayer.Cell()
            wallCell.tile = map.tileSets.getTile(12)

            (actionPoint.x - 1..actionPoint.x + 1).filter { it >= 0 && it < walls.width }.forEach { x ->
                run {
                    (actionPoint.y - 1..actionPoint.y + 1).filter { it >= 0 && it < walls.width }.filter { y ->
                        map.layers.all { it is TiledMapTileLayer && it.getCell(x, y) == null }
                    }.forEach {
                        y ->
                        run {
                            walls.setCell(x, y, wallCell)
                            createBlock(x, y)
                        }
                    }
                }
            }
            return true
        } else {
            val groundLayer = map.layers.get("ground") as TiledMapTileLayer
            if (groundLayer.getCell(actionPoint.x, actionPoint.y) == null) {
                val lampPosition = lamps.keys.firstOrNull { it == actionPoint }
                if (lampPosition != null) {
                    lamps.remove(lampPosition)
                }
                return false
            }
            groundLayer.setCell(actionPoint.x, actionPoint.y, null)
            world.destroyBody(bodies[actionPoint])
            bodies.remove(actionPoint)
            grounds.remove(actionPoint)
            player.addItem(Items.SOIL, 1)
            return false//shall use durability on ground
        }
    }

    fun placeGround(player: Player): Boolean {
        val actionPoint = player.getActionPoint()
        var empty = map.layers
                .filter { it.name != "background" }
                .all { it is TiledMapTileLayer && it.getCell(actionPoint.x, actionPoint.y) == null }
        empty = empty and lamps.keys.none { it == actionPoint }
        empty = empty and police.none { it.aStar.contains(actionPoint) }
        if (empty) {
            initializeGround(actionPoint.x, actionPoint.y)
            val groundLayer = map.layers["ground"] as TiledMapTileLayer
            val cell = TiledMapTileLayer.Cell()
            val tile = map.tileSets.getTile(10)
            cell.tile = tile
            groundLayer.setCell(actionPoint.x, actionPoint.y, cell)
            return true
        }
        return false
    }

    fun placeLamp(player: Player): Boolean {
        val actionPoint = player.getActionPoint()
        var empty = map.layers
                .filter { it.name != "background" }
                .all { it is TiledMapTileLayer && it.getCell(actionPoint.x, actionPoint.y) == null }
        empty = empty and lamps.keys.none { it == actionPoint }
        if (empty) {
            placeLampAt(Point2D(actionPoint.x, actionPoint.y))
            return true
        }
        return false
    }

    fun shoot(player: Player): Boolean {
        val bullet = player.inventory.filter { it.type == Items.BULLET }.firstOrNull()
        if (bullet != null && bullet.quantity > 0) {

        }
        return false
    }

    private fun getWall(actionPoint: Point2D): TiledMapTileLayer.Cell? {
        val walls = map.layers.get("wall") as TiledMapTileLayer
        return walls.getCell(actionPoint.x, actionPoint.y)
    }


}