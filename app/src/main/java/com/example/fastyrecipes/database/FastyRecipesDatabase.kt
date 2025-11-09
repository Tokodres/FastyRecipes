package com.example.fastyrecipes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fastyrecipes.database.daos.ProductoDao
import com.example.fastyrecipes.database.daos.RecetaDao
import com.example.fastyrecipes.modelo.Producto
import com.example.fastyrecipes.modelo.Receta

@Database(
    entities = [Receta::class, Producto::class],
    version = 3,
    exportSchema = false
)
abstract class FastyRecipesDatabase : RoomDatabase() {

    abstract fun recetaDao(): RecetaDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var INSTANCE: FastyRecipesDatabase? = null

        fun getDatabase(context: Context): FastyRecipesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FastyRecipesDatabase::class.java,
                    "fasty_recipes_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}