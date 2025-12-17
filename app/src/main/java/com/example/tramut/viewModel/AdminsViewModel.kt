package com.example.myfacilitybookingsystem.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.AdminUser
import com.example.tramut.rooms.entity.Review
import com.example.tramut.rooms.repo.AdminRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminsViewModel : ViewModel() {

    private val repository = AdminRepository()
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // Holds the currently logged-in Admin's details
    var adminUser = mutableStateOf<AdminUser?>(null)
        private set

    // UI States
    var isLoading = mutableStateOf(false)
        private set

    var loginError = mutableStateOf(false)
        private set

    // --- LOGIN LOGIC ---
    fun login(loginId: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            loginError.value = false

            // Call Repository to check Firebase
            // Note: Repository handles mapping "loginId" -> "admin_id"
            val user = repository.loginAdmin(loginId, pass)

            if (user != null) {
                adminUser.value = user
                onSuccess()
            } else {
                loginError.value = true
            }
            isLoading.value = false
        }
    }

    // --- LOGOUT LOGIC ---
    fun performLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            adminUser.value = null // Clear local state
            onLogoutSuccess()
        }
    }

    fun fetchReviewsByDepartment(dept: String) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("department", dept) // THIS IS THE KEY
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = snapshot.toObjects(Review::class.java)
                    _reviews.value = list
                }
            }
    }
}