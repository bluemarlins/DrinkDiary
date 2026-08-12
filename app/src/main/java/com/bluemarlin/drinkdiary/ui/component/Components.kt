package com.bluemarlin.drinkdiary.ui.component

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingCriterion
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.MonthlyInsight
import com.bluemarlin.drinkdiary.domain.model.PriceBracketInsight
import com.bluemarlin.drinkdiary.domain.model.roundToHalf
import com.bluemarlin.drinkdiary.domain.model.roundToTenth
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryThemeTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private const val MinOverallRating = 0.0
private const val MaxRating = 5.0
private const val OverallRatingSliderSteps = 49
private const val SensoryMetricSliderSteps = 9

@Composable
fun DDPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDContainedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun DDAddRecordFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
    ) {
        Text("+", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun DDLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun DDEmptyContent(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        DDPrimaryButton(text = actionText, onClick = onAction)
    }
}

@Composable
fun DDErrorContent(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        if (onRetry != null) {
            DDSecondaryButton(text = stringResource(R.string.retry), onClick = onRetry)
        }
    }
}

@Composable
fun DDConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { DDDestructiveButton(text = stringResource(R.string.delete), onClick = onConfirm) },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun DDProUpgradeDialog(
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pro_upgrade_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.pro_limit_message))
                Text(stringResource(R.string.pro_upgrade_description))
            }
        },
        confirmButton = {
            DDPrimaryButton(text = stringResource(R.string.pro_upgrade_button), onClick = onUpgradeClick)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.later)) }
        },
    )
}

@Composable
fun DDProLockOverlay(
    message: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🔒",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            DDPrimaryButton(text = stringResource(R.string.pro_benefits_button), onClick = onUpgradeClick)
        }
    }
}

@Composable
fun DDTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
    )
}

@Composable
fun DDNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.filter { it.isDigit() }) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
    )
}

@Composable
fun DDMultilineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        minLines = 3,
    )
}

@Composable
fun DDDateTimeField(
    label: String,
    valueMillis: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(valueMillis).atZone(zone).toLocalDate()
    val showPicker = {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedMillis =
                    java.time.LocalDate
                        .of(year, month + 1, dayOfMonth)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()
                onValueChange(selectedMillis)
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth,
        ).show()
    }

    Box(modifier = modifier.fillMaxWidth().clickable(onClick = showPicker)) {
        OutlinedTextField(
            value = formatRecordedDate(valueMillis),
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
        )
    }
}

@Composable
fun DDFormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
fun DDFormErrorText(
    error: String?,
    modifier: Modifier = Modifier,
) {
    if (error != null) {
        Text(
            text = error,
            modifier = modifier,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun DDImagePicker(
    imageUri: String?,
    onImageSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri == null) {
                onImageSelected(null)
            } else {
                coroutineScope.launch {
                    val storedUri = copyImageToInternalStorage(context, uri)
                    onImageSelected(storedUri)
                }
            }
        }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (imageUri != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val imageWidth = maxWidth * 0.8f
                val imageHeight = (imageWidth * 4f / 3f).coerceAtMost(420.dp)
                DDUriImage(
                    imageUri = imageUri,
                    contentDescription = stringResource(R.string.editor_select_photo),
                    modifier =
                        Modifier
                            .width(imageWidth)
                            .height(imageHeight)
                            .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDSecondaryButton(
                text =
                    if (imageUri ==
                        null
                    ) {
                        stringResource(R.string.editor_select_photo)
                    } else {
                        stringResource(R.string.editor_change_photo)
                    },
                onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )
            if (imageUri != null) {
                DDDestructiveButton(
                    text = stringResource(R.string.editor_remove_photo),
                    onClick = { onImageSelected(null) },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private suspend fun copyImageToInternalStorage(
    context: Context,
    sourceUri: Uri,
): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val imageDir = File(context.filesDir, "drink_record_images").apply { mkdirs() }
            val imageFile = File(imageDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@runCatching null
            Uri.fromFile(imageFile).toString()
        }.getOrNull()
    }

@Composable
fun DDRatingInput(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    error: String? = null,
    enabled: Boolean = true,
) {
    val sliderValue = rating.coerceIn(MinOverallRating, MaxRating)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.component_rating_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.width(48.dp),
            )
            Slider(
                value = sliderValue.toFloat(),
                onValueChange = { onRatingChange(roundToTenth(it.toDouble())) },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                valueRange = MinOverallRating.toFloat()..MaxRating.toFloat(),
                steps = OverallRatingSliderSteps,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.component_rating_bad),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.component_rating_good),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (error !=
            null
        ) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DDSensoryMetricSlider(
    criterion: DrinkRatingCriterion,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val roundedValue = roundToHalf(value).coerceIn(MinOverallRating, MaxRating)
    val currentLabel =
        when {
            roundedValue <= 1.0 -> stringResource(criterion.minLabelRes)
            roundedValue < 2.5 ->
                stringResource(
                    R.string.criterion_label_direction_format,
                    stringResource(criterion.minLabelRes),
                )
            roundedValue <= 3.0 -> stringResource(R.string.criterion_label_middle)
            roundedValue < 4.5 ->
                stringResource(
                    R.string.criterion_label_direction_format,
                    stringResource(criterion.maxLabelRes),
                )
            else -> stringResource(criterion.maxLabelRes)
        }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(criterion.labelRes), style = MaterialTheme.typography.titleSmall)
            Text(
                text = "$currentLabel · %.1f".format(roundedValue),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(criterion.descriptionRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = roundedValue.toFloat(),
            onValueChange = { onValueChange(roundToHalf(it.toDouble())) },
            valueRange = MinOverallRating.toFloat()..MaxRating.toFloat(),
            steps = SensoryMetricSliderSteps,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(criterion.minLabelRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(criterion.maxLabelRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDDrinkTypeSelector(
    selected: DrinkType?,
    onSelected: (DrinkType) -> Unit,
    error: String? = null,
) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (useDropdown) {
            DDOptionDropdown(
                label = stringResource(R.string.component_drink_type),
                selectedLabel =
                    selected?.let { stringResource(it.labelRes) } ?: stringResource(R.string.component_select),
                options = DrinkType.entries.map { it to stringResource(it.labelRes) },
                onSelected = onSelected,
            )
        } else {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DrinkType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = DrinkType.entries.size),
                    ) { Text(stringResource(type.labelRes), maxLines = 1, overflow = TextOverflow.Clip) }
                }
            }
        }
        if (error !=
            null
        ) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDCollectionStatusSelector(
    selected: CollectionStatus?,
    onSelected: (CollectionStatus) -> Unit,
    error: String? = null,
) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (useDropdown) {
            DDOptionDropdown(
                label = stringResource(R.string.component_collection_status),
                selectedLabel =
                    selected?.let { stringResource(it.labelRes) } ?: stringResource(R.string.component_select),
                options = CollectionStatus.entries.map { it to stringResource(it.labelRes) },
                onSelected = onSelected,
            )
        } else {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CollectionStatus.entries.forEachIndexed { index, status ->
                    SegmentedButton(
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CollectionStatus.entries.size),
                    ) { Text(stringResource(status.labelRes), maxLines = 1, overflow = TextOverflow.Clip) }
                }
            }
        }
        if (error !=
            null
        ) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDPeriodSegmentedControl(
    selected: DashboardPeriod,
    onSelected: (DashboardPeriod) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        DashboardPeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onSelected(period) },
                modifier = Modifier.weight(1f),
                shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardPeriod.entries.size),
            ) { Text(stringResource(period.labelRes), maxLines = 1, overflow = TextOverflow.Clip) }
        }
    }
}

@Composable
fun DDDrinkTypeFilter(
    selected: DrinkType?,
    onSelected: (DrinkType?) -> Unit,
) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (useDropdown) {
        DDOptionDropdown(
            label = stringResource(R.string.component_drink_type),
            selectedLabel = selected?.let { stringResource(it.labelRes) } ?: stringResource(R.string.component_all),
            options =
                listOf<DrinkType?>(null).map { it to stringResource(R.string.component_all) } +
                    DrinkType.entries.map { it to stringResource(it.labelRes) },
            onSelected = onSelected,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = selected == null, onClick = {
                    onSelected(null)
                }, label = { Text(stringResource(R.string.component_all)) })
            }
            DrinkType.entries.forEach { type ->
                item {
                    FilterChip(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        label = { Text(stringResource(type.labelRes)) },
                    )
                }
            }
        }
    }
}

@Composable
fun DDCollectionStatusFilter(
    selected: CollectionStatus?,
    onSelected: (CollectionStatus?) -> Unit,
) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (useDropdown) {
        DDOptionDropdown(
            label = stringResource(R.string.component_collection_status),
            selectedLabel = selected?.let { stringResource(it.labelRes) } ?: stringResource(R.string.component_all),
            options =
                listOf<CollectionStatus?>(null).map { it to stringResource(R.string.component_all) } +
                    CollectionStatus.entries.map { it to stringResource(it.labelRes) },
            onSelected = onSelected,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = selected == null, onClick = {
                    onSelected(null)
                }, label = { Text(stringResource(R.string.component_all)) })
            }
            CollectionStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        label = { Text(stringResource(status.labelRes)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> DDOptionDropdown(
    label: String,
    selectedLabel: String,
    options: List<Pair<T, String>>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$label: $selectedLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("▼")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    },
                )
            }
        }
    }
}

@Composable
fun DDDrinkTypeBadge(type: DrinkType) {
    AssistChip(onClick = {}, label = { Text(stringResource(type.labelRes)) })
}

@Composable
fun DDCollectionStatusBadge(status: CollectionStatus) {
    AssistChip(onClick = {}, label = { Text(stringResource(status.labelRes)) })
}

@Composable
fun DDRatingStars(rating: Double) {
    Text(ratingStarsText(rating), color = MaterialTheme.colorScheme.primary)
}

@Composable
fun DDRatingValueText(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "%.1f".format(rating),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
fun DDRatingBreakdownRadarChart(
    criteria: List<DrinkRatingCriterion>,
    breakdown: DrinkRatingBreakdown,
    modifier: Modifier = Modifier,
) {
    val visibleCriteria = criteria.take(5)
    val values = breakdown.values.take(visibleCriteria.size)
    if (visibleCriteria.isEmpty() || visibleCriteria.size != values.size) return

    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant
    val axis = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.component_tasting_profile), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.component_profile_supporting),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier.size(300.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                val angles =
                    visibleCriteria.indices.map { index ->
                        -90.0 + (360.0 / visibleCriteria.size) * index
                    }
                Canvas(modifier = Modifier.size(210.dp)) {
                    val center = this.center
                    val radius = size.minDimension * 0.42f

                    fun pointFor(
                        angleDegrees: Double,
                        ratio: Double,
                    ): androidx.compose.ui.geometry.Offset {
                        val radians = Math.toRadians(angleDegrees)
                        return androidx.compose.ui.geometry.Offset(
                            x = center.x + kotlin.math.cos(radians).toFloat() * radius * ratio.toFloat(),
                            y = center.y + kotlin.math.sin(radians).toFloat() * radius * ratio.toFloat(),
                        )
                    }

                    for (level in 1..5) {
                        val ratio = level / 5.0
                        val gridPath = Path()
                        angles.forEachIndexed { index, angle ->
                            val point = pointFor(angle, ratio)
                            if (index == 0) gridPath.moveTo(point.x, point.y) else gridPath.lineTo(point.x, point.y)
                        }
                        gridPath.close()
                        drawPath(gridPath, color = outline, style = Stroke(width = 1.dp.toPx()))
                    }

                    angles.forEach { angle ->
                        val end = pointFor(angle, 1.0)
                        drawLine(color = axis, start = center, end = end, strokeWidth = 1.dp.toPx())
                    }

                    val profilePath = Path()
                    values.forEachIndexed { index, value ->
                        val point = pointFor(angles[index], (value / MaxRating).coerceIn(0.0, 1.0))
                        if (index == 0) profilePath.moveTo(point.x, point.y) else profilePath.lineTo(point.x, point.y)
                    }
                    profilePath.close()
                    drawPath(profilePath, color = primary.copy(alpha = 0.24f))
                    drawPath(profilePath, color = primary, style = Stroke(width = 2.dp.toPx()))
                }
                visibleCriteria.forEachIndexed { index, criterion ->
                    val angle = Math.toRadians(angles[index])
                    val xOffset = (kotlin.math.cos(angle).toFloat() * 112f).dp
                    val yOffset = (kotlin.math.sin(angle).toFloat() * 112f).dp
                    RadarAxisBadge(
                        text = stringResource(criterion.labelRes),
                        value = values[criterion.index],
                        modifier = Modifier.align(Alignment.Center).offset(x = xOffset, y = yOffset),
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarAxisBadge(
    text: String,
    value: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(76.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "%.1f".format(value),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun ratingStarsText(rating: Double): String {
    val normalized = roundToHalf(rating).coerceIn(0.0, 5.0)
    val fullStars = normalized.toInt()
    val hasHalf = normalized - fullStars == 0.5
    val emptyStars = 5 - fullStars - if (hasHalf) 1 else 0
    return buildString {
        repeat(fullStars) { append("★") }
        if (hasHalf) append("½")
        repeat(emptyStars) { append("☆") }
    }
}

@Composable
fun DDPriceText(
    price: Long?,
    modifier: Modifier = Modifier,
) {
    Text(text = formatPrice(price), modifier = modifier)
}

@Composable
fun DDRecordedDateText(
    recordedAtMillis: Long,
    modifier: Modifier = Modifier,
) {
    Text(text = formatRecordedDate(recordedAtMillis), modifier = modifier)
}

@Composable
fun DDDrinkRecordListItem(
    record: DrinkRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDImageThumbnail(record.imageUri)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    record.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(record.type.labelRes), style = MaterialTheme.typography.bodySmall)
                    DDRatingValueText(record.rating)
                }
                Text(
                    "${stringResource(
                        record.collectionStatus.labelRes,
                    )} · ${formatRecordedDate(record.recordedAtMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun DDDrinkRecordCard(
    record: DrinkRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.name, style = MaterialTheme.typography.titleSmall)
            Text(
                "${stringResource(record.type.labelRes)} · ${stringResource(record.collectionStatus.labelRes)}",
                style = MaterialTheme.typography.bodySmall,
            )
            DDRatingValueText(record.rating)
        }
    }
}

@Composable
fun DDDashboardMetricTile(
    title: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 108.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun DDDrinkTypeDonutCard(
    wineCount: Int,
    whiskeyCount: Int,
    beerCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val chartColors = DrinkDiaryThemeTokens.chartColors
    val entries =
        listOf(
            Triple(stringResource(R.string.drink_type_wine), wineCount, chartColors.wine),
            Triple(stringResource(R.string.drink_type_whiskey), whiskeyCount, chartColors.whiskey),
            Triple(stringResource(R.string.drink_type_beer), beerCount, chartColors.beer),
        )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    if (totalCount == 0) {
                        drawArc(
                            color = outlineColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    } else {
                        var startAngle = -90f
                        entries.forEach { (_, count, color) ->
                            val sweep = (count.toFloat() / totalCount.toFloat()) * 360f
                            if (sweep > 0f) {
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                )
                            }
                            startAngle += sweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.dashboard_metric_record_unit, totalCount),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.component_all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.component_type_proportion), style = MaterialTheme.typography.titleMedium)
                entries.forEach { (label, count, color) ->
                    val ratio = if (totalCount == 0) 0 else (count * 100) / totalCount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "$ratio% · ${stringResource(R.string.dashboard_metric_record_unit, count)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DDImageThumbnail(imageUri: String?) {
    if (imageUri == null) {
        Box(
            modifier =
                Modifier
                    .size(
                        56.dp,
                    ).clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.component_photo_thumbnail), style = MaterialTheme.typography.labelSmall)
        }
    } else {
        DDUriImage(
            imageUri = imageUri,
            contentDescription = stringResource(R.string.component_photo_thumbnail),
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun DDInfoRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
fun DDRecordHeroImage(
    imageUri: String?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val imageWidth = maxWidth * 0.8f
        val imageHeight = (imageWidth * 4f / 3f).coerceAtMost(520.dp)
        if (imageUri == null) {
            Box(
                modifier =
                    Modifier
                        .width(imageWidth)
                        .height(220.dp.coerceAtMost(imageHeight))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.component_no_photo))
            }
        } else {
            DDUriImage(
                imageUri = imageUri,
                contentDescription = stringResource(R.string.component_main_photo),
                modifier =
                    Modifier
                        .width(imageWidth)
                        .height(imageHeight)
                        .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun DDUriImage(
    imageUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageUri) {
        bitmap =
            withContext(Dispatchers.IO) {
                runCatching {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(imageUri))
                    ImageDecoder.decodeBitmap(source).asImageBitmap()
                }.getOrNull()
            }
    }

    if (bitmap == null) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.component_photo_thumbnail), style = MaterialTheme.typography.labelSmall)
        }
    } else {
        Image(
            bitmap = requireNotNull(bitmap),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

@Composable
fun DDDashboardSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier =
            modifier.fillMaxWidth().then(
                if (onClick !=
                    null
                ) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun DDStatusSummaryCard(
    status: CollectionStatus,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    DDDashboardSummaryCard(
        title = stringResource(status.labelRes),
        value = stringResource(R.string.dashboard_metric_record_unit, count),
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun DDDrinkTypeRatioCard(
    wineCount: Int,
    whiskeyCount: Int,
    beerCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val value =
        if (totalCount == 0) {
            stringResource(
                R.string.drink_type_wine,
            ) + " 0% · " + stringResource(R.string.drink_type_whiskey) + " 0% · " +
                stringResource(R.string.drink_type_beer) +
                " 0%"
        } else {
            fun ratio(count: Int): Int = (count * 100) / totalCount
            stringResource(R.string.drink_type_wine) + " ${ratio(wineCount)}% · " +
                stringResource(R.string.drink_type_whiskey) +
                " ${ratio(whiskeyCount)}% · " +
                stringResource(R.string.drink_type_beer) +
                " ${ratio(beerCount)}%"
        }
    DDDashboardSummaryCard(
        title = stringResource(R.string.component_type_proportion),
        value = value,
        modifier = modifier,
    )
}

fun formatRecordedDate(millis: Long): String =
    Instant
        .ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

@Composable
fun formatPrice(price: Long?): String =
    price?.let {
        NumberFormat.getNumberInstance(Locale.getDefault()).format(it) +
            stringResource(R.string.component_currency_unit)
    }
        ?: "-"

@Composable
fun DDMonthlyTrendCard(
    monthlyTrend: List<MonthlyInsight>,
    modifier: Modifier = Modifier,
) {
    val isEmpty = monthlyTrend.isEmpty() || monthlyTrend.all { it.totalCount == 0 }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.insights_taste_trend), style = MaterialTheme.typography.titleMedium)
            if (isEmpty) {
                Text(
                    text = stringResource(R.string.insights_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    monthlyTrend.forEach { insight ->
                        val ratingText = insight.averageRating?.let { "%.1f".format(it) } ?: "-"
                        val repurchaseText =
                            insight.repurchaseRate?.let {
                                stringResource(
                                    R.string.insights_repurchase_rate_format,
                                    "%.0f%%".format(
                                        it * 100,
                                    ),
                                )
                            }
                                ?: "-"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = insight.yearMonthLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(72.dp),
                            )
                            Row(
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    val ratingRatio =
                                        ((insight.averageRating ?: 0.0) / 5.0)
                                            .coerceIn(
                                                0.0,
                                                1.0,
                                            ).toFloat()
                                    if (ratingRatio > 0f) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(ratingRatio)
                                                    .fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.primary),
                                        )
                                    }
                                }
                                Text(
                                    text = "★ $ratingText",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                text = "재구매 $repurchaseText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DDPriceBracketCard(
    priceBrackets: List<PriceBracketInsight>,
    modifier: Modifier = Modifier,
) {
    val isEmpty = priceBrackets.isEmpty() || priceBrackets.all { it.count == 0 }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.insights_price_bracket), style = MaterialTheme.typography.titleMedium)
            if (isEmpty) {
                Text(
                    text = stringResource(R.string.insights_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    priceBrackets.forEach { insight ->
                        val ratingText = insight.averageRating?.let { "%.1f".format(it) } ?: "-"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = insight.bracket.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.component_price_count_format, insight.count),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "★ $ratingText",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
