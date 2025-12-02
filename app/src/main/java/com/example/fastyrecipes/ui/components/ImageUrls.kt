package com.example.fastyrecipes.ui.components

object ImageUrls {
    // URLs de Unsplash para diferentes categorías de comida
    val defaultRecipes = listOf(
        "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400", // Pizza
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400", // Hamburguesa
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400", // Brownie
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400", // Comida variada
        "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=400", // Tarta
        "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400", // Salmón
        "https://images.unsplash.com/photo-1605478371315-e4c2bf81296a?w=400", // Pasta
        "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400", // Ensalada
        "https://images.unsplash.com/photo-1606636660488-16a8646f012c?w=400", // Pollo
        "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400"  // Sopa
    )

    fun getRandomImageUrl(): String {
        return defaultRecipes.random()
    }

    fun getImageByCategory(category: String): String {
        return when (category.lowercase()) {
            "pizza", "italiana" -> "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400"
            "hamburguesa", "comida rápida" -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"
            "postre", "dulce" -> "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400"
            "pasta", "italiana" -> "https://images.unsplash.com/photo-1605478371315-e4c2bf81296a?w=400"
            "ensalada", "saludable" -> "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400"
            "pollo", "carne" -> "https://images.unsplash.com/photo-1606636660488-16a8646f012c?w=400"
            "pescado", "marisco" -> "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400"
            "sopa", "caldo" -> "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400"
            else -> getRandomImageUrl()
        }
    }
}