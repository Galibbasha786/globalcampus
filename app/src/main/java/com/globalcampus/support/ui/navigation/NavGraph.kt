package com.globalcampus.support.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.ui.academic.AcademicToolkitScreen
import com.globalcampus.support.ui.auth.LoginScreen
import com.globalcampus.support.ui.auth.SignupScreen
import com.globalcampus.support.ui.buddy.BuddyMatchingScreen
import com.globalcampus.support.ui.budget.BudgetPlannerScreen
import com.globalcampus.support.ui.chat.AiChatScreen
import com.globalcampus.support.ui.chat.CommunityChatScreen
import com.globalcampus.support.ui.community.CommunityScreen
import com.globalcampus.support.ui.community.PostDetailScreen
import com.globalcampus.support.ui.components.GlobalCampusBottomBar
import com.globalcampus.support.ui.components.GlobalCampusTopBar
import com.globalcampus.support.ui.doc.DocExplainerScreen
import com.globalcampus.support.ui.events.EventsScreen
import com.globalcampus.support.ui.help.HelpScreen
import com.globalcampus.support.ui.home.HomeScreen
import com.globalcampus.support.ui.info.ArticleDetailScreen
import com.globalcampus.support.ui.info.InfoListScreen
import com.globalcampus.support.ui.lostfound.LostFoundScreen
import com.globalcampus.support.ui.navigator.CampusNavigatorScreen
import com.globalcampus.support.ui.notifications.NotificationsScreen
import com.globalcampus.support.ui.onboarding.OnboardingScreen
import com.globalcampus.support.ui.planner.ArrivalPlannerScreen
import com.globalcampus.support.ui.profile.ProfileScreen
import com.globalcampus.support.ui.splash.SplashScreen
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import com.globalcampus.support.ui.tools.PhrasebookScreen
import com.globalcampus.support.ui.tools.VisaToolsScreen
import kotlinx.coroutines.launch

private val BOTTOM_TABS = setOf(
    Routes.HOME,
    Routes.COMMUNITY,
    Routes.HELP,
    Routes.CHAT,
    Routes.PROFILE
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val auth = remember { AuthRepository() }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in BOTTOM_TABS

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Side Bar Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(PrimaryNavy, SecondaryRoyalBlue))
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌍", fontSize = 28.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "LPU GlobalCampus",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            auth.currentUser?.email ?: "International Student Hub",
                            color = Color.White.copy(0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Navigation Drawer Items with Icons
                NavigationDrawerItem(
                    label = { Text("Home Hub", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Home, null, tint = PrimaryNavy) },
                    selected = currentRoute == Routes.HOME,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.HOME) { launchSingleTop = true }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Live Community Chat", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Forum, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.CHAT_COMMUNITY,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.CHAT_COMMUNITY)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Arrival Planner", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.FlightLand, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.PLANNER,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.PLANNER)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Buddy Matcher", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.People, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.BUDDY,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.BUDDY)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Budget Planner", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.BUDGET,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.BUDGET)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Campus Events", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Event, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.EVENTS,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.EVENTS)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("LPU Satellite Navigation", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Satellite, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.NAVIGATOR,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.NAVIGATOR)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("AI Doc Explainer", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.AutoAwesome, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.DOC_EXPLAINER,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.DOC_EXPLAINER)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Lost & Found", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Search, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.LOST_FOUND,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.LOST_FOUND)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Academic Toolkit", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.School, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.ACADEMIC,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.ACADEMIC)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("My Profile", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Person, null, tint = SecondaryRoyalBlue) },
                    selected = currentRoute == Routes.PROFILE,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        auth.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showBottomBar) {
                    GlobalCampusTopBar(
                        title = "LPU GlobalCampus",
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAiClick = { navController.navigate(Routes.CHAT) },
                        onNotificationClick = { navController.navigate("notifications") }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    GlobalCampusBottomBar(navController = navController, currentRoute = currentRoute)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.padding(innerPadding)
            ) {
                // ---------- Splash & Onboarding ----------
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onNavigateToHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        },
                        onNavigateToOnboarding = {
                            navController.navigate(Routes.ONBOARDING) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                // ---------- Auth ----------
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onGoToSignup = { navController.navigate(Routes.SIGNUP) }
                    )
                }

                composable(Routes.SIGNUP) {
                    SignupScreen(
                        onSignupSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onGoToLogin = { navController.popBackStack() }
                    )
                }

                // ---------- Main tabs ----------
                composable(Routes.HOME) {
                    HomeScreen(
                        onLogout = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        },
                        onOpenInfoHub = { navController.navigate(Routes.INFO_HUB) },
                        onOpenCommunity = { navController.navigate(Routes.COMMUNITY) },
                        onOpenHelp = { navController.navigate(Routes.HELP) },
                        onOpenChat = { navController.navigate(Routes.CHAT) },
                        onOpenVisaTools = { navController.navigate(Routes.VISA_TOOLS) },
                        onOpenPhrasebook = { navController.navigate(Routes.PHRASEBOOK) },
                        onOpenPlanner = { navController.navigate(Routes.PLANNER) },
                        onOpenBuddy = { navController.navigate(Routes.BUDDY) },
                        onOpenBudget = { navController.navigate(Routes.BUDGET) },
                        onOpenEvents = { navController.navigate(Routes.EVENTS) },
                        onOpenNavigator = { navController.navigate(Routes.NAVIGATOR) },
                        onOpenDocExplainer = { navController.navigate(Routes.DOC_EXPLAINER) },
                        onOpenLostFound = { navController.navigate(Routes.LOST_FOUND) },
                        onOpenAcademic = { navController.navigate(Routes.ACADEMIC) }
                    )
                }

                composable(Routes.COMMUNITY) {
                    CommunityScreen(
                        onOpenPost = { id ->
                            navController.navigate(Routes.postDetail(id))
                        }
                    )
                }

                composable(Routes.HELP) {
                    HelpScreen(
                        onOpenPost = { id ->
                            navController.navigate(Routes.postDetail(id))
                        }
                    )
                }

                composable(Routes.CHAT) {
                    AiChatScreen(
                        onOpenVisaTools = { navController.navigate(Routes.VISA_TOOLS) },
                        onOpenPhrasebook = { navController.navigate(Routes.PHRASEBOOK) }
                    )
                }

                composable(Routes.CHAT_COMMUNITY) {
                    CommunityChatScreen(
                        onBack = { navController.popBackStack() },
                        onStartVideoCall = { navController.navigate(Routes.VIDEO_CALL) }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }

                // ---------- Full-screen / Tools ----------
                composable(Routes.INFO_HUB) {
                    InfoListScreen(
                        onOpenArticle = { id ->
                            navController.navigate(Routes.articleDetail(id))
                        }
                    )
                }

                composable(Routes.ARTICLE_DETAIL) { entry ->
                    val id = entry.arguments?.getString("articleId") ?: ""
                    ArticleDetailScreen(articleId = id, onBack = { navController.popBackStack() })
                }

                composable(Routes.POST_DETAIL) { entry ->
                    val id = entry.arguments?.getString("postId") ?: ""
                    PostDetailScreen(postId = id, onBack = { navController.popBackStack() })
                }

                composable(Routes.VISA_TOOLS) {
                    VisaToolsScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.PHRASEBOOK) {
                    PhrasebookScreen(onBack = { navController.popBackStack() })
                }

                // ---------- Innovative Feature Routes ----------
                composable(Routes.PLANNER) {
                    ArrivalPlannerScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.BUDDY) {
                    BuddyMatchingScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.BUDGET) {
                    BudgetPlannerScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.EVENTS) {
                    EventsScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.NAVIGATOR) {
                    CampusNavigatorScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.DOC_EXPLAINER) {
                    DocExplainerScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.LOST_FOUND) {
                    LostFoundScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.ACADEMIC) {
                    AcademicToolkitScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.VIDEO_CALL) {
                    com.globalcampus.support.ui.call.VideoCallScreen(
                        recipientName = "LPU Global Community",
                        isGroupCall = true,
                        onEndCall = { navController.popBackStack() }
                    )
                }
                composable("notifications") {
                    NotificationsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
