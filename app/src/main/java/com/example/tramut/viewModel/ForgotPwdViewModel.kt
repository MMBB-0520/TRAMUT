package com.example.tramut.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.repo.UsersRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPwdViewModel (
    private val usersRepo: UsersRepo
) : ViewModel() {

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _isChecking = MutableStateFlow<Boolean?>(false)
    val isChecking: StateFlow<Boolean?> = _isChecking
    private val EmailRegex =
        Regex("^[a-zA-Z0-9._%+-]+@(student\\.tarc\\.edu\\.my|tarc\\.edu\\.my)$")


    suspend fun checkEmailValidAndExists(email: String): Boolean {
        // Step 1: 格式验证
        if (!EmailRegex.matches(email)) {
            _emailError.value = "Invalid TARC email format"
            return false
        }

        _isChecking.value = true
        val user = usersRepo.findByEmail(email)
        _isChecking.value = false

        return if (user != null) {
            _emailError.value = null
            true
        } else {
            _emailError.value = "Email not found"
            false
        }
    }



    /** 忘记密码：通过 loginId + IC 验证发送重置邮件 */
    fun sendPasswordReset(loginId: String, inputIC: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = usersRepo.sendPasswordResetEmail(loginId, inputIC)
            onResult(success)
        }
    }
}