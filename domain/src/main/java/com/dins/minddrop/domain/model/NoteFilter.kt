package com.dins.minddrop.domain.model

/**
 * Search and filter criteria applied to the note list.
 *
 * A blank [query] and null [type]/[priority] each mean "no restriction", so the
 * default instance matches every note.
 */
data class NoteFilter(
    val query: String = "",
    val type: NoteType? = null,
    val priority: Priority? = null
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || priority != null
}
