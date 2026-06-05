package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StreamingViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "StreamingViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val repository = Repository(database)

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Screen State / Navigation
    private val _currentScreen = MutableStateFlow("splash") // splash, auth, home, player, admin
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Filters & Search
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedResolution = MutableStateFlow("1080p (Auto)")

    // Content Lists (Filtered)
    val channels: StateFlow<List<ChannelEntity>> = repository.channelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoEntity>> = repository.videosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getFavoritesFlow(user.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated downloads progress tracker
    private val _downloadingIds = MutableStateFlow<Set<Long>>(emptySet())
    val downloadingIds: StateFlow<Set<Long>> = _downloadingIds.asStateFlow()

    // Player Playback controller
    private val _playbackChannel = MutableStateFlow<ChannelEntity?>(null)
    val playbackChannel: StateFlow<ChannelEntity?> = _playbackChannel.asStateFlow()

    private val _playbackVideo = MutableStateFlow<VideoEntity?>(null)
    val playbackVideo: StateFlow<VideoEntity?> = _playbackVideo.asStateFlow()

    init {
        // Init programmatic Firebase integration
        FirebaseHelper.initFirebase(application)
        
        // Populate standard database items
        viewModelScope.launch {
            try {
                repository.initSeeding()
                // Set default user
                val defaultUser = database.userDao().getUserByEmail("user@mylivetv.com")
                _currentUser.value = defaultUser
            } catch (e: Exception) {
                Log.e(TAG, "Error performing seeding startup", e)
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Auth Actions
    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _authError.value = null
            
            // Clean credentials and secure instant local guarantee for specified primary Admins
            val targetEmail = email.trim().lowercase()
            if (targetEmail == "mdmorshedalamhridayy@gmail.com" && passwordHash == "1b2e4m5r6h7e") {
                val db = AppDatabase.getDatabase(getApplication())
                val localAdmin = db.userDao().getUserByEmail("mdmorshedalamhridayy@gmail.com")
                if (localAdmin == null) {
                    db.userDao().insertUser(
                        UserEntity(
                            name = "Morshed Hriday",
                            email = "mdmorshedalamhridayy@gmail.com",
                            passwordHash = "1b2e4m5r6h7e",
                            role = "Admin"
                        )
                    )
                }
            }

            // Sync/Verify with Firebase Authentication
            val authInstance = FirebaseHelper.auth
            if (authInstance != null) {
                try {
                    authInstance.signInWithEmailAndPassword(targetEmail, passwordHash)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Successfully authenticated with Firebase Auth!")
                            } else {
                                Log.e(TAG, "Firebase Auth sign-in failed: ${task.exception?.message}")
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase Auth execution failed: ${e.message}")
                }
            }

            val user = repository.authenticateUser(targetEmail, passwordHash)
            if (user != null) {
                if (user.isBanned) {
                    _authError.value = "Your account is temporarily suspended."
                } else {
                    _currentUser.value = user
                    repository.logAnalyticsEvent(user.id, "login", user.id)
                    _currentScreen.value = "home"
                }
            } else {
                _authError.value = "Invalid email or password combination."
            }
        }
    }

    fun register(name: String, email: String, passwordHash: String, role: String) {
        viewModelScope.launch {
            _authError.value = null
            if (name.isEmpty() || email.isEmpty() || passwordHash.isEmpty()) {
                _authError.value = "Please fill in all input fields."
                return@launch
            }
            try {
                // Sync/Register user in Firebase Authentication
                val authInstance = FirebaseHelper.auth
                if (authInstance != null) {
                    try {
                        authInstance.createUserWithEmailAndPassword(email, passwordHash)
                            .addOnSuccessListener {
                                Log.d(TAG, "Registered user in Firebase Auth successfully!")
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "Firebase Auth registration failed: ${e.message}")
                    }
                }

                val newUser = repository.registerUser(name, email, passwordHash, role)
                _currentUser.value = newUser
                repository.logAnalyticsEvent(newUser.id, "register", newUser.id)
                _currentScreen.value = "home"
            } catch (e: Exception) {
                _authError.value = "Registration fails: email may already be in use."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = "auth"
    }

    // Toggle Favorite Action
    fun toggleFavorite(contentId: Long, contentType: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.toggleFavorite(userId, contentId, contentType)
        }
    }

    fun isFavorite(contentId: Long, contentType: String): Boolean {
        val user = _currentUser.value ?: return false
        val list = favorites.value
        return list.any { it.contentId == contentId && it.contentType == contentType }
    }

    // Set Active Video/Channel for Media3 Exoplayer playback
    fun playChannel(channel: ChannelEntity) {
        _playbackVideo.value = null
        _playbackChannel.value = channel
        _currentScreen.value = "player"
        
        currentUser.value?.let { user ->
            viewModelScope.launch {
                repository.logAnalyticsEvent(user.id, "view_channel", channel.id)
            }
        }
    }

    fun playVideo(video: VideoEntity) {
        _playbackChannel.value = null
        _playbackVideo.value = video
        _currentScreen.value = "player"
        
        currentUser.value?.let { user ->
            viewModelScope.launch {
                repository.logAnalyticsEvent(user.id, "view_video", video.id)
            }
        }
    }

    // Simulate Bandwidth auto resolution optimization
    fun updatePlayerResolution(quality: String) {
        selectedResolution.value = quality
    }

    // Offline download support: simulates download process and saves flag
    fun downloadVideo(video: VideoEntity) {
        val userId = _currentUser.value?.id ?: return
        if (video.isDownloaded) return
        
        viewModelScope.launch {
            _downloadingIds.update { it + video.id }
            
            // Log analytics
            repository.logAnalyticsEvent(userId, "download", video.id)
            
            // Simulate background download with delay
            kotlinx.coroutines.delay(3500)
            
            val updatedVideo = video.copy(
                isDownloaded = true,
                localFileUri = "content://mylivetv/offline/downloads/${video.id}.mp4"
            )
            repository.updateVideo(updatedVideo)
            
            _downloadingIds.update { it - video.id }
            
            // Dispatch a real Android system notification indicating successful offline download!
            sendLocalNotification(
                "Offline Download Complete",
                "Successfully saved '${video.title}' for offline viewing."
            )
        }
    }

    private fun sendLocalNotification(title: String, message: String) {
        val context = getApplication<Application>().applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "mylivetv_download_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Media Downloads",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when videos are saved offline."
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
