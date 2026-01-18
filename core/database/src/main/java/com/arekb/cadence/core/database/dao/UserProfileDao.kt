package com.arekb.cadence.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arekb.cadence.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    /**
     * Inserts a user profile into the database.
     * If a user with the same primary key (id) already exists,
     * it will be replaced with the new data.
     * @param user The UserEntity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    /**
     * Retrieves the user profile from the database.
     * Since there should only ever be one user, we limit the query to 1.
     * This function returns a Flow, so the UI will automatically update whenever
     * the user's profile data changes. The return type is nullable (UserEntity?)
     * to handle the case where the table is empty.
     * @return A Flow emitting the UserEntity, or null if no user is found.
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    /**
     * Deletes the user profile from the database.
     * This is useful for handling a logout scenario.
     */
    @Query("DELETE FROM user_profile")
    suspend fun clearUser()
}