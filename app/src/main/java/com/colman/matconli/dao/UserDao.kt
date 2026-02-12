package com.colman.matconli.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.colman.matconli.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User WHERE id = :id")
    fun getById(id: String): LiveData<User?>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getByIdSync(id: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Update
    fun update(user: User)
}

