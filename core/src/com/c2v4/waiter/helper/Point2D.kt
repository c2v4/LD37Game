package com.c2v4.waiter.helper

import com.c2v4.waiter.screen.GameScene

data class Point2D(val x:Int,val y:Int)

fun aStar(source:Point2D,destination: Point2D,blockers:Set<Point2D>): List<Point2D> {
    val closedSet = mutableSetOf<Point2D>()
    val openSet = mutableMapOf<Point2D, MutableList<Point2D>>()
    openSet.put(source, mutableListOf<Point2D>())
    while (openSet.isNotEmpty()) {
        for ((x1, y1) in openSet.keys.toSet()) {
            val point2D = Point2D(x1, y1)
            val currentPath: MutableList<Point2D> = openSet[point2D] ?: mutableListOf()
            if (openSet.containsKey(point2D)) {
                openSet.remove(point2D)
            }
            for (i in -1..1) {
                for (j in -1..1) {
                    if (i == j || j == -i) continue
                    val currentPoint = Point2D(x1 + i, y1 + j)
                    if (blockers.contains(currentPoint) or
                            closedSet.contains(currentPoint) or
                            openSet.contains(currentPoint)) continue
                    val list = currentPath.toMutableList()
                    list.add(currentPoint)
                    if(currentPoint==destination){
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