package com.puntogris.telescope.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.VectorDistanceType

@Entity
data class ImageEntity(
    @Id
    var id: Long = 0,

    var uri: String = "",

    @Index
    var name: String = "",

    var timestamp: Long = 0,

    @HnswIndex(dimensions = 512, distanceType = VectorDistanceType.DOT_PRODUCT)
    var embedding: FloatArray = floatArrayOf()
)