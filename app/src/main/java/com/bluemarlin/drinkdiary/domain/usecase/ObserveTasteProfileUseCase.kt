package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

class ObserveTasteProfileUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<TasteProfile> {
        val (drinkType, traits) =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine to Trait.of(DrinkType.Wine)
                TypeScope.Whiskey -> DrinkType.Whiskey to Trait.of(DrinkType.Whiskey)
                TypeScope.Combined -> null to Trait.shared
            }

        return repository.observeRecords(drinkType).map { records ->
            val preferences =
                traits.map { trait ->
                    val highGroup = mutableListOf<DrinkRecord>()
                    val lowGroup = mutableListOf<DrinkRecord>()
                    var unsureCount = 0

                    for (record in records) {
                        when (record.taste[trait]) {
                            TraitAnswer.High -> highGroup.add(record)
                            TraitAnswer.Low -> lowGroup.add(record)
                            TraitAnswer.Unsure -> unsureCount++
                            null -> {}
                        }
                    }

                    var direction: TraitAnswer? = null

                    if (highGroup.size >= TasteThresholds.MIN_SAMPLES_PER_SIDE &&
                        lowGroup.size >= TasteThresholds.MIN_SAMPLES_PER_SIDE
                    ) {
                        val avgHigh = highGroup.map { it.rating }.average()
                        val avgLow = lowGroup.map { it.rating }.average()

                        if (abs(avgHigh - avgLow) >= TasteThresholds.MIN_RATING_GAP) {
                            direction = if (avgHigh > avgLow) TraitAnswer.High else TraitAnswer.Low
                        }
                    }

                    TraitPreference(
                        trait = trait,
                        direction = direction,
                        highSamples = highGroup.size,
                        lowSamples = lowGroup.size,
                        unsureSamples = unsureCount,
                    )
                }

            TasteProfile(
                scope = scope,
                recordCount = records.size,
                preferences = preferences,
            )
        }
    }
}
