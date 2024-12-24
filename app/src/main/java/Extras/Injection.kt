package Extras

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object Injection {
    private val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun instance(): FirebaseFirestore {
        return instance
    }

    // Example for providing a custom instance with specific settings
    fun customInstance(customSettings: FirebaseFirestoreSettings): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = customSettings
        }
    }
}
