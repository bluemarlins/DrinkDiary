package com.bluemarlin.drinkdiary.domain.model

// 사용자가 축 하나에 답할 수 있는 값. Mid는 "보통이었다"이며 버리지 않는다.
//
// 2026-08-13 판은 가운데를 Unsure("판단 못 함")로 두고 판정에서 제외했다. 논리는 옳았지만
// 결과가 나빴다 — 입문자 접점 103종은 모든 축에서 가운데가 가장 두꺼워서(51~56%),
// 가운데를 버리는 것은 데이터의 절반을 버리는 것이었다.
// software-architecture.md 2-3절, departments/planner/axis-validation-2026-08.md.
enum class TraitAnswer(
    // 상관 계산에 쓰는 순서값. 등간이라고 주장하는 것이 아니라 순서를 수치로 옮긴 것뿐이다.
    val level: Int,
) {
    Low(0),
    Mid(1),
    High(2),
    ;

    companion object {
        fun fromName(name: String?): TraitAnswer? = entries.firstOrNull { it.name == name }
    }
}
