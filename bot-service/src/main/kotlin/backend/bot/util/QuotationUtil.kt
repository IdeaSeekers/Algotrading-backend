package backend.bot.util

import backend.tinkoff.model.Quotation

fun Quotation.toDouble(): Double = units.toDouble() + nano.toDouble() * 1e-9