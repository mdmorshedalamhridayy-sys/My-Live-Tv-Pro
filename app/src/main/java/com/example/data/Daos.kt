package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY id DESC")
    fun getAllChannelsFlow(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels ORDER BY id DESC")
    suspend fun getAllChannels(): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE category = :category ORDER BY id DESC")
    fun getChannelsByCategoryFlow(category: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannelById(id: Long)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY id DESC")
    suspend fun getAllVideos(): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY id DESC")
    fun getVideosByCategoryFlow(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDownloaded = 1")
    fun getDownloadedVideosFlow(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isBanned = :isBanned WHERE id = :id")
    suspend fun updateUserBanStatus(id: Long, isBanned: Boolean)

    @Query("UPDATE users SET role = :role WHERE id = :id")
    suspend fun updateUserRole(id: Long, role: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY scheduleTime DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics ORDER BY timestamp DESC")
    fun getAllAnalyticsFlow(): Flow<List<AnalyticsEntity>>

    @Query("SELECT * FROM analytics")
    suspend fun getAllAnalytics(): List<AnalyticsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AnalyticsEntity): Long

    @Query("SELECT COUNT(*) FROM analytics WHERE actionType = 'view_channel' AND contentId = :channelId")
    suspend fun getChannelViews(channelId: Long): Int

    @Query("SELECT COUNT(*) FROM analytics WHERE actionType = 'view_video' AND contentId = :videoId")
    suspend fun getVideoViews(videoId: Long): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesFlow(userId: Long): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND contentId = :contentId AND contentType = :contentType LIMIT 1")
    suspend fun getFavorite(userId: Long, contentId: Long, contentType: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND contentId = :contentId AND contentType = :contentType")
    suspend fun removeFavorite(userId: Long, contentId: Long, contentType: String)
}

@Dao
interface GatewayNumberDao {
    @Query("SELECT * FROM gateway_numbers ORDER BY id DESC")
    fun getAllGatewayNumbersFlow(): Flow<List<GatewayNumberEntity>>

    @Query("SELECT * FROM gateway_numbers ORDER BY id DESC")
    suspend fun getAllGatewayNumbers(): List<GatewayNumberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGatewayNumber(gatewayNumber: GatewayNumberEntity): Long

    @Delete
    suspend fun deleteGatewayNumber(gatewayNumber: GatewayNumberEntity)

    @Query("DELETE FROM gateway_numbers WHERE id = :id")
    suspend fun deleteGatewayNumberById(id: Long)
}
