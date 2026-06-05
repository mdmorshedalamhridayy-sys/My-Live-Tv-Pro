package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(private val db: AppDatabase) {
    private val TAG = "Repository"

    val channelsFlow: Flow<List<ChannelEntity>> = db.channelDao().getAllChannelsFlow()
    val videosFlow: Flow<List<VideoEntity>> = db.videoDao().getAllVideosFlow()
    val usersFlow: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    val notificationsFlow: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotificationsFlow()
    val analyticsFlow: Flow<List<AnalyticsEntity>> = db.analyticsDao().getAllAnalyticsFlow()

    fun getFavoritesFlow(userId: Long): Flow<List<FavoriteEntity>> = db.favoriteDao().getFavoritesFlow(userId)

    suspend fun initSeeding() = withContext(Dispatchers.IO) {
        // Only seed if no channels and videos exist yet
        val existingChannels = db.channelDao().getAllChannels()
        val existingVideos = db.videoDao().getAllVideos()

        if (existingChannels.isEmpty()) {
            Log.d(TAG, "Seeding default Live TV channels...")
            val defaults = listOf(
                ChannelEntity(
                    name = "T Sports Bangla Live",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500",
                    category = "Bangladesh TV"
                ),
                ChannelEntity(
                    name = "GTV Sports BD",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1540747737956-3787217ab2ad?w=500",
                    category = "Bangladesh TV"
                ),
                ChannelEntity(
                    name = "Somoy TV Live 24/7",
                    streamUrl = "https://rtmp.somoynews.tv/live/somoy.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=500",
                    category = "Live News"
                ),
                ChannelEntity(
                    name = "France 24 HD Network",
                    streamUrl = "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                    category = "Live News"
                ),
                ChannelEntity(
                    name = "Al Jazeera English",
                    streamUrl = "https://live-hls-web-aje.getaj.net/AJE/index.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1546422904-90eabf3bac0a?w=500",
                    category = "Live News"
                ),
                ChannelEntity(
                    name = "Sky Sports Cricket",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1531415080290-bc98545bab3f?w=500",
                    category = "Live Sports"
                ),
                ChannelEntity(
                    name = "BTV World HD LIVE",
                    streamUrl = "http://103.119.100.10:8000/btv_world.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1595152772835-219674b2a8a6?w=500",
                    category = "Bangladesh TV"
                ),
                ChannelEntity(
                    name = "NASA Science Live",
                    streamUrl = "https://ntv1.nasatv.nasa.gov/hls/ntv1_1080p.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500",
                    category = "Live News"
                ),
                ChannelEntity(
                    name = "Red Bull Live Sports",
                    streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/global/master.m3u8",
                    thumbnail = "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500",
                    category = "Live Sports"
                )
            )
            for (ch in defaults) {
                db.channelDao().insertChannel(ch)
            }
        }

        if (existingVideos.isEmpty()) {
            Log.d(TAG, "Seeding default VOD matches and programs...")
            val defaults = listOf(
                VideoEntity(
                    title = "Bangladesh vs India Asia Cup Classic Rematch Highlights",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500",
                    category = "Match Highlights",
                    durationSeconds = 600
                ),
                VideoEntity(
                    title = "Dhaka Premier League Cricket Top 10 Best Wickets",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1531415080290-bc98545bab3f?w=500",
                    category = "Cricket Specials",
                    durationSeconds = 300
                ),
                VideoEntity(
                    title = "Post-Match Critical Press Conference Analysis",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=500",
                    category = "Talk Shows",
                    durationSeconds = 900
                ),
                VideoEntity(
                    title = "Under-19 Stars Cricket Championship Full Journey Documentary",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    thumbnail = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=500",
                    category = "Documentaries",
                    durationSeconds = 1800
                )
            )
            for (v in defaults) {
                db.videoDao().insertVideo(v)
            }
        }

        // Seed some admin, editor and subscriber users to test Auth easily!
        val existingUsers = db.userDao().getUserByEmail("admin@mylivetv.com")
        if (existingUsers == null) {
            Log.d(TAG, "Seeding default user logins (admin, editor, users)...")
            db.userDao().insertUser(
                UserEntity(
                    name = "Morshed Admin",
                    email = "admin@mylivetv.com",
                    passwordHash = "admin123", // standard testing hash
                    role = "Admin"
                )
            )
            db.userDao().insertUser(
                UserEntity(
                    name = "Sujon Editor",
                    email = "editor@mylivetv.com",
                    passwordHash = "editor123",
                    role = "Editor"
                )
            )
            db.userDao().insertUser(
                UserEntity(
                    name = "Regular Subscriber",
                    email = "user@mylivetv.com",
                    passwordHash = "user123",
                    role = "User"
                )
            )
        }

        // Seed the primary requested admin user
        val primaryAdmin = db.userDao().getUserByEmail("mdmorshedalamhridayy@gmail.com")
        if (primaryAdmin == null) {
            Log.d(TAG, "Seeding primary admin: mdmorshedalamhridayy@gmail.com")
            val pId = db.userDao().insertUser(
                UserEntity(
                    name = "Morshed Hriday",
                    email = "mdmorshedalamhridayy@gmail.com",
                    passwordHash = "1b2e4m5r6h7e",
                    role = "Admin"
                )
            )
            // Sync to Firestore
            FirebaseHelper.firestore?.let { fs ->
                val data = hashMapOf(
                    "id" to pId,
                    "name" to "Morshed Hriday",
                    "email" to "mdmorshedalamhridayy@gmail.com",
                    "role" to "Admin",
                    "is_banned" to false,
                    "last_login" to System.currentTimeMillis()
                )
                fs.collection("users").document(pId.toString()).set(data)
            }
        }

        // Safety block to ensure outstanding Live channels are imported even if the database is already seeded!
        val liveChannelsToSeed = listOf(
            ChannelEntity(
                name = "Somoy TV Live 24/7",
                streamUrl = "https://rtmp.somoynews.tv/live/somoy.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=500",
                category = "Live News"
            ),
            ChannelEntity(
                name = "France 24 HD Network",
                streamUrl = "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
                category = "Live News"
            ),
            ChannelEntity(
                name = "Al Jazeera English",
                streamUrl = "https://live-hls-web-aje.getaj.net/AJE/index.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1546422904-90eabf3bac0a?w=500",
                category = "Live News"
            ),
            ChannelEntity(
                name = "BTV World HD LIVE",
                streamUrl = "http://103.119.100.10:8000/btv_world.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1595152772835-219674b2a8a6?w=500",
                category = "Bangladesh TV"
            ),
            ChannelEntity(
                name = "NASA Science Live",
                streamUrl = "https://ntv1.nasatv.nasa.gov/hls/ntv1_1080p.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500",
                category = "Live News"
            ),
            ChannelEntity(
                name = "Red Bull Live Sports",
                streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/global/master.m3u8",
                thumbnail = "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=500",
                category = "Live Sports"
            )
        )

        val currentChannels = db.channelDao().getAllChannels()
        for (newCh in liveChannelsToSeed) {
            val exists = currentChannels.any { it.name.trim().equals(newCh.name.trim(), ignoreCase = true) }
            if (!exists) {
                Log.d(TAG, "Incremental seeding adds missing premium channel: ${newCh.name}")
                db.channelDao().insertChannel(newCh)
            }
        }

        // Seed default payment gateway numbers if table is empty
        val currentGateways = db.gatewayNumberDao().getAllGatewayNumbers()
        if (currentGateways.isEmpty()) {
            db.gatewayNumberDao().insertGatewayNumber(GatewayNumberEntity(provider = "bKash", number = "01789456123", type = "Personal"))
            db.gatewayNumberDao().insertGatewayNumber(GatewayNumberEntity(provider = "Nagad", number = "01876543210", type = "Personal"))
            db.gatewayNumberDao().insertGatewayNumber(GatewayNumberEntity(provider = "Rocket", number = "01912345678", type = "Agent"))
        }
    }

    // --- Channel Operations ---
    suspend fun addChannel(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        val insertedId = db.channelDao().insertChannel(channel)
        // Also sync write to Firestore in Background
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to insertedId,
                "name" to channel.name,
                "stream_url" to channel.streamUrl,
                "thumbnail" to channel.thumbnail,
                "category" to channel.category,
                "created_at" to channel.createdAt
            )
            fs.collection("channels").document(insertedId.toString())
                .set(data)
                .addOnSuccessListener { Log.d(TAG, "Firestore Channel synced successfully!") }
                .addOnFailureListener { e -> Log.e(TAG, "Firestore Channel sync failed", e) }
        }
    }

    suspend fun updateChannel(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        db.channelDao().updateChannel(channel)
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to channel.id,
                "name" to channel.name,
                "stream_url" to channel.streamUrl,
                "thumbnail" to channel.thumbnail,
                "category" to channel.category,
                "created_at" to channel.createdAt
            )
            fs.collection("channels").document(channel.id.toString()).set(data)
        }
    }

    suspend fun deleteChannel(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        db.channelDao().deleteChannel(channel)
        FirebaseHelper.firestore?.let { fs ->
            fs.collection("channels").document(channel.id.toString()).delete()
        }
    }

    // --- Video Operations ---
    suspend fun addVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        val insertedId = db.videoDao().insertVideo(video)
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to insertedId,
                "title" to video.title,
                "video_url" to video.videoUrl,
                "thumbnail" to video.thumbnail,
                "category" to video.category,
                "duration_seconds" to video.durationSeconds,
                "created_at" to video.createdAt
            )
            fs.collection("videos").document(insertedId.toString()).set(data)
        }
    }

    suspend fun updateVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        db.videoDao().updateVideo(video)
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to video.id,
                "title" to video.title,
                "video_url" to video.videoUrl,
                "thumbnail" to video.thumbnail,
                "category" to video.category,
                "duration_seconds" to video.durationSeconds,
                "created_at" to video.createdAt
            )
            fs.collection("videos").document(video.id.toString()).set(data)
        }
    }

    suspend fun deleteVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        db.videoDao().deleteVideo(video)
        FirebaseHelper.firestore?.let { fs ->
            fs.collection("videos").document(video.id.toString()).delete()
        }
    }

    // --- User Operations ---
    suspend fun registerUser(name: String, email: String, passwordHash: String, role: String): UserEntity = withContext(Dispatchers.IO) {
        val user = UserEntity(name = name, email = email, passwordHash = passwordHash, role = role)
        val id = db.userDao().insertUser(user)
        val savedUser = user.copy(id = id)
        
        // Match user's role and sync to Firebase Firestore user profiles if reachable!
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to id,
                "name" to name,
                "email" to email,
                "role" to role,
                "is_banned" to false,
                "last_login" to savedUser.lastLogin
            )
            fs.collection("users").document(id.toString()).set(data)
        }
        
        savedUser
    }

    suspend fun authenticateUser(email: String, passwordHash: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByEmail(email)
        if (user != null && user.passwordHash == passwordHash) {
            val updatedUser = user.copy(lastLogin = System.currentTimeMillis())
            db.userDao().insertUser(updatedUser)
            updatedUser
        } else null
    }

    suspend fun banUser(id: Long, isBanned: Boolean) = withContext(Dispatchers.IO) {
        db.userDao().updateUserBanStatus(id, isBanned)
        FirebaseHelper.firestore?.let { fs ->
            fs.collection("users").document(id.toString()).update("is_banned", isBanned)
        }
    }

    suspend fun updateUserRole(id: Long, role: String) = withContext(Dispatchers.IO) {
        db.userDao().updateUserRole(id, role)
        FirebaseHelper.firestore?.let { fs ->
            fs.collection("users").document(id.toString()).update("role", role)
        }
    }

    // --- Favorites ---
    suspend fun toggleFavorite(userId: Long, contentId: Long, contentType: String) = withContext(Dispatchers.IO) {
        val existing = db.favoriteDao().getFavorite(userId, contentId, contentType)
        if (existing != null) {
            db.favoriteDao().deleteFavorite(existing)
        } else {
            db.favoriteDao().insertFavorite(FavoriteEntity(userId = userId, contentId = contentId, contentType = contentType))
        }
    }

    suspend fun isFavorite(userId: Long, contentId: Long, contentType: String): Boolean = withContext(Dispatchers.IO) {
        db.favoriteDao().getFavorite(userId, contentId, contentType) != null
    }

    // --- Analytics ---
    suspend fun logAnalyticsEvent(userId: Long, actionType: String, contentId: Long) = withContext(Dispatchers.IO) {
        val analytics = AnalyticsEntity(userId = userId, actionType = actionType, contentId = contentId)
        db.analyticsDao().insertAnalytics(analytics)
        
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "user_id" to userId,
                "action_type" to actionType,
                "content_id" to contentId,
                "timestamp" to analytics.timestamp
            )
            fs.collection("analytics").add(data)
        }
    }

    // --- Notifications ---
    suspend fun scheduleNotification(title: String, message: String, scheduleTime: Long): Long = withContext(Dispatchers.IO) {
        val notif = NotificationEntity(title = title, message = message, scheduleTime = scheduleTime, sentStatus = false)
        val id = db.notificationDao().insertNotification(notif)
        
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to id,
                "title" to title,
                "message" to message,
                "schedule_time" to scheduleTime,
                "sent_status" to false
            )
            fs.collection("notifications").document(id.toString()).set(data)
        }
        id
    }

    // --- Subscription Operations ---
    suspend fun updateUserSubscription(userId: Long, plan: String, expiry: Long, status: String) = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(
                subscriptionPlan = plan,
                subscriptionExpiry = expiry,
                subscriptionStatus = status
            )
            db.userDao().insertUser(updatedUser)
            FirebaseHelper.firestore?.let { fs ->
                fs.collection("users").document(userId.toString()).update(
                    mapOf(
                        "subscriptionPlan" to plan,
                        "subscriptionExpiry" to expiry,
                        "subscriptionStatus" to status
                    )
                )
            }
        }
    }

    // --- Dynamic Payment Gateway Number CRUD ---
    val gatewayNumbersFlow: Flow<List<GatewayNumberEntity>> = db.gatewayNumberDao().getAllGatewayNumbersFlow()

    suspend fun addGatewayNumber(gatewayNumber: GatewayNumberEntity) = withContext(Dispatchers.IO) {
        val insertedId = db.gatewayNumberDao().insertGatewayNumber(gatewayNumber)
        FirebaseHelper.firestore?.let { fs ->
            val data = hashMapOf(
                "id" to insertedId,
                "provider" to gatewayNumber.provider,
                "number" to gatewayNumber.number,
                "type" to gatewayNumber.type,
                "isAvailable" to gatewayNumber.isAvailable
            )
            fs.collection("gateway_numbers").document(insertedId.toString()).set(data)
        }
    }

    suspend fun deleteGatewayNumber(id: Long) = withContext(Dispatchers.IO) {
        db.gatewayNumberDao().deleteGatewayNumberById(id)
        FirebaseHelper.firestore?.let { fs ->
            fs.collection("gateway_numbers").document(id.toString()).delete()
        }
    }
}
