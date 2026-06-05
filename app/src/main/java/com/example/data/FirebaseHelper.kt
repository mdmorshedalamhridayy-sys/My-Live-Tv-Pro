package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    private const val TAG = "FirebaseHelper"
    private var isFirebaseInitialized = false

    fun initFirebase(context: Context) {
        if (isFirebaseInitialized) return
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                isFirebaseInitialized = true
                Log.d(TAG, "Firebase already initialized by system/content-provider")
                return
            }

            // Programmatic configuration based on user's exact parameters!
            val options = FirebaseOptions.Builder()
                .setProjectId("live-match-tv")
                .setApplicationId("1:1036414524761:android:3c80355258b2fff97f0048")
                .setApiKey("AIzaSyCdZ021bWAUUjrQmvBqfpozIJz-LM2uGYQ")
                .setStorageBucket("live-match-tv.firebasestorage.app")
                .build()

            FirebaseApp.initializeApp(context, options)
            isFirebaseInitialized = true
            Log.d(TAG, "Firebase programmatically initialized successfully!")
            
            // Setup / seed the primary admin user in Firebase Auth and Firestore
            setupAdminAuth()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase programmatically: ${e.message}", e)
        }
    }

    private fun setupAdminAuth() {
        val authInstance = auth ?: return
        try {
            authInstance.createUserWithEmailAndPassword("mdmorshedalamhridayy@gmail.com", "1b2e4m5r6h7e")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Primary admin user seeded in Firebase Auth")
                        // Also seed/register in Firestore profile
                        firestore?.let { fs ->
                            val data = hashMapOf(
                                "name" to "Morshed Hriday",
                                "email" to "mdmorshedalamhridayy@gmail.com",
                                "role" to "Admin",
                                "is_banned" to false,
                                "last_login" to System.currentTimeMillis()
                            )
                            val uid = authInstance.currentUser?.uid ?: "firebase_admin"
                            fs.collection("users").document(uid).set(data)
                                .addOnSuccessListener { Log.d(TAG, "Firestore admin profileSynced") }
                        }
                    } else {
                        Log.d(TAG, "Firebase Auth admin user already exists or skipped: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding Admin Auth: ${e.message}")
        }
    }

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseInitialized) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Firestore instance: ${e.message}")
                null
            }
        } else null

    val auth: FirebaseAuth?
        get() = if (isFirebaseInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Auth instance: ${e.message}")
                null
            }
        } else null
}
