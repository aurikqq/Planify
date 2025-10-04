package com.aurikqq.planify

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aurikqq.planify.screens.MainScreen
import com.aurikqq.planify.ui.theme.PlanifyTheme
import kotlinx.coroutines.launch
import java.io.File

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
        bottomBar = { if (orientation == Configuration.ORIENTATION_PORTRAIT) BottomBar(navController) },
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
    }
}

@Composable
fun UpdateLabel() {
    val context = LocalContext.current
    var isUpdateAvailable by rememberSaveable { mutableStateOf(false) }
    var isDownloading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var apk by rememberSaveable { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        isUpdateAvailable = checkUpdates(context)
    }

    if (isUpdateAvailable) {
        Button(
            onClick = {
                isDownloading = true
                scope.launch { apk = downloadApk(context) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(if (!isDownloading) "Скачать обновление" else "Скачивание...")
        }
    }
} /*TODO finalize this*/

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
    var selectedTab by rememberSaveable { mutableIntStateOf(PlansScreenTabs.Daily.ordinal) }

    PrimaryTabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == PlansScreenTabs.Daily.ordinal,
            onClick = {
                navController.navigate(PLANS_SCREEN)
                selectedTab = PlansScreenTabs.Daily.ordinal
            },
            text = { Text("Дневные", overflow = TextOverflow.Ellipsis) }
        )
        Tab(
            selected = selectedTab == PlansScreenTabs.Notes.ordinal,
            onClick = {
                navController.navigate(CONSTANT_PLANS_SCREEN)
                selectedTab = PlansScreenTabs.Notes.ordinal
            },
            text = { Text("Записи", overflow = TextOverflow.Ellipsis) }
        )
        Tab(
            selected = selectedTab == PlansScreenTabs.History.ordinal,
            onClick = {
                navController.navigate(HISTORY_SCREEN)
                selectedTab = PlansScreenTabs.History.ordinal
            },
            text = { Text("История", overflow = TextOverflow.Ellipsis) }
        )
    }
}

private suspend fun checkUpdates(context: Context) : Boolean {
    val current = getCurrentVersion(context)
    val latest = getLatestVersion()

    return isNewVersionAvailable(current!!, latest!!)
}

private suspend fun downloadUpdate(context: Context) : File {
    return downloadApk(context)
}

private fun installUpdate(context: Context, apk: File) {
    installApk(context, apk)
}

@Preview
@Composable
fun Preview() {
    MainActivity()
}