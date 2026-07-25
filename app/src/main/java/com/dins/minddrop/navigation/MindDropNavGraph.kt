package com.dins.minddrop.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dins.minddrop.ui.addnote.AddNoteScreen
import com.dins.minddrop.ui.notedetail.NoteDetailScreen
import com.dins.minddrop.ui.notelist.NoteListScreen

@Composable
fun MindDropNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NoteDestination.NoteList,
        modifier = modifier.fillMaxSize()
    ) {
        composable<NoteDestination.NoteList> {
            NoteListScreen(
                onAddNoteClick = { navController.navigate(NoteDestination.AddNote) },
                onNoteClick = { noteId -> navController.navigate(NoteDestination.NoteDetail(noteId)) }
            )
        }
        composable<NoteDestination.AddNote> {
            AddNoteScreen(
                onNoteSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable<NoteDestination.NoteDetail> { backStackEntry ->
            val destination: NoteDestination.NoteDetail = backStackEntry.toRoute()
            NoteDetailScreen(
                noteId = destination.noteId,
                onNoteDeleted = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
