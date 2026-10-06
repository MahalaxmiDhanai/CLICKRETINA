package com.brewkery.app.domain

import kotlin.random.Random

object TicketIdGenerator {
    /** Returns a ticket id in the format #BK-XXXXX (5 random digits). */
    fun generate(): String {
        val digits = Random.nextInt(0, 99999).toString().padStart(5, '0')
        return "#BK-$digits"
    }
}
