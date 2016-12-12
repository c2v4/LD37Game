package com.c2v4.waiter.helper

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.c2v4.waiter.entity.dynamic.Player
import com.c2v4.waiter.entity.dynamic.Policeman

class MyContactListener : ContactListener {
    override fun beginContact(contact: Contact) {
        // Check to see if the collision is between the second sprite and the bottom of the screen
        // If so apply a random amount of upward force to both objects... just because
        val objA = contact.fixtureA.body.userData
        val objB = contact.fixtureB.body.userData
        if(objA is Player && objB is Policeman){
            objA.rip()
        }
        if(objB is Player && objA is Policeman){
            objB.rip()
        }
    }

    override fun endContact(contact: Contact) {
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }
}