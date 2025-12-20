package com.example.tramut.viewModel

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Users
import com.example.tramut.rooms.repo.UsersRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
class LoginViewModel(
    private val usersRepo: UsersRepo
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<Users?>(null)
    val currentUser: StateFlow<Users?> = _currentUser

    private val _studentIdValid = MutableStateFlow<Boolean?>(null)
    val studentIdValid: StateFlow<Boolean?> = _studentIdValid

    private val _staffIdValid = MutableStateFlow<Boolean?>(null)
    val staffIdValid: StateFlow<Boolean?> = _staffIdValid

    private val _studentLoginError = MutableStateFlow(false)
    val studentLoginError: StateFlow<Boolean> = _studentLoginError

    private val _staffLoginError = MutableStateFlow(false)
    val staffLoginError: StateFlow<Boolean> = _staffLoginError

    private val _isStudentLoggedIn  = MutableStateFlow(false)
    val isStudentLoggedIn  : StateFlow<Boolean> = _isStudentLoggedIn

    private val _isStaffLoggedIn   = MutableStateFlow(false)
    val isStaffLoggedIn  : StateFlow<Boolean> = _isStaffLoggedIn

    private val _idValid = MutableStateFlow<Boolean?>(null)
    val idValid: StateFlow<Boolean?> = _idValid
    private val _showLoginError = MutableStateFlow(false)
    val showLoginError: StateFlow<Boolean> = _showLoginError



    val users: StateFlow<List<Users>> =
        usersRepo.getAllUsers().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        syncUserFirebase()
    }

    private fun syncUserFirebase() {
        viewModelScope.launch {
            usersRepo.syncFromFirebase()
        }
    }

    fun checkStudentId(input: String) {
        if (input.isEmpty()) {
            _studentIdValid.value = null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val exists = usersRepo.checkStudentExists(input)
            withContext(Dispatchers.Main) {
                _studentIdValid.value = exists
            }
        }
    }

    fun checkStaffId(input: String) {
        if (input.isEmpty()) {
            _staffIdValid.value = null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val exists = usersRepo.checkStaffExists(input)
            withContext(Dispatchers.Main) {
                _staffIdValid.value = exists
            }
        }
    }

    fun login(loginId: String, password: String, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = usersRepo.getUserByLoginIdAndRole(loginId, role)
            if (user == null) {
                if (role == "Student")
                    _studentLoginError.value = true
                else
                    _staffLoginError.value = true
                onResult(false)
                return@launch
            }

            try {
                auth.signInWithEmailAndPassword(user.email, password).await()
                _currentUser.value = user
                if (role == "Student") {
                    _isStudentLoggedIn.value = true
                    _studentLoginError.value = false
                }
                else{
                    _isStaffLoggedIn.value = true
                    _staffLoginError.value = false
                }
                usersRepo.syncFromFirebase()
                onResult(true)
            } catch (e: Exception) {
                if (role == "Student")
                    _studentLoginError.value = true
                else
                    _staffLoginError.value = true
                onResult(false)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _isStudentLoggedIn.value = false
        _isStaffLoggedIn.value = false
    }


}