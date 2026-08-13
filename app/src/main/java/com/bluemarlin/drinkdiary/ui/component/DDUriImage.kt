package com.bluemarlin.drinkdiary.ui.component

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 이미지 라이브러리를 새로 들이지 않기 위해 플랫폼 ImageDecoder로 직접 디코드한다
// (harness.md §10 — 공식 대안으로 충분하면 서드파티를 추가하지 않는다).
@Composable
fun DDUriImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val loaded = bitmap
    if (loaded == null) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text("사진", style = MaterialTheme.typography.labelSmall)
        }
    } else {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}
