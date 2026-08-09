package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecord(
    val id: Long,
    val type: DrinkType,
    val name: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val tastingNote: String?,
    /** Catalog keys from [TastingTag], plus any custom text the user typed. */
    val tastingTags: List<String>,
    val rating: Double,
    /** Null when never set by the user — see [effectiveAbv]. */
    val abv: Double?,
    /** Null when never set by the user — see [effectiveVolumeMl]. */
    val volumeMl: Int?,
    val collectionStatus: CollectionStatus,
    val recordedAtMillis: Long,
) {
    /**
     * Falls back to the drink type's typical strength when the user never entered one, so
     * intake figures can be computed for every record. [isIntakeEstimated] says whether the
     * result was measured or guessed.
     */
    val effectiveAbv: Double
        get() = abv ?: type.defaultAbv()

    val effectiveVolumeMl: Int
        get() = volumeMl ?: type.defaultVolumeMl()

    /** True when either figure came from a type default rather than from the user. */
    val isIntakeEstimated: Boolean
        get() = abv == null || volumeMl == null

    /** Grams of pure alcohol: volume × strength × ethanol density. */
    val pureAlcoholGrams: Double
        get() = effectiveVolumeMl * (effectiveAbv / 100.0) * ETHANOL_DENSITY

    private companion object {
        const val ETHANOL_DENSITY = 0.789
    }
}
