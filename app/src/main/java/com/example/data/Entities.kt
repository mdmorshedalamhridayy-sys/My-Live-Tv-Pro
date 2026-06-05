package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val streamUrl: String,
    val thumbnail: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val videoUrl: String,
    val thumbnail: String,
    val category: String,
    val isDownloaded: Boolean = false,
    val localFileUri: String? = null,
    val durationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "Admin", "Editor", "User"
    val isBanned: Boolean = false,
    val lastLogin: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val scheduleTime: Long,
    val sentStatus: Boolean = false,
    val targetType: String = "All" // "All", "Subscribers"
) : Serializable

@Entity(tableName = "analytics")
data class AnalyticsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val actionType: String, // "view_channel", "view_video", "login", "download"
    val contentId: Long,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val contentId: Long,
    val contentType: String // "channel", "video"
) : Serializable
