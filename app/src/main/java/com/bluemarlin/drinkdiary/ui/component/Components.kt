package com.bluemarlin.drinkdiary.ui.component

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.roundToHalf
import java.text.NumberFormat
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DDPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDContainedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Composable
fun DDDestructiveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun DDAddRecordFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
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
fun DDEmptyContent(message: String, actionText: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
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
    )
}

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
    )
}

@Composable
fun DDMultilineTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
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
                val selectedMillis = java.time.LocalDate.of(year, month + 1, dayOfMonth)
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
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DrinkType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = DrinkType.entries.size),
                    ) { Text(type.label, maxLines = 1, overflow = TextOverflow.Clip) }
                }
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CollectionStatus.entries.forEachIndexed { index, status ->
                    SegmentedButton(
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CollectionStatus.entries.size),
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
                shape = SegmentedButtonDefaults.itemShape(index = index, count = DashboardPeriod.entries.size),
            ) { Text(period.label, maxLines = 1, overflow = TextOverflow.Clip) }
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
            item { FilterChip(selected = selected == null, onClick = { onSelected(null) }, label = { Text("전체") }) }
            DrinkType.entries.forEach { type ->
                item { FilterChip(selected = selected == type, onClick = { onSelected(type) }, label = { Text(type.label) }) }
            }
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
            item { FilterChip(selected = selected == null, onClick = { onSelected(null) }, label = { Text("전체") }) }
            CollectionStatus.entries.forEach { status ->
                item { FilterChip(selected = selected == status, onClick = { onSelected(status) }, label = { Text(status.label) }) }
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
    AssistChip(onClick = {}, label = { Text(type.label) })
}

@Composable
fun DDCollectionStatusBadge(status: CollectionStatus) {
    AssistChip(onClick = {}, label = { Text(status.label) })
}

@Composable
fun DDRatingStars(rating: Double) {
    Text(ratingStarsText(rating), color = MaterialTheme.colorScheme.primary)
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
    ElevatedCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDImageThumbnail(record.imageUri)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(record.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(record.type.label, style = MaterialTheme.typography.bodySmall)
                    DDRatingStars(record.rating)
                }
                Text("${record.collectionStatus.label} · ${formatRecordedDate(record.recordedAtMillis)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DDDrinkRecordCard(record: DrinkRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.name, style = MaterialTheme.typography.titleSmall)
            Text("${record.type.label} · ${record.collectionStatus.label}", style = MaterialTheme.typography.bodySmall)
            DDRatingStars(record.rating)
        }
    }
}

@Composable
fun DDImageThumbnail(imageUri: String?) {
    if (imageUri == null) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text("사진", style = MaterialTheme.typography.labelSmall)
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
            Box(
                modifier = Modifier
                    .width(imageWidth)
                    .height(220.dp.coerceAtMost(imageHeight))
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("등록된 사진 없음")
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
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
    val value = if (totalCount == 0) {
        "와인 0% · 위스키 0% · 맥주 0%"
    } else {
        fun ratio(count: Int): Int = (count * 100) / totalCount
        "와인 ${ratio(wineCount)}% · 위스키 ${ratio(whiskeyCount)}% · 맥주 ${ratio(beerCount)}%"
    }
    DDDashboardSummaryCard(
        title = "종류별 비중",
        value = value,
        modifier = modifier,
    )
}

fun formatRecordedDate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

fun formatPrice(price: Long?): String =
    price?.let { NumberFormat.getNumberInstance(Locale.KOREA).format(it) + "원" } ?: "-"
