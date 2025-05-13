package com.example.p2papp

enum class PageType {
    NORMAL,       // 1 o 2 bloques de texto
    TEXT_IMAGE    // Texto con imagen abajo (el nuevo tipo)
}

data class PageContent(
    val type: PageType,
    val title1: String? = null,
    val description1: String? = null,
    val title2: String? = null,
    val description2: String? = null,
    val imageRes1Id: Int? = null,
    val imageRes2Id: Int? = null
)

