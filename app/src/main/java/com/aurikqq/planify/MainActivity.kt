package com.aurikqq.planify

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.screens.DailyPlansScreen
import com.aurikqq.planify.screens.MainScreen
import com.aurikqq.planify.screens.NotesScreen
import com.aurikqq.planify.screens.PlansScreenUiState
import com.aurikqq.planify.screens.SettingsScreen
import com.aurikqq.planify.screens.UpdateLabel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.PlansScreenViewModel
import com.aurikqq.planify.viewmodels.PlansScreenViewModelFactory

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
    val context = LocalContext.current
    val navController = rememberNavController()
    val orientation = LocalConfiguration.current.navigation

    val viewModel: PlansScreenViewModel = viewModel(
        factory = PlansScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE),
                context
            )))

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                UpdateLabel()
                if (orientation == Configuration.ORIENTATION_PORTRAIT) BottomBar(navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = if (isKeyboardOpen()) 0.dp else innerPadding.calculateBottomPadding()
            )
        ) {
            NavHost(
                navController = navController,
                startDestination = MAIN_SCREEN,
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                composable(route = MAIN_SCREEN) {
                    MainScreen(
                        context,
                        viewModel,
                        uiState,
                        navController = navController,
                        modifier = Modifier
                    )
                }
                composable(route = SETTINGS_SCREEN) {
                    SettingsScreen(navController = navController)
                }
            }
        }
        Column {
            Spacer(modifier = Modifier.size(64.dp))
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomBar(navController: NavController) {
        BottomAppBar(windowInsets = BottomAppBarDefaults.windowInsets) {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = currentRoute == MAIN_SCREEN,
                onClick = {
                    navController.navigate(MAIN_SCREEN)
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
                selected = navController.currentDestination?.route == "",
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
                selected = currentRoute == SETTINGS_SCREEN,
                onClick = {
                    navController.navigate(SETTINGS_SCREEN)
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
fun TabsBar(
    context: Context,
    viewModel: PlansScreenViewModel,
    uiState: PlansScreenUiState,
    onTabSelected: (String) -> Unit
) {
    var currentRoute: String? by remember { mutableStateOf(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val pagerState = rememberPagerState(pageCount = { mainScreenTabs.size })

    LaunchedEffect(currentRoute) {
        val newIndex = mainScreenTabs.indexOf(currentRoute)
        if (newIndex != -1 && newIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(newIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        keyboardController?.hide()
        val newRoute = mainScreenTabs[pagerState.currentPage]
        if (newRoute != currentRoute) {
            onTabSelected(newRoute)
            currentRoute = newRoute
        }
    }

    Column {
        Row {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                contentColor = TabRowDefaults.secondaryContentColor,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        width = 64.dp,
                        modifier = Modifier
                            .tabIndicatorOffset(pagerState.currentPage)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Tab(
                    selected = pagerState.currentPage == mainScreenTabs.indexOf(PLANS_SCREEN),
                    onClick = {
                        onTabSelected(PLANS_SCREEN)
                        currentRoute = PLANS_SCREEN
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Checklist, null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Дневные")
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
                Tab(
                    selected = pagerState.currentPage == mainScreenTabs.indexOf(NOTES_SCREEN),
                    onClick = {
                        onTabSelected(NOTES_SCREEN)
                        currentRoute = NOTES_SCREEN
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Записи")
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
            }
        }

            HorizontalPager(pagerState) { page ->
                when (mainScreenTabs[page]) {
                    PLANS_SCREEN -> {
                        DailyPlansScreen(context, uiState, viewModel)
                    }
                    NOTES_SCREEN -> NotesScreen()
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

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun Preview() {
    PlanifyTheme { MainActivity() }
}