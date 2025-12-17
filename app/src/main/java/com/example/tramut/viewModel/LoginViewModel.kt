package com.example.tramut.viewModel

import android.util.Log.e
import androidx.compose.runtime.mutableStateListOf
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

    private val _invalid = MutableStateFlow<Boolean?>(null)
    val invalid: StateFlow<Boolean?> = _invalid


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
    // 存储成员的信息
    var members = mutableStateListOf<Pair<String, String>>()
        private set // 保持封装性
    var validationResults = mutableListOf<Boolean>()
        private set


    // 验证成员的 ID 和用户名
    fun validateMembers() {
        viewModelScope.launch {
            validationResults.clear()  // 清空之前的验证结果

            // 遍历每个成员并验证
            for (member in members) {
                val loginId = member.first
                val username = member.second

                // 对每个成员进行验证，首先检查 loginId 和 role 是否有效
                val isLoginIdValid = usersRepo.checkUserByLoginId(loginId, "Student")

                // 如果 loginId 有效，再检查 username 是否匹配
                if (isLoginIdValid) {
                    val isUsernameValid = usersRepo.checkLoginIdAndUsername(loginId, username)
                    validationResults.add(isUsernameValid)
                    _invalid.value = true
                } else {
                    // 如果 loginId 无效，用户名也会被认为无效
                    validationResults.add(false)
                    _invalid.value = false
                }
            }
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