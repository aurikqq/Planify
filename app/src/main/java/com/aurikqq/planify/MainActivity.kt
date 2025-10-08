package com.aurikqq.planify

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.screens.DailyPlansScreen
import com.aurikqq.planify.screens.HistoryScreen
import com.aurikqq.planify.screens.MainScreen
import com.aurikqq.planify.screens.NotesScreen
import com.aurikqq.planify.screens.UpdateLabel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.MainScreenViewModel
import com.aurikqq.planify.viewmodels.MainScreenViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlanifyTheme {
                AppActivity()
            }
        }
    }
}

@Composable
fun isKeyboardOpen() : Boolean {
    val ime = WindowInsets.ime.getBottom(LocalDensity.current)
    return ime > 0
}

@Composable
fun AppActivity() {
    val navController = rememberNavController()
    val orientation = LocalConfiguration.current.navigation

    Scaffold(
        bottomBar = {
            Column {
                UpdateLabel()
                if (orientation == Configuration.ORIENTATION_PORTRAIT) BottomBar(navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MainScreen(
            navController = navController,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = if (isKeyboardOpen()) 0.dp else innerPadding.calculateBottomPadding()
                )
        )
        Column {
            Spacer(modifier = Modifier.size(64.dp))
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomBar(navController: NavController) {
    val currentScreen = navController.currentBackStackEntry?.destination?.route
        BottomAppBar(windowInsets = BottomAppBarDefaults.windowInsets) {
            NavigationBarItem(
                selected = true,
                onClick = {
                    navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Checklist,
                        null
                    )
                },
                label = { Text("Планы") }
            )
            NavigationBarItem(
                enabled = false,
                selected = currentScreen == "",
                onClick = {
                    //navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Work,
                        null
                    )
                },
                label = { Text("Занятия") }
            )
            NavigationBarItem(
                enabled = false,
                selected = currentScreen == "",
                onClick = {
                    //navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        null
                    )
                },
                label = { Text("Настройки") }
            )
    }
}

@Composable
fun NavRail(navController: NavController) {
    val currentScreen = navController.currentBackStackEntry?.destination?.route

    NavigationRail(
        windowInsets = BottomAppBarDefaults.windowInsets,
    ) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            NavigationRailItem(
                selected = true,
                onClick = {
                    navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Checklist,
                        null
                    )
                },
                label = { Text("Планы") }
            )
            Spacer(modifier = Modifier.size(32.dp))

            NavigationRailItem(
                selected = currentScreen == "",
                enabled = false,
                onClick = {
                    //navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Work,
                        null
                    )
                },
                label = { Text("Занятия") }
            )
            Spacer(modifier = Modifier.size(32.dp))

            NavigationRailItem(
                selected = currentScreen == "",
                enabled = false,
                onClick = {
                    //navController.navigate(PLANS_SCREEN)
                },
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        null
                    )
                },
                label = { Text("Настройки") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTab = mainScreenTabs.indexOf(currentRoute ?: mainScreenTabs.first())

    val keyboardController = LocalSoftwareKeyboardController.current
    val pagerState = rememberPagerState(pageCount = { mainScreenTabs.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentRoute) {
        val newIndex = mainScreenTabs.indexOf(currentRoute)
        if (newIndex != -1 && newIndex != pagerState.currentPage) {
            pagerState.scrollToPage(newIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        keyboardController?.hide()
        val newRoute = mainScreenTabs[pagerState.currentPage]
        if (newRoute != currentRoute) {
            navController.navigate(newRoute) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) { /*TODO maybe change to saving last 5 screens or like that*/
                    saveState = true
                }
            }
        }
    }

    Column {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = pagerState.currentPage == mainScreenTabs.indexOf(PLANS_SCREEN),
                onClick = {
                    if (pagerState.currentPage != mainScreenTabs.indexOf(PLANS_SCREEN)) {
                        coroutineScope.launch { pagerState.animateScrollToPage(mainScreenTabs.indexOf(PLANS_SCREEN)) }
                    }
                },
                text = { Text("Дневные", overflow = TextOverflow.Ellipsis) }
            )
            Tab(
                selected = pagerState.currentPage == mainScreenTabs.indexOf(NOTES_SCREEN),
                onClick = {
                    if (pagerState.currentPage != mainScreenTabs.indexOf(NOTES_SCREEN)) {
                        coroutineScope.launch { pagerState.animateScrollToPage(mainScreenTabs.indexOf(NOTES_SCREEN)) }
                    }
                },
                text = { Text("Записи", overflow = TextOverflow.Ellipsis) }
            )
            Tab(
                selected = pagerState.currentPage == mainScreenTabs.indexOf(HISTORY_SCREEN),
                onClick = {
                    if (pagerState.currentPage != mainScreenTabs.indexOf(HISTORY_SCREEN)) {
                        coroutineScope.launch { pagerState.animateScrollToPage(mainScreenTabs.indexOf(HISTORY_SCREEN)) }
                    }
                },
                text = { Text("История", overflow = TextOverflow.Ellipsis) }
            )
        }

        HorizontalPager(pagerState) { page ->
            when (mainScreenTabs[page]) {
                PLANS_SCREEN -> {
                    val context = LocalContext.current
                    val parentEntry = remember(navBackStackEntry) {
                        navController.getBackStackEntry(PLANS_SCREEN)
                    }
                    val viewModel: MainScreenViewModel = viewModel(
                        factory = MainScreenViewModelFactory(
                            Repository(
                                context.getSharedPreferences(
                                    PREFERENCES_NAME, Context.MODE_PRIVATE),
                                context
                            )),
                        viewModelStoreOwner = parentEntry
                    )
                    val uiState by viewModel.uiState.collectAsState()
                    DailyPlansScreen(context, uiState, viewModel)
                }
                NOTES_SCREEN -> NotesScreen()
                HISTORY_SCREEN -> HistoryScreen()
            }
        }
    }
}

suspend fun checkUpdates(context: Context) : Boolean {
    val current = getCurrentVersion(context)
    val latest = getLatestVersion()
    val result = isNewVersionAvailable(current!!, latest!!)

    return result
}

@Preview
@Composable
fun Preview() {
    MainActivity()
}