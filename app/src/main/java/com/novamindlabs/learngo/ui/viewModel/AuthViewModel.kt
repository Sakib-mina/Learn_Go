package com.novamindlabs.learngo.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.data.model.UserRegister
import com.novamindlabs.learngo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signupState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle)
    val signupState: StateFlow<Resource<FirebaseUser>> = _signupState

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle)
    val loginState: StateFlow<Resource<FirebaseUser>> = _loginState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userName = MutableStateFlow("Learner")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _userCoins = MutableStateFlow(0)
    val userCoins: StateFlow<Int> = _userCoins

    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount

    private val _dailyTaskCount = MutableStateFlow(0)
    val dailyTaskCount: StateFlow<Int> = _dailyTaskCount

    private val _purchasedQuizzes = MutableStateFlow<List<String>>(emptyList())
    val purchasedQuizzes: StateFlow<List<String>> = _purchasedQuizzes

    private val _quizState = MutableStateFlow<Resource<List<Map<String, Any>>>>(Resource.Idle)
    val quizState: StateFlow<Resource<List<Map<String, Any>>>> = _quizState

    private val _purchaseStatus = MutableSharedFlow<Resource<String>>()
    val purchaseStatus: SharedFlow<Resource<String>> = _purchaseStatus

    private val _deductStatus = MutableSharedFlow<Resource<Unit>>()
    val deductStatus: SharedFlow<Resource<Unit>> = _deductStatus

    init {
        checkUserAndStartListener(repository.getCurrentUser())
    }

    private fun checkUserAndStartListener(user: FirebaseUser?) {
        _currentUser.value = user
        user?.let {
            startUserDataListener(it.uid)
        }
    }

    private fun startUserDataListener(uid: String) {
        repository.getUserDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val lastUpdate = snapshot.getLong("lastTaskDate") ?: 0L
                if (isNewDay(lastUpdate)) {
                    viewModelScope.launch {
                        repository.resetDailyTask(uid)
                    }
                    return@addSnapshotListener
                }

                _userName.value = snapshot.getString("name") ?: "Learner"
                _userEmail.value = snapshot.getString("email") ?: ""

                _userCoins.value = (snapshot.get("coins") as? Long)?.toInt() ?: 0
                _streakCount.value = (snapshot.get("streakCount") as? Long)?.toInt() ?: 0

                @Suppress("UNCHECKED_CAST")
                val purchased = snapshot.get("purchasedQuizzes") as? List<String> ?: emptyList()
                _purchasedQuizzes.value = purchased

                val taskRaw = snapshot.get("dailyTaskCount")
                _dailyTaskCount.value = (taskRaw as? Long)?.toInt() ?: 0
            }
        }
    }

    private fun isNewDay(lastUpdate: Long): Boolean {
        if (lastUpdate == 0L) return false
        val lastDate = Calendar.getInstance().apply { timeInMillis = lastUpdate }
        val currentDate = Calendar.getInstance()

        return lastDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR) ||
                lastDate.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)
    }

    fun purchaseQuizCategory(category: String) {
        viewModelScope.launch {
            _purchaseStatus.emit(Resource.Loading())
            val result = repository.purchaseQuiz(category)
            _purchaseStatus.emit(result)
        }
    }

    fun fetchQuizzes(category: String) {
        viewModelScope.launch {
            _quizState.value = Resource.Loading()
            _quizState.value = repository.getQuizzesByCategory(category)
        }
    }

    fun isPurchased(category: String): Boolean {
        val freeQuizzes = listOf(
            "Islamic",
            "Prophets_Quiz",
            "Namaj_Quiz",
            "Sports",
            "History",
            "BD_History",
            "Sahaba_Life",
            "Riddles",
            "Computer_IT",
            "Animal_World"
        )
        if (freeQuizzes.contains(category)) return true

        return _purchasedQuizzes.value.contains(category)
    }

    fun incrementDailyTask() {
        viewModelScope.launch {
            repository.incrementDailyTaskCount()
        }
    }

    fun updateCoins(coinsToAdd: Int) {
        _currentUser.value?.uid?.let { uid ->
            viewModelScope.launch {
                repository.updateUserCoins(uid, coinsToAdd)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.loginUser(email, password)
            _loginState.value = result
            if (result is Resource.Success) checkUserAndStartListener(result.data)
        }
    }

    fun register(userReq: UserRegister) {
        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            val result = repository.registerUser(userReq)
            _signupState.value = result
            if (result is Resource.Success) checkUserAndStartListener(result.data)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            val result = repository.signInWithGoogle(idToken)
            _signupState.value = result
            if (result is Resource.Success) checkUserAndStartListener(result.data)
        }
    }

    fun checkAutoLogin(): Boolean {
        val user = repository.getCurrentUser()
        return if (user != null) {
            _currentUser.value = user
            startUserDataListener(user.uid)
            true
        } else false
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _userName.value = "Learner"
        _userCoins.value = 0
        _streakCount.value = 0
        _dailyTaskCount.value = 0
        _userEmail.value = ""
        _purchasedQuizzes.value = emptyList()
        _loginState.value = Resource.Idle
        _signupState.value = Resource.Idle
    }
}