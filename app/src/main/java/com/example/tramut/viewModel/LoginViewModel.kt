package com.example.tramut.viewModel

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

    // 当前登录用户
    private val _currentUser = MutableStateFlow<Users?>(null)
    val currentUser: StateFlow<Users?> = _currentUser

    // 学号/ID 是否存在
    private val _studentIdValid = MutableStateFlow<Boolean?>(null)
    val studentIdValid: StateFlow<Boolean?> = _studentIdValid

    private val _staffIdValid = MutableStateFlow<Boolean?>(null)
    val staffIdValid: StateFlow<Boolean?> = _staffIdValid

    private val _idValid = MutableStateFlow<Boolean?>(null)
    val idValid: StateFlow<Boolean?> = _idValid


    // 错误显示
    private val _studentLoginError = MutableStateFlow(false)
    val studentLoginError: StateFlow<Boolean> = _studentLoginError

    private val _staffLoginError = MutableStateFlow(false)
    val staffLoginError: StateFlow<Boolean> = _staffLoginError

    private val _showLoginError = MutableStateFlow(false)
    val showLoginError: StateFlow<Boolean> = _showLoginError




    // 所有用户列表 (Room 数据库)
    val users: StateFlow<List<Users>> =
        usersRepo.getAllUsers().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // App 启动时同步一次 Firebase 数据到 Room
        syncUserFirebase()
    }

    /** 同步 Firebase 数据到 Room */
    private fun syncUserFirebase() {
        viewModelScope.launch {
            usersRepo.syncFromFirebase()
        }
    }

    /** 检查用户ID是否存在 */
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
    /** 获取单个用户信息 */
    fun getUserByLoginId(loginId: String, onSuccess: (Users?) -> Unit) {
        viewModelScope.launch {
            val user = usersRepo.getUserByLoginId(loginId)
            onSuccess(user)
        }
    }

    /** 登录 */
    fun login(loginId: String, password: String, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = usersRepo.getUserByLoginIdAndRole(loginId, role)
            if (user == null) {
                // 对应角色不存在
                if (role == "Student") _studentLoginError.value = true
                else _staffLoginError.value = true
                onResult(false)
                return@launch
            }

            try {
                auth.signInWithEmailAndPassword(user.email, password).await()
                _currentUser.value = user
                if (role == "Student") _studentLoginError.value = false
                else _staffLoginError.value = false

                usersRepo.syncFromFirebase()
                onResult(true)
            } catch (e: Exception) {
                if (role == "Student") _studentLoginError.value = true
                else _staffLoginError.value = true
                onResult(false)
            }
        }
    }

    /** 登出 */
    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }


}