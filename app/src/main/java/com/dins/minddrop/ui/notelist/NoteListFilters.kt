package com.dins.minddrop.ui.notelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority

@Composable
fun NoteListFilters(
    searchQuery: String,
    selectedType: NoteType?,
    selectedPriority: Priority?,
    filtersExpanded: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onTypeSelected: (NoteType) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search notes") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Expanding vertically rather than just fading keeps the list below from
        // jumping -- it slides down as the chips take up their space.
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically(animationSpec = tween(ANIMATION_MS)) +
                fadeIn(animationSpec = tween(ANIMATION_MS)),
            exit = shrinkVertically(animationSpec = tween(ANIMATION_MS)) +
                fadeOut(animationSpec = tween(ANIMATION_MS))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(type.name) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { onPrioritySelected(priority) },
                            label = { Text(priority.name) }
                        )
                    }
                }
            }
        }
    }
}

private const val ANIMATION_MS = 200
