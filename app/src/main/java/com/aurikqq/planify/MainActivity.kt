package com.aurikqq.planify

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.screens.DailyPlansScreen
import com.aurikqq.planify.screens.DrawerContent
import com.aurikqq.planify.screens.MainScreen
import com.aurikqq.planify.screens.NotesScreen
import com.aurikqq.planify.screens.PlansScreenUiState
import com.aurikqq.planify.screens.SettingsScreen
import com.aurikqq.planify.screens.UpdateLabel
import com.aurikqq.planify.ui.theme.PlanifyTheme
import com.aurikqq.planify.viewmodels.PlansScreenViewModel
import com.aurikqq.planify.viewmodels.PlansScreenViewModelFactory
import com.aurikqq.planify.viewmodels.SettingsScreenViewModel
import com.aurikqq.planify.viewmodels.SettingsScreenViewModelFactory
//import com.aurikqq.planify.widgets.textwidget.Widget
//import com.aurikqq.planify.widgets.textwidget.WidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
//    fun isSystemInDarkMode() : Boolean {
//        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
//                Configuration.UI_MODE_NIGHT_YES
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(
//            if (isSystemInDarkMode())
//                R.style.Theme_App_Starting_Dark
//            else
//                R.style.Theme_App_Starting_Light
//        )

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
//            lifecycleScope.launch(Dispatchers.Default) {
//                //GlanceAppWidgetManager(this@MainActivity)
//                    //.setWidgetPreviews(WidgetReceiver::class)
//            }
//        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppActivity()
        }
    }

    //val intent = (applicationContext as Activity).intent

//    val widgetId = intent?.extras?.getInt(
//        AppWidgetManager.EXTRA_APPWIDGET_ID,
//        AppWidgetManager.INVALID_APPWIDGET_ID
//    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    //////fuck it
//
//    private fun saveTextWidgetState(id: String) = lifecycleScope.launch(Dispatchers.IO) {
//        val repo = Repository(
//            applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
//            applicationContext
//        )
//        val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
//        val note = repo.getNotesList().find { it.id == id } ?: return@launch
//        updateAppWidgetState(applicationContext, glanceId) { prefs ->
//            prefs[Widget().noteId] = id
//            prefs[Widget().noteTitle] = note.title
//            prefs[Widget().noteText] = note.text
//        }
//        Widget().update(applicationContext, glanceId)
//    }
}

@Composable
fun isKeyboardOpen() : Boolean {
    val ime = WindowInsets.ime.getBottom(LocalDensity.current)
    return ime > 0
}

//@Composable
//fun launchScreen(plansCardRect: Rect?, onExit: () -> Unit) {
//    var isIconVisible by remember { mutableStateOf(true) }
//    var isBoxExpanding by remember { mutableStateOf(false) }
//
//    val sizeX = plansCardRect?.width()
//    val sizeY = plansCardRect?.height()
//    val posY = plansCardRect?.centerY()
//
//    val transition = remember {
//        MutableTransitionState(true).apply {
//            targetState = isIconVisible
//        }
//    }
//
//    LaunchedEffect(transition.isIdle, transition.currentState) {
//        isIconVisible = false
//
//        if (transition.isIdle && !transition.currentState) {
//            delay(500L)
//            isBoxExpanding = true
//            delay(1000L)
//            onExit()
//        }
//    }
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        Box(modifier = Modifier
//            .size(
//                animateDpAsState(if (isBoxExpanding) sizeX!!.dp else 200.dp).value,
//                animateDpAsState(if (isBoxExpanding) sizeY!!.dp else 200.dp).value
//            )
//            .clip(CircleShape)
//            .background(CardDefaults.cardColors().containerColor)
//            .align(Alignment.Center)
//            .offset(
//                0.dp,
//                animateDpAsState(
//                    if (isBoxExpanding) posY!!.dp else 0.dp,
//                ).value
//            )
//        )
//
//        AnimatedVisibility(visibleState = transition, exit = fadeOut()) {
//            Image(
//                painterResource(R.drawable.ic_launcher_foreground),
//                null,
//                contentScale = ContentScale.Fit,
//                modifier = Modifier.align(Alignment.Center).size(300.dp)
//            )
//        }
//    }
//}

@Composable
fun AppActivity() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val viewModel: PlansScreenViewModel = viewModel(
        factory = PlansScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE),
                context
            )
        )
    )
    val settingsViewModel: SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModelFactory(
            Repository(
                context.getSharedPreferences(
                    PREFERENCES_NAME, Context.MODE_PRIVATE
                ),
                context
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val tabletMode = isTablet(context)

    val drawerState = remember{ DrawerState(DrawerValue.Closed) }
    var drawerContent by remember { mutableStateOf<@Composable () -> Unit>({}) }
    val scope = rememberCoroutineScope()

    var plansCardRect by remember { mutableStateOf<Rect?>(null) }
    var launchAnimationPlaying by remember { mutableStateOf(true) }


    PlanifyTheme(darkTheme = settingsUiState.isDarkThemeOn) {
        if (!tabletMode) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { DrawerContent(uiState, viewModel, drawerState, scope) },
                gesturesEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
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
                                composable(
                                    route = MAIN_SCREEN,
                                    enterTransition = {
                                        slideIntoContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Right,
                                            tween(350)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Left,
                                            tween(350)
                                        )
                                    }
                                ) {
                                    MainScreen(
                                        context,
                                        viewModel,
                                        uiState,
                                        drawerState = drawerState,
                                        scope = scope,
                                        modifier = Modifier,
//                                        { drawerContent =
//                                            { DrawerContent(uiState, viewModel, drawerState, scope) }
//                                        },
                                        //{ plansCardRect = it }
                                    )
                                }
                                composable(
                                    route = SETTINGS_SCREEN,
                                    enterTransition = {
                                        slideIntoContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Left,
                                            tween(350)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Right,
                                            tween(350)
                                        )
                                    }
                                ) {
                                    SettingsScreen(settingsViewModel)
                                }
                            }
                        }
                        Column {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                    }

                    Column(
                        Modifier
                            .background(Color.Transparent)
                            .align(Alignment.BottomCenter)
                    ) {
                        UpdateLabel()
                        if (!tabletMode) BottomBar(navController)
                    }
                }
            }
        }
        else {
            Box(Modifier.fillMaxSize()) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row {
                        NavRail(navController)

                        Column(
                            modifier = Modifier
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
                                enterTransition = { slideInVertically { it } + fadeIn() },
                                exitTransition = { slideOutVertically { -it } + fadeOut() },
                                popEnterTransition = { slideInVertically { -it } + fadeIn() },
                                popExitTransition = { slideOutVertically { it } + fadeOut() },
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                composable(
                                    route = MAIN_SCREEN,
                                    enterTransition = {
                                        slideIntoContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Down,
                                            tween(350)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Up,
                                            tween(350)
                                        )
                                    }
                                ) {
                                    MainScreen(
                                        context,
                                        viewModel,
                                        uiState,
                                        drawerState = drawerState,
                                        scope = scope,
                                        modifier = Modifier,
//                                        { drawerContent =
//                                            { DrawerContent(uiState, viewModel, drawerState, scope) }
//                                        },
                                        //{ plansCardRect = it }
                                    )
                                }
                                composable(
                                    route = SETTINGS_SCREEN,
                                    enterTransition = {
                                        slideIntoContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Up,
                                            tween(350)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Down,
                                            tween(350)
                                        )
                                    }
                                ) {
                                    SettingsScreen(settingsViewModel)
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier
                        .background(Color.Transparent)
                        .align(Alignment.BottomCenter)
                ) {
                    UpdateLabel()
                    if (!tabletMode) BottomBar(navController)
                }
            }
        }

//        if (launchAnimationPlaying) {
//            println("launched")
//            launchScreen(plansCardRect) { launchAnimationPlaying = false }
//        }
//        else {
//            println("stopped")
//        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomBar(navController: NavController) {
    Box {
//        Image(painterResource(
//            R.drawable.christmas_tree),
//            null,
//            modifier = Modifier
//                .size(48.dp)
//                .offset(44.dp, (-43).dp)
//        )

        BottomAppBar(
            windowInsets = BottomAppBarDefaults.windowInsets,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
        ) {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = currentRoute == MAIN_SCREEN,
                onClick = {
                    if (currentRoute != MAIN_SCREEN) {
                        navController.navigate(MAIN_SCREEN)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Checklist,
                        null
                    )
                },
                label = { Text(stringResource(R.string.label_plans)) }
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
                label = { Text(stringResource(R.string.label_activities)) }
            )
            NavigationBarItem(
                selected = currentRoute == SETTINGS_SCREEN,
                onClick = {
                    if (currentRoute != SETTINGS_SCREEN) {
                        navController.navigate(SETTINGS_SCREEN)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        null
                    )
                },
                label = { Text(stringResource(R.string.label_settings)) }
            )
        }
    }
}

@Composable
fun NavRail(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationRail(
        windowInsets = BottomAppBarDefaults.windowInsets,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            NavigationRailItem(
                selected = currentRoute == MAIN_SCREEN,
                onClick = {
                    if (currentRoute != MAIN_SCREEN) {
                        navController.navigate(MAIN_SCREEN)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Checklist,
                        null
                    )
                },
                label = { Text(stringResource(R.string.label_plans)) }
            )
            Spacer(modifier = Modifier.size(64.dp))

            NavigationRailItem(
                enabled = false,
                selected = false,
                onClick = {},
                icon = {
                    Icon(
                        Icons.Default.Work,
                        null
                    )
                },
                label = { Text(stringResource(R.string.label_activities)) }
            )
            Spacer(modifier = Modifier.size(64.dp))

            NavigationRailItem(
                selected = currentRoute == SETTINGS_SCREEN,
                onClick = {
                    if (currentRoute != SETTINGS_SCREEN) {
                        navController.navigate(SETTINGS_SCREEN)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        null
                    )
                },
                label = { Text(stringResource(R.string.label_settings)) }
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
    onTabSelected: (String) -> Unit,
    //onPlansRendered: (Rect) -> Unit
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
                            Text(stringResource(R.string.label_daily))
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
                            Text(stringResource(R.string.label_notes))
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
            }
        }

            HorizontalPager(state = pagerState) { page ->
                when (mainScreenTabs[page]) {
                    PLANS_SCREEN -> {
                        DailyPlansScreen(context, uiState, viewModel /*onPlansRendered*/)
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

/*
enum class TextWidgetDataTypes {
    PLANS,
    NOTE
}
*/

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun isTablet(context: Context = LocalContext.current) : Boolean {
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    return windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
fun Preview() {
    PlanifyTheme { MainActivity() }
}