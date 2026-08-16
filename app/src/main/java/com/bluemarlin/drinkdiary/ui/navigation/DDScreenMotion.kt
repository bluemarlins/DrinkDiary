package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

// 명세 1절 3번 "공간적 깊이감과 모션 — Depth In/Out (Z-Axis Shared Axis), 탭 전환 모핑".
//
// 깊이 이동과 같은 계층 안의 이동은 다른 모션을 쓴다. 계층을 오갈 때는 방향이 있으므로 Z축으로
// 밀고 당기고, 탭처럼 방향이 없는 이동은 밀면 안 된다 — 없는 방향을 만들어 내면 사용자가
// "어디로 갔지"를 되짚게 된다.
//
// `SizeTransform(clip = false)`은 생성자로 넘긴다. infix `using`은 `AnimatedContentTransitionScope`
// 안에서만 쓸 수 있어서 여기(최상위 함수)서는 부를 수 없다.
//
// 시스템 애니메이션 배율(설정 > 접근성)은 Compose가 알아서 따른다. 배율이 0이면 즉시 전환된다.

private const val DepthDuration = 300
private const val DepthFadeOut = 90
private val NoClip = SizeTransform(clip = false)

// 계층 안으로 들어갈 때(목록 -> 상세). 들어오는 화면이 뒤에서 다가오고, 나가는 화면은 앞으로 물러난다.
fun depthIn(): ContentTransform =
    ContentTransform(
        targetContentEnter =
            fadeIn(tween(DepthDuration, delayMillis = DepthFadeOut)) +
                scaleIn(tween(DepthDuration, delayMillis = DepthFadeOut), initialScale = 0.8f),
        initialContentExit =
            fadeOut(tween(DepthFadeOut)) + scaleOut(tween(DepthDuration), targetScale = 1.1f),
        sizeTransform = NoClip,
    )

// 계층 밖으로 나올 때(상세 -> 목록). 방향만 뒤집는다.
fun depthOut(): ContentTransform =
    ContentTransform(
        targetContentEnter =
            fadeIn(tween(DepthDuration, delayMillis = DepthFadeOut)) +
                scaleIn(tween(DepthDuration, delayMillis = DepthFadeOut), initialScale = 1.1f),
        initialContentExit =
            fadeOut(tween(DepthFadeOut)) + scaleOut(tween(DepthDuration), targetScale = 0.8f),
        sizeTransform = NoClip,
    )

// 같은 계층 안에서의 이동(탭 전환). 방향이 없으므로 깊이를 쓰지 않는다.
fun fadeThrough(): ContentTransform =
    ContentTransform(
        targetContentEnter =
            fadeIn(tween(210, delayMillis = 90)) +
                scaleIn(tween(210, delayMillis = 90), initialScale = 0.92f),
        initialContentExit = fadeOut(tween(90)),
        sizeTransform = NoClip,
    )
