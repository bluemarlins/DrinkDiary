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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 이미지 라이브러리를 새로 들이지 않기 위해 플랫폼 ImageDecoder로 직접 디코드한다
// (harness.md §10 — 공식 대안으로 충분하면 서드파티를 추가하지 않는다).
private sealed interface PhotoState {
    data object Loading : PhotoState

    data class Loaded(
        val bitmap: ImageBitmap,
    ) : PhotoState

    data object Failed : PhotoState
}

@Composable
fun DDUriImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var state by remember(imageUri) { mutableStateOf<PhotoState>(PhotoState.Loading) }

    LaunchedEffect(imageUri) {
        state =
            withContext(Dispatchers.IO) {
                runCatching {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(imageUri))
                    PhotoState.Loaded(ImageDecoder.decodeBitmap(source).asImageBitmap())
                }.getOrElse { PhotoState.Failed }
            }
    }

    when (val current = state) {
        is PhotoState.Loaded ->
            Image(
                bitmap = current.bitmap,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
            )

        // 못 읽은 것과 읽는 중인 것을 같은 자리로 뭉개면 **조용한 실패**가 된다(harness.md §7).
        // 사진이 사라진 화면을 사용자가 "원래 안 넣었나 보다"로 읽으면 그 기록은 영영 안 고쳐진다.
        PhotoState.Failed ->
            Placeholder(
                text = "사진을 불러오지 못했어요",
                description = "사진을 불러오지 못했어요. 기록을 고쳐서 다시 넣을 수 있어요.",
                modifier = modifier,
            )

        PhotoState.Loading -> Placeholder(text = "", description = null, modifier = modifier)
    }
}

@Composable
private fun Placeholder(
    text: String,
    description: String?,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (description == null) {
                        Modifier
                    } else {
                        Modifier.semantics { contentDescription = description }
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
