package com.example.pr5_2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAll(): List<Recipe>

    @Insert
    fun insert(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)
}