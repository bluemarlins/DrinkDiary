package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.AppResult

// 고른 사진을 **앱 안으로 들여온다.**
//
// 갤러리가 준 URI를 그대로 저장하면 안 된다. Photo Picker의 권한은 프로세스 수명까지라
// 앱을 다시 켜는 순간 `SecurityException`이 나고 사진 자리가 빈다(prd.md F1-3의 WARNING,
// 2026-08-17 에뮬레이터에서 확인). 원본이 갤러리에서 지워져도 마찬가지다.
//
// 로컬 우선 앱에서 이건 곧 데이터 손실이다 — 복구 경로가 영구히 없다.
interface PhotoRepository {
    suspend fun import(sourceUri: String): AppResult<String>
}
