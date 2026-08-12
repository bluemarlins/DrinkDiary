package com.bluemarlin.drinkdiary.domain.model

import com.bluemarlin.drinkdiary.R

enum class DashboardPeriod(
    val labelRes: Int,
) {
    Weekly(R.string.dashboard_period_weekly),
    Monthly(R.string.dashboard_period_monthly),
    Yearly(R.string.dashboard_period_yearly),
}
