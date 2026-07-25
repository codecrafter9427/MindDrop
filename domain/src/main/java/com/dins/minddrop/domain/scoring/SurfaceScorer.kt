package com.dins.minddrop.domain.scoring

import com.dins.minddrop.domain.model.Note

interface SurfaceScorer {
    fun score(note: Note, now: Long): Float
}
