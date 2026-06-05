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
        
        // Listen to app updates from Firestore
        listenToAppUpdates()
        
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

    // App Update properties
    val currentAppVersion = "1.0.0"
    
    private val _latestAppVersion = MutableStateFlow("1.0.0")
    val latestAppVersion: StateFlow<String> = _latestAppVersion.asStateFlow()
    
    private val _updateMessage = MutableStateFlow("Sleek performance enhancements and newly loaded direct streaming matching channels.")
    val updateMessage: StateFlow<String> = _updateMessage.asStateFlow()
    
    private val _updateUrl = MutableStateFlow("https://mylivetvapp-update-links.com/download")
    val updateUrl: StateFlow<String> = _updateUrl.asStateFlow()
    
    private val _isUpdateMandatory = MutableStateFlow(false)
    val isUpdateMandatory: StateFlow<Boolean> = _isUpdateMandatory.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun listenToAppUpdates() {
        FirebaseHelper.firestore?.collection("app_config")?.document("update_info")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to app updates in Firestore", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val latestVer = snapshot.getString("latest_version") ?: "1.0.0"
                    val msg = snapshot.getString("update_message") ?: "A new update is available with sleek features."
                    val url = snapshot.getString("update_url") ?: "https://mylivetvapp-update-links.com/download"
                    val isMandatory = snapshot.getBoolean("is_mandatory") ?: false
                    
                    _latestAppVersion.value = latestVer
                    _updateMessage.value = msg
                    _updateUrl.value = url
                    _isUpdateMandatory.value = isMandatory
                    
                    if (isVersionGreater(latestVer, currentAppVersion)) {
                        _showUpdateDialog.value = true
                    }
                }
            }
    }

    fun publishAppUpdate(version: String, message: String, url: String, isMandatory: Boolean) {
        viewModelScope.launch {
            FirebaseHelper.firestore?.let { fs ->
                val data = hashMapOf(
                    "latest_version" to version.trim(),
                    "update_message" to message.trim(),
                    "update_url" to url.trim(),
                    "is_mandatory" to isMandatory,
                    "updated_at" to System.currentTimeMillis()
                )
                fs.collection("app_config").document("update_info").set(data)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully published app update config to Firestore!")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to publish app update config to Firestore: ${e.message}")
                    }
            }
            
            _latestAppVersion.value = version
            _updateMessage.value = message
            _updateUrl.value = url
            _isUpdateMandatory.value = isMandatory
            
            if (isVersionGreater(version, currentAppVersion)) {
                _showUpdateDialog.value = true
            }
        }
    }

    private fun isVersionGreater(latest: String, current: String): Boolean {
        return try {
            val latestParts = latest.trim().split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.trim().split(".").map { it.toIntOrNull() ?: 0 }
            for (i in 0 until minOf(latestParts.size, currentParts.size)) {
                if (latestParts[i] > currentParts[i]) return true
                if (latestParts[i] < currentParts[i]) return false
            }
            latestParts.size > currentParts.size
        } catch (e: Exception) {
            latest != current
        }
    }

    // --- Subscription & Payment Gateway flows/actions ---
    val gatewayNumbers: StateFlow<List<GatewayNumberEntity>> = repository.gatewayNumbersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentRequests: StateFlow<List<PaymentRequestEntity>> = repository.allPaymentRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPaymentRequests: StateFlow<List<PaymentRequestEntity>> = repository.allPaymentRequestsFlow
        .map { list ->
            val curId = _currentUser.value?.id ?: -1L
            list.filter { it.userId == curId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshCurrentUser() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = database.userDao().getUserById(user.id)
            if (updatedUser != null) {
                _currentUser.value = updatedUser
            }
        }
    }

    fun submitPaymentRequest(planName: String, planDays: Int, price: Int, provider: String, senderNumber: String, trxId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val request = PaymentRequestEntity(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                planName = planName,
                validityDays = planDays,
                price = price,
                provider = provider,
                senderNumber = senderNumber,
                transactionId = trxId,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.createPaymentRequest(request)
            repository.logAnalyticsEvent(user.id, "payment_submit_${planName.replace(" ", "_")}", user.id)
            refreshCurrentUser()
        }
    }

    fun approvePaymentRequest(reqId: Long) {
        viewModelScope.launch {
            repository.updatePaymentRequestStatus(reqId, "Approved")
            refreshCurrentUser()
        }
    }

    fun rejectPaymentRequest(reqId: Long) {
        viewModelScope.launch {
            repository.updatePaymentRequestStatus(reqId, "Rejected")
            refreshCurrentUser()
        }
    }

    fun purchaseSubscription(planName: String, planDays: Int, price: Int, provider: String, senderNumber: String, trxId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val expiryTime = if (planDays == -1) {
                Long.MAX_VALUE
            } else {
                System.currentTimeMillis() + (planDays.toLong() * 24L * 60L * 60L * 1000L)
            }
            repository.updateUserSubscription(
                userId = user.id,
                plan = planName,
                expiry = expiryTime,
                status = "Active"
            )
            repository.logAnalyticsEvent(user.id, "purchase_subscription_${planName.replace(" ", "_")}_idx", user.id)
            refreshCurrentUser()
        }
    }


    fun addGatewayNumberByAdmin(provider: String, number: String, type: String) {
        viewModelScope.launch {
            repository.addGatewayNumber(
                GatewayNumberEntity(
                    provider = provider,
                    number = number,
                    type = type,
                    isAvailable = true
                )
            )
        }
    }

    fun removeGatewayNumberByAdmin(id: Long) {
        viewModelScope.launch {
            repository.deleteGatewayNumber(id)
        }
    }
}
