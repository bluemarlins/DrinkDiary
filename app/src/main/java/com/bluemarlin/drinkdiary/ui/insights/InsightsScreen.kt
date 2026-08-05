package com.bluemarlin.drinkdiary.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.component.DDMonthlyTrendCard
import com.bluemarlin.drinkdiary.ui.component.DDPriceBracketCard
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType

@Composable
fun InsightsRoute(
    viewModel: InsightsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    DDScreenScaffold(
        title = "인사이트",
        screenType = DDScreenType.Detail,
        onBackClick = onBack,
    ) { padding ->
        when (val uiState = state) {
            InsightsUiState.Loading -> DDLoadingContent(Modifier.padding(padding))
            InsightsUiState.Empty ->
                DDEmptyContent(
                    message = "표시할 인사이트가 없습니다.",
                    actionText = "뒤로가기",
                    onAction = onBack,
                    modifier = Modifier.padding(padding),
                )

            is InsightsUiState.Error -> DDErrorContent(uiState.message, modifier = Modifier.padding(padding))
            is InsightsUiState.Success ->
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .padding(16.dp),
                ) {
                    val contentMaxWidth = if (maxWidth >= 840.dp) 1100.dp else maxWidth
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .widthIn(max = contentMaxWidth)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        DDMonthlyTrendCard(monthlyTrend = uiState.summary.monthlyTrend)
                        DDPriceBracketCard(priceBrackets = uiState.summary.priceBrackets)
                    }
                }
        }
    }
}
