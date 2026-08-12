package com.bluemarlin.drinkdiary.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordListItem
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab

private val TopLevelBottomContentPadding = 112.dp

@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onDashboardClick: () -> Unit,
    onCollectionClick: () -> Unit,
    onOpenRecord: (Long) -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val state by viewModel.uiState.collectAsState()

    DDScreenScaffold(
        title = stringResource(R.string.search_title),
        screenType = DDScreenType.TopLevel,
        selectedTab = DDTopLevelTab.Search,
        onDashboardClick = onDashboardClick,
        onCollectionClick = onCollectionClick,
        onSearchClick = {},
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .padding(16.dp)
                    .widthIn(max = 840.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SearchTextField(
                query = query,
                onQueryChange = viewModel::updateQuery,
                onClearClick = viewModel::clearQuery,
            )
            SearchStateContent(
                state = state,
                onOpenRecord = onOpenRecord,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.search_label)) },
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Text("×", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
    )
}

@Composable
private fun SearchStateContent(
    state: SearchUiState,
    onOpenRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        SearchUiState.Idle -> SearchGuideContent(stringResource(R.string.search_guide_idle), modifier)
        is SearchUiState.InvalidQuery -> SearchGuideContent(stringResource(R.string.search_guide_invalid), modifier)
        SearchUiState.Loading ->
            Column(
                modifier = modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        is SearchUiState.Empty -> SearchGuideContent(stringResource(R.string.search_empty), modifier)
        is SearchUiState.Success ->
            SearchResultList(
                records = state.records,
                onOpenRecord = onOpenRecord,
                modifier = modifier,
            )
        is SearchUiState.Error -> DDErrorContent(state.message, modifier = modifier)
    }
}

@Composable
private fun SearchGuideContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchResultList(
    records: List<DrinkRecord>,
    onOpenRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = TopLevelBottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(records, key = { it.id }) { record ->
            DDDrinkRecordListItem(
                record = record,
                onClick = { onOpenRecord(record.id) },
            )
        }
    }
}
