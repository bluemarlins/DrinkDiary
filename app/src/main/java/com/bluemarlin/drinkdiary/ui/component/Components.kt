package com.bluemarlin.drinkdiary.ui.component

import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DashboardSummary
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import com.bluemarlin.drinkdiary.domain.model.findTastingTag
import com.bluemarlin.drinkdiary.domain.model.tagCategoryLabel
import com.bluemarlin.drinkdiary.domain.model.tastingTags
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.bluemarlin.drinkdiary.domain.model.roundToHalf
import java.text.NumberFormat
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DDPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    // Gold, not the M3 default `primary` (Cellar Green): the dark-moody pivot already
    // established gold as the "must-pop CTA" color (FAB, rebuy badge) — a pale mint
    // green button read as a leftover pre-pivot color against the gold/rose palette.
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    ) { Text(text) }
}

@Composable
fun DDSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    // Gold outline, not the M3 default `primary` (Cellar Green) — matches this app's
    // established rule that Gold carries every interactive/CTA accent, Green is
    // reserved for brand identity and drink-type badges only.
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
    ) { Text(text) }
}

@Composable
fun DDContainedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = RoundedCornerShape(8.dp)) { Text(text) }
}

@Composable
fun DDDestructiveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) {
        Text(text)
    }
}

@Composable
fun DDAddRecordFab(onClick: () -> Unit) {
    // Explicit gold fill: the FAB is the primary CTA and must be the most visually
    // prominent element on screen — the default primaryContainer (green) blends into
    // this app's green-toned dark background instead of standing out.
    // Extra bottom offset: on screens with the AdMob banner in the bottom bar, the
    // Scaffold's default FAB margin leaves it almost touching the banner's top edge.
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.offset(y = (-12).dp),
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
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
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(y = (-40).dp)
                .blur(60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            DDPrimaryButton(text = actionText, onClick = onAction)
            if (secondaryActionText != null && onSecondaryAction != null) {
                DDSecondaryButton(text = secondaryActionText, onClick = onSecondaryAction)
            }
        }
    }
}

@Composable
fun DDErrorContent(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        if (onRetry != null) {
            DDSecondaryButton(text = "다시 시도", onClick = onRetry)
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
        confirmButton = { DDDestructiveButton(text = "삭제", onClick = onConfirm) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        shape = RoundedCornerShape(8.dp),
    )
}

// Gold focus border/label/cursor, not the M3 default `primary` (Cellar Green) —
// same "Gold=interactive accent" rule applied to buttons/chips/rating input.
@Composable
private fun ddTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    focusedLabelColor = MaterialTheme.colorScheme.secondary,
    cursorColor = MaterialTheme.colorScheme.secondary,
)

@Composable
fun DDTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, error: String? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        colors = ddTextFieldColors(),
    )
}

@Composable
fun DDNumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, error: String? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.filter { it.isDigit() }) },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        colors = ddTextFieldColors(),
    )
}

@Composable
fun DDMultilineTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        colors = ddTextFieldColors(),
        modifier = modifier.fillMaxWidth(),
        minLines = 3,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDDateTimeField(
    label: String,
    valueMillis: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val zone = ZoneId.systemDefault()
    // Compose Material3's own DatePickerDialog, not the legacy android.app.DatePickerDialog
    // — the legacy dialog renders as a light-themed system dialog with M2 teal accents and
    // English button labels, jarring against the app's forced dark/gold theme. The M3
    // version automatically inherits DrinkDiaryTheme's color scheme.
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().clickable(onClick = { showPicker = true })) {
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

    if (showPicker) {
        // Known limitation: on a device set to a non-Korean system locale, the M3
        // DatePicker's month/weekday chrome ("August 2026", "S M T W T F S") still renders
        // in that locale — it resolves via Compose's own text.intl.Locale (tied to the
        // Activity's real Configuration), which neither `Locale.setDefault()` nor a
        // `LocalConfiguration` override reaches. Confirm/취소 and the color scheme are
        // already correctly forced Korean/dark; fully localizing the picker's internal
        // chrome would need wrapping the Activity's base context locale (attachBaseContext)
        // — out of scope for this round, tracked in service-launch-roadmap.md.
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Instant.ofEpochMilli(valueMillis).atZone(zone)
                .toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val selectedMillis = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC)
                                .toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
                            onValueChange(selectedMillis)
                        }
                        showPicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                ) { Text("취소") }
            },
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onSecondary,
                    todayDateBorderColor = MaterialTheme.colorScheme.secondary,
                ),
            )
        }
    }
}

@Composable
fun DDFormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
fun DDFormErrorText(error: String?, modifier: Modifier = Modifier) {
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
fun DDImagePicker(imageUri: String?, onImageSelected: (String?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
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
                    contentDescription = "선택한 사진",
                    modifier = Modifier
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
                text = if (imageUri == null) "사진 선택" else "사진 변경",
                onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )
            if (imageUri != null) {
                DDDestructiveButton(
                    text = "사진 제거",
                    onClick = { onImageSelected(null) },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private suspend fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? =
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            DDSecondaryButton(
                text = "-",
                onClick = { onRatingChange((rating - 0.5).coerceAtLeast(0.5)) },
                enabled = enabled && rating > 0.5,
            )
            Text(
                text = ratingStarsText(rating),
                style = MaterialTheme.typography.headlineSmall,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    // Echoes the error color on the glyphs themselves, not just the
                    // caption below — same signal DDTextField gives via its border.
                    error != null -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                },
            )
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.titleMedium,
            )
            DDSecondaryButton(
                text = "+",
                onClick = { onRatingChange((rating + 0.5).coerceAtMost(5.0)) },
                enabled = enabled && rating < 5.0,
            )
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDDrinkTypeSelector(selected: DrinkType?, onSelected: (DrinkType) -> Unit, error: String? = null) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (useDropdown) {
            DDOptionDropdown(
                label = "주류 종류",
                selectedLabel = selected?.label ?: "선택",
                options = DrinkType.entries.map { it to it.label },
                onSelected = onSelected,
            )
        } else {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().then(ddSelectorErrorModifier(error)),
            ) {
                DrinkType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text(type.label, maxLines = 1, overflow = TextOverflow.Clip) }
                }
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

// Shared visual error affordance for selector-style controls (segmented rows) that
// have no built-in error state of their own — a border echoes the same signal
// DDTextField gives via its border, so error controls aren't only distinguishable
// by the caption text below them.
@Composable
private fun ddSelectorErrorModifier(error: String?): Modifier =
    if (error != null) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp)).padding(2.dp)
    } else {
        Modifier
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDCollectionStatusSelector(selected: CollectionStatus?, onSelected: (CollectionStatus) -> Unit, error: String? = null) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (useDropdown) {
            DDOptionDropdown(
                label = "컬렉션 상태",
                selectedLabel = selected?.label ?: "선택",
                options = CollectionStatus.entries.map { it to it.label },
                onSelected = onSelected,
            )
        } else {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().then(ddSelectorErrorModifier(error)),
            ) {
                CollectionStatus.entries.forEachIndexed { index, status ->
                    SegmentedButton(
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        // Rose for 비선호, matching DDCollectionStatusBadge's established
                        // Wish(Gold)/Pass(Rose) color language — the M3 default would
                        // render every selected chip in the same gold tone.
                        colors = if (status == CollectionStatus.NotForMe) {
                            SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        } else {
                            SegmentedButtonDefaults.colors()
                        },
                    ) { Text(status.label, maxLines = 1, overflow = TextOverflow.Clip) }
                }
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDPeriodSegmentedControl(selected: DashboardPeriod, onSelected: (DashboardPeriod) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        DashboardPeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onSelected(period) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) { Text(period.label, maxLines = 1, overflow = TextOverflow.Clip) }
        }
    }
}

@Composable
fun DDThemeModeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) { Text(mode.label, maxLines = 1, overflow = TextOverflow.Clip) }
        }
    }
}

// Month-grid calendar tied to the period selector above. No calendar library dependency
// (none exists in this project) — a plain Row/Column grid over java.time, same pattern
// already used for other custom layouts (e.g. DDDrinkTypeRatioCard). Monday-first columns
// (not the usual Korean Sunday-first convention) so the Weekly highlight below matches
// ObserveDashboardSummaryUseCase's Monday-Sunday week definition as a single contiguous
// row, rather than splitting across two visual rows.
@Composable
fun DDDashboardCalendar(
    period: DashboardPeriod,
    recordDates: Set<LocalDate>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    val yearMonth = remember { YearMonth.from(today) }
    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOffset = firstOfMonth.dayOfWeek.value - 1 // Monday=1..Sunday=7 -> 0..6
    val totalWeeks = (firstDayOffset + daysInMonth + 6) / 7
    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val currentWeekEnd = currentWeekStart.plusDays(6)

    Card(
        modifier = modifier.fillMaxWidth().then(ddGlassBorderModifier()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${yearMonth.year}년 ${yearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    )
                    if (!expanded) {
                        Text(
                            "이번 달 기록 ${recordDates.size}일",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(
                    onClick = onToggleExpanded,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                ) {
                    Text(if (expanded) "접기 ▲" else "펼치기 ▼")
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("월", "화", "수", "목", "금", "토", "일").forEach { label ->
                            Text(
                                label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    for (week in 0 until totalWeeks) {
                        val weekDates = (0..6).map { dayOfWeekIndex ->
                            val dayNumber = week * 7 + dayOfWeekIndex - firstDayOffset + 1
                            if (dayNumber in 1..daysInMonth) yearMonth.atDay(dayNumber) else null
                        }
                        val highlightRow = period == DashboardPeriod.Weekly &&
                            weekDates.any { it != null && !it.isBefore(currentWeekStart) && !it.isAfter(currentWeekEnd) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (highlightRow) {
                                        Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f))
                                    } else {
                                        Modifier
                                    },
                                )
                                .padding(vertical = 4.dp),
                        ) {
                            weekDates.forEach { date ->
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                    if (date != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .then(
                                                        if (date == today) {
                                                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                                        } else {
                                                            Modifier
                                                        },
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (date in recordDates) {
                                                            MaterialTheme.colorScheme.secondary
                                                        } else {
                                                            Color.Transparent
                                                        },
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DDDrinkTypeFilter(selected: DrinkType?, onSelected: (DrinkType?) -> Unit) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (useDropdown) {
        DDOptionDropdown(
            label = "주류 종류",
            selectedLabel = selected?.label ?: "전체",
            options = listOf<DrinkType?>(null).map { it to "전체" } + DrinkType.entries.map { it to it.label },
            onSelected = onSelected,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelected(null) },
                    label = { Text("전체") },
                    leadingIcon = ddFilterChipLeadingIcon(selected == null),
                    colors = ddFilterChipColors(),
                )
            }
            DrinkType.entries.forEach { type ->
                item {
                    FilterChip(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        label = { Text(type.label) },
                        leadingIcon = ddFilterChipLeadingIcon(selected == type),
                        colors = ddFilterChipColors(),
                    )
                }
            }
        }
    }
}

// Solid gold, not `primaryContainer` (green) — a green selected-chip fill reads at
// ~1.7:1 contrast against this app's green-toned dark background (measured in the
// Collection UI quality loop), barely distinguishable from the unselected state.
@Composable
private fun ddFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.secondary,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
    // Without this, Compose falls back to its own default leading-icon tint instead
    // of matching the deliberately-chosen label color, leaving the checkmark washed
    // out against the gold fill (~1.4:1 contrast, found in the Collection UI loop).
    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
)

private fun ddFilterChipLeadingIcon(selected: Boolean): @Composable (() -> Unit)? =
    if (selected) {
        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
    } else {
        null
    }

/**
 * Multi-select tasting tag picker — the app's only multi-select chip surface; every other
 * `FilterChip` here picks exactly one value.
 *
 * Uses `FlowRow` rather than the horizontally-scrolling `LazyRow` the filters use, because a
 * type offers 30-40 tags and side-scrolling through them hides most of the vocabulary. Only
 * the beginner-friendly tags show until the user expands, so the default state stays short
 * enough not to bury the rest of the form.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DDTastingTagPicker(
    type: DrinkType?,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (type == null) {
        Text(
            "주류 종류를 먼저 선택하면 태그를 고를 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    val catalog = remember(type) { type.tastingTags() }
    // Tags the user typed in themselves, or picked under a different drink type before
    // switching — keep showing them so switching type never silently drops a selection.
    val customTags = remember(selected, catalog) {
        selected.filter { key -> catalog.none { it.key == key } }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TagCategory.entries.forEach { category ->
            val all = catalog.filter { it.category == category }
            if (all.isEmpty()) return@forEach
            // Anything already chosen stays visible when collapsed, otherwise collapsing
            // would hide a selection the user just made.
            val visible = if (expanded) all else all.filter { it.isBasic || it.key in selected }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    type.tagCategoryLabel(category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    visible.forEach { tag ->
                        FilterChip(
                            selected = tag.key in selected,
                            onClick = { onToggle(tag.key) },
                            label = { Text(tag.label) },
                            leadingIcon = ddFilterChipLeadingIcon(tag.key in selected),
                            colors = ddFilterChipColors(),
                        )
                    }
                }
            }
        }

        if (customTags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "직접 추가",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    customTags.forEach { key ->
                        FilterChip(
                            selected = true,
                            onClick = { onToggle(key) },
                            label = { Text(findTastingTag(key)?.label ?: key) },
                            leadingIcon = ddFilterChipLeadingIcon(true),
                            colors = ddFilterChipColors(),
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it },
                label = { Text("직접 입력") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    val text = customText.trim()
                    if (text.isNotEmpty()) {
                        onToggle(text)
                        customText = ""
                    }
                },
                enabled = customText.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) { Text("추가") }
        }

        TextButton(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        ) {
            Text(if (expanded) "접기 ▲" else "더보기 ▼")
        }
    }
}

/** Read-only tag display for the detail screen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DDTastingTagList(tags: List<String>, modifier: Modifier = Modifier) {
    if (tags.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { key ->
            AssistChip(
                onClick = {},
                label = { Text(findTastingTag(key)?.label ?: key) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                    labelColor = MaterialTheme.colorScheme.secondary,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
            )
        }
    }
}

@Composable
fun DDCollectionStatusFilter(selected: CollectionStatus?, onSelected: (CollectionStatus?) -> Unit) {
    val useDropdown = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (useDropdown) {
        DDOptionDropdown(
            label = "컬렉션 상태",
            selectedLabel = selected?.label ?: "전체",
            options = listOf<CollectionStatus?>(null).map { it to "전체" } + CollectionStatus.entries.map { it to it.label },
            onSelected = onSelected,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelected(null) },
                    label = { Text("전체") },
                    leadingIcon = ddFilterChipLeadingIcon(selected == null),
                    colors = ddFilterChipColors(),
                )
            }
            CollectionStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        label = { Text(status.label) },
                        leadingIcon = ddFilterChipLeadingIcon(selected == status),
                        colors = ddStatusFilterChipColors(status),
                    )
                }
            }
        }
    }
}

@Composable
private fun ddStatusFilterChipColors(status: CollectionStatus) = when (status) {
    CollectionStatus.Repurchase -> FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.secondary,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
    )
    CollectionStatus.NotForMe -> FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
    CollectionStatus.Normal -> FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        ) {
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
    AssistChip(onClick = {}, label = { Text(type.label) })
}

@Composable
fun DDCollectionStatusBadge(status: CollectionStatus) {
    when (status) {
        CollectionStatus.Repurchase -> AssistChip(
            onClick = {},
            label = { Text(status.label) },
            colors = AssistChipDefaults.assistChipColors(
                // Gold, not green — a green badge on this app's green-toned dark
                // background barely reads as an accent. Translucent fill + gold
                // border/text (not a solid `secondary` fill) so a static status
                // badge doesn't read as a second tappable CTA next to real gold
                // buttons like RecordDetail's 수정 — the gold hue still matches
                // the FAB/button family, just at lower visual weight.
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                labelColor = MaterialTheme.colorScheme.secondary,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        )
        CollectionStatus.NotForMe -> AssistChip(
            onClick = {},
            label = { Text(status.label) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            border = null,
        )
        CollectionStatus.Normal -> AssistChip(
            onClick = {},
            label = { Text(status.label) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = null,
        )
    }
}

@Composable
fun DDRatingStars(rating: Double) {
    // Gold, not the M3 default `primary` (Cellar Green) — same leftover pre-pivot
    // hue clash already fixed for DDPrimaryButton; stars are an accent, not a
    // brand-identity element, so they follow the established gold accent color.
    Text(ratingStarsText(rating), color = MaterialTheme.colorScheme.secondary)
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
fun DDPriceText(price: Long?, modifier: Modifier = Modifier) {
    Text(text = formatPrice(price), modifier = modifier)
}

@Composable
fun DDRecordedDateText(recordedAtMillis: Long, modifier: Modifier = Modifier) {
    Text(text = formatRecordedDate(recordedAtMillis), modifier = modifier)
}

@Composable
fun DDDrinkRecordListItem(record: DrinkRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).then(ddGlassBorderModifier()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDImageThumbnail(record.imageUri)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        record.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (record.price != null) {
                        DDPriceText(record.price, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    DDDrinkTypeBadge(record.type)
                    DDCollectionStatusBadge(record.collectionStatus)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DDRatingStars(record.rating)
                    Text(formatRecordedDate(record.recordedAtMillis), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun DDDrinkRecordCard(record: DrinkRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).then(ddGlassBorderModifier()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDImageThumbnail(record.imageUri)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(record.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    DDDrinkTypeBadge(record.type)
                    DDCollectionStatusBadge(record.collectionStatus)
                }
                DDRatingStars(record.rating)
            }
        }
    }
}

@Composable
fun DDImageThumbnail(imageUri: String?) {
    if (imageUri == null) {
        Box(
            modifier = Modifier.size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("사진", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        DDUriImage(
            imageUri = imageUri,
            contentDescription = "기록 사진",
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun DDInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
fun DDRecordHeroImage(imageUri: String?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val imageWidth = maxWidth * 0.8f
        val imageHeight = (imageWidth * 4f / 3f).coerceAtMost(520.dp)
        if (imageUri == null) {
            // No photo — a flat placeholder box read as an unfinished dev state, so this
            // is a mesh-style gradient (brand gold-to-rose) with the app's own icon glyph
            // as a large translucent watermark instead. Reuses the existing launcher
            // foreground asset, no new image asset needed.
            // See app/docs/design/research-immersive-ui.md section 5.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                    alpha = 0.28f,
                    // Tint to a single flat tone instead of the glyph's full original
                    // hues — a faded but still multi-color icon read as a translucent
                    // sticker competing with the gradient, not a brand watermark texture.
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                )
                Text(
                    "등록된 사진 없음",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                )
            }
        } else {
            DDUriImage(
                imageUri = imageUri,
                contentDescription = "기록 대표 사진",
                modifier = Modifier
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
        bitmap = withContext(Dispatchers.IO) {
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
            Text("사진", style = MaterialTheme.typography.labelSmall)
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
fun DDDashboardSummaryCard(title: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .then(ddGlassBorderModifier()),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/**
 * Bento-style hero stat tile — full-width, gradient-washed, large serif number.
 * Used for the single most important Dashboard metric (record count) so the stats
 * area reads as an asymmetric bento grid instead of a uniform 2x2, per
 * app/docs/design/research-immersive-ui.md section 2/5.
 */
@Composable
fun DDHeroSummaryCard(title: String, value: String, caption: String? = null, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().then(ddGlassBorderModifier()),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
            )
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

/**
 * Approximated "glassmorphism" border: no real backdrop blur (would need a third-party
 * library), just a translucent container color (applied by the caller) plus a thin
 * gold-to-rose gradient stroke. See app/docs/design/research-immersive-ui.md.
 */
@Composable
fun ddGlassBorderModifier(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)): Modifier {
    val borderBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f),
        ),
    )
    return Modifier.border(1.5.dp, borderBrush, shape)
}

@Composable
fun DDStatusSummaryCard(
    status: CollectionStatus,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    DDDashboardSummaryCard(
        title = status.label,
        value = "${count}개",
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
    Card(
        modifier = modifier.fillMaxWidth().then(ddGlassBorderModifier()),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("종류별 비중", style = MaterialTheme.typography.labelLarge)
            if (totalCount == 0) {
                Text("기록이 없습니다.", style = MaterialTheme.typography.bodyMedium)
            } else {
                // Capsule track (height/2 corner radius) instead of a squared-off bar, with
                // an inline Serif percentage label per wide-enough segment and a thin gold
                // accent border on the largest-share segment — approximates a "glow" without
                // real blur/shadow per app/docs/design/design-system.md section 14.
                val maxCount = maxOf(wineCount, whiskeyCount, beerCount)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    if (wineCount > 0) {
                        DDRatioSegment(wineCount, totalCount, maxCount, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
                    }
                    if (whiskeyCount > 0) {
                        DDRatioSegment(whiskeyCount, totalCount, maxCount, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                    }
                    if (beerCount > 0) {
                        DDRatioSegment(beerCount, totalCount, maxCount, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DDRatioLegendItem("와인", wineCount, totalCount, MaterialTheme.colorScheme.tertiary)
                    DDRatioLegendItem("위스키", whiskeyCount, totalCount, MaterialTheme.colorScheme.secondary)
                    DDRatioLegendItem("맥주", beerCount, totalCount, MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun RowScope.DDRatioSegment(count: Int, total: Int, maxCount: Int, color: Color, onColor: Color) {
    val percent = (count * 100) / total
    Box(
        modifier = Modifier
            .weight(count.toFloat())
            .fillMaxHeight()
            .background(color)
            .then(
                if (count == maxCount) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (percent >= 15) {
            Text(
                "$percent%",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                color = onColor,
            )
        }
    }
}

@Composable
private fun DDRatioLegendItem(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total == 0) 0 else (count * 100) / total
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text("$label $percent%", style = MaterialTheme.typography.bodySmall)
    }
}

// Vico (com.patrykandpatrick.vico:compose-m3) bar chart — a real charting library for
// this one, since it's a genuine 8-point trend that benefits from proper entrance
// animation; DDTypeRatingComparisonCard below only has 3 values so stays plain Compose
// for visual consistency with DDDrinkTypeRatioCard instead. `rememberM3VicoTheme()` +
// `ProvideVicoTheme` pull chart colors straight from MaterialTheme.colorScheme (primary/
// secondary/tertiary), so no manual color wiring is needed. Axes are intentionally
// omitted (both default to null) for a clean sparkline/mini-chart look.
@Composable
fun DDWeeklyTrendChart(weeklyCounts: List<Int>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(weeklyCounts) {
        if (weeklyCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                columnModel { series(weeklyCounts) }
            }
        }
    }
    Card(
        modifier = modifier.fillMaxWidth().then(ddGlassBorderModifier()),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("최근 8주 기록 추이", style = MaterialTheme.typography.labelLarge)
            ProvideVicoTheme(rememberM3VicoTheme()) {
                val goldColor = MaterialTheme.colorScheme.secondary
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    fill = Fill(
                                        Brush.verticalGradient(
                                            listOf(goldColor.copy(alpha = 0.95f), goldColor.copy(alpha = 0.25f)),
                                        ),
                                    ),
                                    thickness = 16.dp,
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                ),
                            ),
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                )
            }
        }
    }
}

// Same color roles as DDDrinkTypeRatioCard (와인=tertiary/Rose, 위스키=secondary/Gold,
// 맥주=primary/Green) so the two type-breakdown cards read as one consistent language —
// this answers "뭘 좋아하는지" (average rating) where the ratio card only answers
// "뭘 많이 마셨는지" (count).
@Composable
fun DDTypeRatingComparisonCard(summary: DashboardSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().then(ddGlassBorderModifier()),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("종류별 평점 비교", style = MaterialTheme.typography.labelLarge)
            DDTypeRatingRow("와인", summary.wineCount, summary.wineAverageRating, MaterialTheme.colorScheme.tertiary)
            DDTypeRatingRow("위스키", summary.whiskeyCount, summary.whiskeyAverageRating, MaterialTheme.colorScheme.secondary)
            DDTypeRatingRow("맥주", summary.beerCount, summary.beerAverageRating, MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DDTypeRatingRow(label: String, count: Int, averageRating: Double?, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$label ${count}회", style = MaterialTheme.typography.bodyMedium)
            Text(
                averageRating?.let { "★ %.1f".format(it) } ?: "-",
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.18f)),
        ) {
            if (averageRating != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (averageRating / 5.0).toFloat().coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color),
                )
            }
        }
    }
}

fun formatRecordedDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

fun formatPrice(price: Long?): String =
    price?.let { NumberFormat.getNumberInstance(Locale.KOREA).format(it) + "원" } ?: "-"
