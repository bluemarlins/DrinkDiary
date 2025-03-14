package com.bluemarlin.drinkdiary.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemarlin.drinkdiary.ui.theme.ButtonGroupSelected
import com.bluemarlin.drinkdiary.ui.theme.ButtonGroupUnSelected
import java.text.SimpleDateFormat
import java.util.Locale

enum class SortOption(val label: String) {
    DATE("by date"),
    PRICE("by price"),
    RATING("by rating")
}


@Composable
fun HomeScreen() {
    var sortOption by remember { mutableStateOf(SortOption.DATE) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 12.dp)
        .background(MaterialTheme.colorScheme.background)) {
        SortButtonGroup(selectedOption = sortOption, onOptionSelected = { sortOption = it })
        WineList(sampleWines, sortOption)
    }
}

@Composable
fun SortButtonGroup(selectedOption: SortOption, onOptionSelected: (SortOption) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SortOption.entries.forEach { option ->
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption == option) ButtonGroupSelected else ButtonGroupUnSelected,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(text = option.label, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun WineList(wines: List<Wine>, sortOption: SortOption) {
    var expandedWineId by remember { mutableStateOf<String?>(null) }
    val lazyListState = rememberLazyListState()

    val sortedWines = remember(sortOption, wines) {
        when (sortOption) {
            SortOption.DATE -> wines.sortedByDescending {
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(it.purchaseDate)
            }

            SortOption.PRICE -> wines.sortedByDescending { it.price.toIntOrNull() ?: 0 }
            SortOption.RATING -> wines.sortedByDescending { it.rating }
        }
    }

    LazyColumn(state = lazyListState) {
        items(sortedWines, key = { it.name }) { wine ->
            WineCard(wine, modifier = Modifier.animateItem(), expandedWineId) { newId ->
                expandedWineId = if (expandedWineId == newId) null else newId
            }
        }
    }
}

@Composable
fun WineCard(wine: Wine, modifier: Modifier, expandedWineId: String?, onExpand: (String) -> Unit) {
    val isExpanded = wine.name == expandedWineId
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f, label = ""
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = wine.imageRes),
                contentDescription = wine.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .weight(3f),
                contentScale = ContentScale.FillHeight
            )
            Column(
                modifier = Modifier
                    .padding(start = 7.dp)
                    .weight(8f)
            ) {
                Text(text = wine.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Date ${wine.purchaseDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Price: ${wine.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(modifier = Modifier
                .weight(2f)
                .rotate(rotationState),
                onClick = { onExpand(if (isExpanded) "" else wine.name) }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color.Gray
                )
            }
        }
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
                Text(text = "Rating: ${wine.rating}", fontSize = 14.sp)
                Text(text = "Where: ${wine.purchaseLocation}", fontSize = 14.sp)
                Text(
                    text = "Notes: ${wine.tastingNotes}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreen()
}