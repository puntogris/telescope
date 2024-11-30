package com.puntogris.telescope.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.VectorDistanceType

@Entity
data class ImageEntity(
    @Id var id: Long = 0,
    var uri: String = "",
    @HnswIndex(dimensions = 512, distanceType = VectorDistanceType.DOT_PRODUCT)
    var embedding: FloatArray = floatArrayOf()
)