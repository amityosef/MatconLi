package com.colman.matconli.model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.colman.matconli.base.MyApplication
import com.colman.matconli.model.dao.RecipeDao
import com.colman.matconli.model.dao.UserDao

@Database(entities = [Recipe::class, User::class], version = 3, exportSchema = false)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
}

object AppLocalDB {

    val db: AppLocalDbRepository by lazy {
        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Application context not available")

        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "matkonli_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}

