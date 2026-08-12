package com.bluemarlin.drinkdiary.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 파일 쓰기 로직은 실제 구현 시 더 정교하게 처리해야 함 (ViewModel 이벤트를 통한 UI 사이드 이펙트)
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Exported -> {
                    // 여기서는 단순히 공유 시트를 띄우는 방식으로 대체 (SAF 구현은 복잡하므로 MVP 수준)
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TEXT, event.csvContent)
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.settings_export_subject))
                        }
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.settings_export_chooser)),
                    )
                }
                is SettingsEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    DDScreenScaffold(
        title = stringResource(R.string.settings_title),
        screenType = DDScreenType.Detail,
        onBackClick = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item {
                ProStatusSection(
                    isPro = state.isProUser,
                    onUpgradeClick = { viewModel.toggleProStatus() }, // 테스트를 위해 토글로 구현
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SettingsHeader(stringResource(R.string.settings_data_management))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_export_csv),
                    subtitle = stringResource(R.string.settings_export_csv_description),
                    icon = Icons.Default.Share,
                    onClick = { viewModel.exportToCsv() },
                    enabled = state.isProUser,
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SettingsHeader(stringResource(R.string.settings_app_info))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_version),
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = {},
                )
            }

            item {
                // 개발용 Pro 상태 토글 (숨김 기능처럼 하단에 배치)
                Text(
                    stringResource(R.string.settings_debug_toggle_pro),
                    style = MaterialTheme.typography.labelSmall,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleProStatus() }
                            .padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun ProStatusSection(
    isPro: Boolean,
    onUpgradeClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isPro) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.settings_pro_activated), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.settings_pro_activated_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(stringResource(R.string.settings_pro_upsell), style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    stringResource(R.string.settings_pro_upsell_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DDPrimaryButton(
                    text = stringResource(R.string.settings_pro_upgrade_button),
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val contentColor =
        if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ListItem(
        headlineContent = {
            Text(title, color = contentColor)
        },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent =
            if (!enabled) {
                {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = stringResource(R.string.settings_lock_content_description),
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                null
            },
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    )
}
