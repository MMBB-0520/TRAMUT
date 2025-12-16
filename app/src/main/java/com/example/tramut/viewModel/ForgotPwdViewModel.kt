package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.repo.UsersRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPwdViewModel (
    private val usersRepo: UsersRepo
) : ViewModel() {

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _lastRequestedEmail = MutableStateFlow<String?>(null)
    val lastRequestedEmail: StateFlow<String?> = _lastRequestedEmail

    private val emailRegex =
        Regex("^[a-zA-Z0-9._%+-]+@(student\\.tarc\\.edu\\.my|tarc\\.edu\\.my)$")

    fun hasMinLength(pwd: String): Boolean = pwd.length >= 8
    fun hasLowerCase(pwd: String): Boolean = pwd.any { it.isLowerCase() }
    fun hasUpperCase(pwd: String): Boolean = pwd.any { it.isUpperCase() }
    fun hasNumberOrSpecial(pwd: String): Boolean =
        pwd.any { it.isDigit() || !it.isLetterOrDigit() }

    fun requestPasswordReset(
        email: String,
        ic: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank()) {
            _emailError.value = null
            return
        }

        if (!emailRegex.matches(email)) {
            _emailError.value = "Invalid TARUMT email format"
            return
        }


        viewModelScope.launch {
            val success = usersRepo.sendPasswordResetEmail(email, ic)

            if (!success) {
                _emailError.value =
                    "Please make sure your registered email and IC Number are correct."
            } else {
                _lastRequestedEmail.value = email
                _emailError.value = null
                onSuccess()
            }
        }
    }

    fun resendResetEmail() {
        val email = _lastRequestedEmail.value

        if (email.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            usersRepo.resendPassword(email)
        }
    }
    fun resetPassword(oobCode: String, newPassword: String, onSuccess: () -> Unit) {

        viewModelScope.launch {
            val success = usersRepo.resetPassword(oobCode, newPassword)

            if (!success) {
                _errorMessage.value = "Failed to reset password"
            } else {
                _errorMessage.value = null
                onSuccess()
            }


        }
    }
}