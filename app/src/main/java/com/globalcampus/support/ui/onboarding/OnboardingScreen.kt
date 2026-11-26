package com.globalcampus.support.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.R
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String,
    val badge: String
)

val ONBOARDING_PAGES = listOf(
    OnboardingPageData(
        imageRes = R.drawable.l1,
        title = "Discover Your Global Campus",
        description = "Access complete university resources, visa guidelines, hostel info, and academic schedules all in one place.",
        badge = "ACADEMICS & LIFE"
    ),
    OnboardingPageData(
        imageRes = R.drawable.l2,
        title = "Connect With Students Worldwide",
        description = "Join a vibrant international community of students from over 50 countries. Share experiences and support each other.",
        badge = "GLOBAL COMMUNITY"
    ),
    OnboardingPageData(
        imageRes = R.drawable.l3,
        title = "24/7 AI Campus Support",
        description = "Get instant AI answers to your questions about FRRO, campus blocks, emergency numbers, and local city guides.",
        badge = "AI ADVISOR"
    )
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGES.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < ONBOARDING_PAGES.size - 1) {
                TextButton(onClick = onFinishOnboarding) {
                    Text(
                        "Skip",
                        color = SecondaryRoyalBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val page = ONBOARDING_PAGES[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = page.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryRoyalBlue.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = page.badge,
                        color = SecondaryRoyalBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = page.description,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(ONBOARDING_PAGES.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (isSelected) 28.dp else 10.dp, height = 10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) PrimaryNavy else Color.LightGray.copy(alpha = 0.5f)
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = if (pagerState.currentPage == ONBOARDING_PAGES.size - 1) "Get Started" else "Next",
            onClick = {
                if (pagerState.currentPage < ONBOARDING_PAGES.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinishOnboarding()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}
