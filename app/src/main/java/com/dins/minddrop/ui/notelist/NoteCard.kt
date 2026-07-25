package com.dins.minddrop.ui.notelist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dins.minddrop.domain.model.Note
import com.dins.minddrop.domain.model.NoteType
import com.dins.minddrop.domain.model.Priority
import com.dins.minddrop.ui.theme.priorityColor

/**
 * [NoteCard] wrapped in a swipe-to-dismiss gesture. Only end-to-start (right to
 * left) is enabled: a single direction keeps the gesture unambiguous, and delete
 * is the only destructive action available from the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // Reacting to the settled value rather than vetoing via confirmValueChange,
    // which is deprecated. No reset is needed on success: deleting invalidates
    // the PagingSource, so this composable leaves the list entirely.
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { DeleteBackground(dismissState.targetValue) }
    ) {
        NoteCard(note = note, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBackground(targetValue: SwipeToDismissBoxValue) {
    // The background intensifies as the swipe passes the dismiss threshold, so
    // the gesture signals its own commit point rather than deleting by surprise.
    val willDismiss = targetValue == SwipeToDismissBoxValue.EndToStart
    val backgroundColor by animateColorAsState(
        targetValue = if (willDismiss) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color.Transparent
        },
        label = "swipeBackground"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (willDismiss) 1f else 0.35f,
        label = "swipeIconAlpha"
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete note",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.alpha(iconAlpha)
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(type = note.type)
                PriorityDot(priority = note.priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = note.content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun TypeChip(type: NoteType, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = type.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PriorityDot(priority: Priority, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(12.dp),
        shape = CircleShape,
        color = priorityColor(priority)
    ) {}
}
