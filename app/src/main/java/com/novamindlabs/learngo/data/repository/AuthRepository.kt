package com.novamindlabs.learngo.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.novamindlabs.learngo.core.Nodes
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.data.model.UserRegister
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val freeQuizCategories = listOf(
        "Hadis_Quiz",
        "Prophets_Quiz",
        "Namaj_Quiz",
        "Quran_Quiz"
    )

    suspend fun registerUser(userReq: UserRegister): Resource<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(userReq.email, userReq.password).await()
        val firebaseUser = result.user!!

        val userData = hashMapOf(
            "uid" to firebaseUser.uid,
            "name" to userReq.name,
            "email" to userReq.email,
            "coins" to 50L,
            "dailyTaskCount" to 0L,
            "purchasedQuizzes" to listOf<String>(),
            "lastTaskDate" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection(Nodes.USER).document(firebaseUser.uid).set(userData).await()
        Resource.Success(firebaseUser)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে")
    }

    suspend fun signInWithGoogle(idToken: String): Resource<FirebaseUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user!!

        val docRef = firestore.collection(Nodes.USER).document(user.uid)
        val document = docRef.get().await()

        if (!document.exists()) {
            val userData = hashMapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: "Learner"),
                "email" to user.email,
                "coins" to 50L,
                "dailyTaskCount" to 0L,
                "purchasedQuizzes" to listOf<String>(),
                "lastTaskDate" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis()
            )
            docRef.set(userData).await()
        }
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "গুগল লগইন ব্যর্থ হয়েছে")
    }

    suspend fun loginUser(email: String, password: String): Resource<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user
        if (user != null) Resource.Success(user)
        else Resource.Error("ইউজার পাওয়া যায়নি")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "লগইন ব্যর্থ হয়েছে")
    }

    suspend fun updateUserCoins(uid: String, coinsToAdd: Int): Resource<Unit> = try {
        firestore.collection(Nodes.USER).document(uid)
            .update("coins", FieldValue.increment(coinsToAdd.toLong()))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("কয়েন আপডেট করা সম্ভব হয়নি")
    }

    suspend fun purchaseQuiz(category: String): Resource<String> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("ইউজার লগইন করা নেই")
        val userRef = firestore.collection(Nodes.USER).document(uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCoins = snapshot.getLong("coins") ?: 0L
            val purchasedList = snapshot.get("purchasedQuizzes") as? List<String> ?: listOf()

            if (purchasedList.contains(category)) {
                return@runTransaction "Already Purchased"
            }

            if (currentCoins >= 20) {
                transaction.update(userRef, "coins", currentCoins - 20)
                transaction.update(userRef, "purchasedQuizzes", FieldValue.arrayUnion(category))
                "Purchase Successful"
            } else {
                throw Exception("পর্যাপ্ত কয়েন নেই")
            }
        }.await()

        Resource.Success("Purchase Successful")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "ক্রয় সম্পন্ন করা সম্ভব হয়নি")
    }

    suspend fun isQuizPurchased(category: String): Boolean {
        if (freeQuizCategories.contains(category)) return true

        val uid = auth.currentUser?.uid ?: return false
        return try {
            val document = firestore.collection(Nodes.USER).document(uid).get().await()
            val purchasedList = document.get("purchasedQuizzes") as? List<String> ?: listOf()
            purchasedList.contains(category)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getQuizzesByCategory(category: String): Resource<List<Map<String, Any>>> = try {
        val isPurchased = isQuizPurchased(category)

        if (!isPurchased) {
            Resource.Error("এই কুইজটি আগে আনলক করুন!")
        } else {
            val snapshot = firestore.collection("Quizzes").document(category)
                .collection("Questions")
                .get().await()

            val list = snapshot.documents.mapNotNull { it.data }
            if (list.isNotEmpty()) {
                Resource.Success(list.shuffled())
            } else {
                Resource.Error("এই ক্যাটাগরিতে কোনো প্রশ্ন পাওয়া যায়নি।")
            }
        }
    } catch (e: Exception) {
        Resource.Error("প্রশ্ন লোড করতে সমস্যা হয়েছে")
    }

    suspend fun incrementDailyTaskCount(): Resource<Unit> = try {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection(Nodes.USER).document(uid)
                .update("dailyTaskCount", FieldValue.increment(1))
                .await()
            Resource.Success(Unit)
        } else {
            Resource.Error("ইউজার লগইন নেই")
        }
    } catch (e: Exception) {
        Resource.Error("টাস্ক আপডেট ব্যর্থ")
    }


    suspend fun resetDailyTask(uid: String): Resource<Unit> = try {
        firestore.collection(Nodes.USER).document(uid)
            .update(
                mapOf(
                    "dailyTaskCount" to 0L,
                    "lastTaskDate" to System.currentTimeMillis()
                )
            ).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("রিসেট ব্যর্থ")
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    fun getUserDocument(uid: String) = firestore.collection(Nodes.USER).document(uid)
    fun logout() = auth.signOut()
}