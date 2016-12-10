package com.c2v4.waiter.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.c2v4.waiter.HEIGHT
import com.c2v4.waiter.WIDTH
import com.c2v4.waiter.entity.dynamic.Player
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.c2v4.waiter.entity.static.Ground
import com.c2v4.waiter.entity.static.plant.Mary
import com.c2v4.waiter.helper.Direction
import com.c2v4.waiter.helper.Direction.*
import com.c2v4.waiter.helper.PPM
import com.c2v4.waiter.helper.Point2D


class GameScene : Screen {
    internal val batch = SpriteBatch()
    internal val camera = OrthographicCamera(WIDTH / PPM, HEIGHT / PPM)

    internal var img = Texture(Gdx.files.internal("badlogic.jpg"))
    internal var player: Player

    val map = TmxMapLoader().load("maps/map.tmx")
    val tiledMapRenderer = OrthogonalTiledMapRenderer(map, 1 / PPM)

    val world = World(Vector2(0f, 0f), false)
    var debugRenderer: Box2DDebugRenderer = Box2DDebugRenderer()

    val grounds = mutableMapOf<Point2D,Ground>()

    constructor() {
        Box2D.init()
        player = Player(world,this)
        camera.translate(Gdx.graphics.width / 2 / PPM, Gdx.graphics.height / 2 / PPM)
        initializeMap()
    }

    private fun initializeMap() {
        val tiledMapTileLayer = map.layers.get("ground") as TiledMapTileLayer
        (0..tiledMapTileLayer.width)
                .forEach { x ->
                    (0..tiledMapTileLayer.height)
                            .forEach { y ->
                                if (tiledMapTileLayer.getCell(x, y) != null) {
                                    initializeGround(x, y)
                                }
                            }
                }
    }

    private fun initializeGround(x: Int, y: Int) {
        val fixtureDef = FixtureDef()
        val polygonShape = PolygonShape()
        polygonShape.setAsBox(16 / PPM, 16f / PPM)
        fixtureDef.shape = polygonShape
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position.x = 32 / PPM * x + 16 / PPM
        bodyDef.position.y = 32 / PPM * y + 16 / PPM
        val createBody = world.createBody(bodyDef)
        createBody.createFixture(fixtureDef)
        grounds.put(Point2D(x,y),Ground(x,y))
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
        player.sprite.draw(batch)
        grounds.values.forEach { it.draw(batch) }
        batch.end()
        debugRenderer.render(world, camera.combined.cpy())//.translate(-Gdx.graphics.width/2/ PPM,-Gdx.graphics.width/2/ PPM,0f))
    }

    private fun update(delta: Float) {
        world.step(1f / 60, 6, 2)
        player.update()
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

    fun plant(player: Player) {
        var x = Math.round(player.sprite.x / 32)
        var y = Math.round(player.sprite.y / 32)
        when (player.direction){
            UP -> y++
            DOWN -> y--
            LEFT -> x--
            RIGHT -> x++
        }
        val ground = grounds.get(Point2D(x, y))
        ground?.plant= Mary()

    }


}