package com.dins.minddrop.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.dins.minddrop.ui.addnote.AddNoteScreen
import com.dins.minddrop.ui.notedetail.NoteDetailScreen
import com.dins.minddrop.ui.notelist.NoteListScreen
import com.dins.minddrop.ui.settings.SettingsScreen

private const val TRANSITION_MS = 300

@Composable
fun MindDropNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NoteDestination.NoteList,
        modifier = modifier.fillMaxSize(),
        // Forward navigation slides in from the right and back pops out to the
        // right, so the gesture direction matches the sense of depth.
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeIn(animationSpec = tween(TRANSITION_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeOut(animationSpec = tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeIn(animationSpec = tween(TRANSITION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeOut(animationSpec = tween(TRANSITION_MS))
        }
    ) {
        composable<NoteDestination.NoteList> {
            NoteListScreen(
                onAddNoteClick = { navController.navigate(NoteDestination.AddNote) },
                onNoteClick = { noteId -> navController.navigate(NoteDestination.NoteDetail(noteId)) },
                onSettingsClick = { navController.navigate(NoteDestination.Settings) }
            )
        }
        composable<NoteDestination.Settings> {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
        composable<NoteDestination.AddNote> {
            AddNoteScreen(
                onNoteSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable<NoteDestination.NoteDetail>(
            deepLinks = listOf(navDeepLink<NoteDestination.NoteDetail>(basePath = "minddrop://note"))
        ) { backStackEntry ->
            val destination: NoteDestination.NoteDetail = backStackEntry.toRoute()
            NoteDetailScreen(
                noteId = destination.noteId,
                onNoteDeleted = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
