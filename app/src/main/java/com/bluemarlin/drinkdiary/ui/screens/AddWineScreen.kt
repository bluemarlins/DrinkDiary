package com.bluemarlin.drinkdiary.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.NumberFormat
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.bluemarlin.drinkdiary.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWineScreen(onWineAdded: (Wine) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var purchaseLocation by remember { mutableStateOf(TextFieldValue("")) }
    var tastingNotes by remember { mutableStateOf(TextFieldValue("")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var purchaseDate by remember { mutableStateOf("") }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap = bitmap
        }
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp

        Box(
            modifier = Modifier
                .width(screenWidth * 0.5f)
                .height(screenHeight * 0.4f)
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .clickable { showImageOptions = true },
            contentAlignment = Alignment.Center
        ) {
            when {
                imageUri != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    showImageOptions = false
                }

                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    showImageOptions = false
                }

                else -> {
                    Text("Tap to add image", color = Color.White, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Wine Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var rawPrice by remember { mutableStateOf("") }

        OutlinedTextField(
            value = formatNumber(rawPrice), // 쉼표 추가된 값 표시
            onValueChange = { input ->
                rawPrice = input.filter { it.isDigit() } // 숫자만 허용
            },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = { Text("원") }, // 원 단위 고정
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 와인 타입 선택
        var expanded by remember { mutableStateOf(false) }
        var wineType by remember { mutableStateOf(WineType.RED) } // 현재 선택된 와인 타입

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = wineType.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Wine Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // 📌 추가해야 Dropdown이 정상 작동!
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                WineType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            wineType = type
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        /*        var rating by remember { mutableFloatStateOf(2.5f) } // 기본값 설정
                Text("Rating: $rating", style = MaterialTheme.typography.bodyLarge)

                Slider(
                    value = rating,
                    onValueChange = { rating = (Math.round(it * 10) / 10f) }, // 소수점 한 자리로 반올림
                    valueRange = 0f..5f,
                    steps = 50, // 0.1 단위로 움직이도록 설정
                    modifier = Modifier.fillMaxWidth()
                )*/
        Spacer(modifier = Modifier.height(16.dp))

        PurchaseDatePicker(onDateChange = { date -> purchaseDate = date })

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = purchaseLocation,
            onValueChange = { purchaseLocation = it },
            label = { Text("Purchase Location") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var rating by remember { mutableFloatStateOf(2.5f) } // 초기 평점
        WineRatingBar(rating = 2.5f, onRatingChange = { value -> rating = value })

        OutlinedTextField(
            value = tastingNotes,
            onValueChange = { tastingNotes = it },
            label = { Text("Tasting Notes") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 와인 추가 버튼
        Button(
            onClick = {
                val newWine = Wine(
                    name = name.text,
                    rating = rating,
                    price = rawPrice,
                    imageRes = R.drawable.wine_sample, // 실제 앱에서는 저장된 이미지 URI 사용 필요
                    purchaseDate = purchaseDate,
                    purchaseLocation = purchaseLocation.text,
                    tastingNotes = tastingNotes.text,
                    type = wineType
                )
                onWineAdded(newWine)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        {
            Text(text = "OK")
        }

        if (showImageOptions) {
            ModalBottomSheet(
                onDismissRequest = { showImageOptions = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Image", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Choose from Gallery")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            takePictureLauncher.launch(cameraIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Take a Photo")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showImageOptions = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = Color.Red)
                    }
                }
            }
        }
    }
}

// 숫자 포맷 (쉼표 추가)
fun formatNumber(value: String): String {
    return if (value.isNotEmpty()) {
        NumberFormat.getNumberInstance(Locale.US).format(value.toLong())
    } else {
        ""
    }
}

@Preview
@Composable
fun AddWineScreenPreview() {
    AddWineScreen(onWineAdded = {})
}