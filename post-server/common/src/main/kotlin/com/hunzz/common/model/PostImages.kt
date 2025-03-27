package com.hunzz.common.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable

@Embeddable
class PostImages(
    @Convert(converter = StringListConverter::class)
    @Column(name = "image_urls")
    val url: List<String>
)