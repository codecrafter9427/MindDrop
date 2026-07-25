package com.dins.minddrop.domain.scoring

import com.dins.minddrop.domain.model.Note

/** Single-purpose scoring contract; `fun interface` so tests can supply a lambda. */
fun interface SurfaceScorer {
    fun score(note: Note, now: Long): Float
}
