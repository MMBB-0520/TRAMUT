package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyBookingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // StateFlow 用于 Compose UI 监听
    private val _bookingList = MutableStateFlow<List<Booking>>(emptyList())
    val bookingList: StateFlow<List<Booking>> = _bookingList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 添加UI状态管理
    private val _uiState = MutableStateFlow<BookingUIState>(BookingUIState.Idle)
    val uiState: StateFlow<BookingUIState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening(userId: String) {
        _isLoading.value = true

        listenerRegistration = firestore
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    // 处理错误
                    _uiState.value = BookingUIState.Error(error.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(
                            bookingNo = doc.getString("bookingId") ?: doc.id
                        )
                    }
                    _bookingList.value = bookings
                }
            }
    }

    // 取消预订的方法
    suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            _uiState.value = BookingUIState.Loading

            // 查找对应的文档ID
            val querySnapshot = firestore.collection("bookings")
                .whereEqualTo("bookingId", bookingId)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentId = querySnapshot.documents[0].id

                // 更新状态为"cancelled"
                firestore.collection("bookings").document(documentId)
                    .update("status", "cancelled")
                    .await()

                _uiState.value = BookingUIState.Success
                true
            } else {
                _uiState.value = BookingUIState.Error("Booking not found")
                false
            }
        } catch (e: Exception) {
            _uiState.value = BookingUIState.Error(e.message ?: "Failed to cancel booking")
            false
        }
    }

    // ViewModelScope封装的方法，方便在Compose中调用
    fun cancelBookingWithScope(bookingId: String) {
        viewModelScope.launch {
            cancelBooking(bookingId)
        }
    }

    fun resetUIState() {
        _uiState.value = BookingUIState.Idle
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // UI状态密封类
    sealed class BookingUIState {
        object Idle : BookingUIState()
        object Loading : BookingUIState()
        object Success : BookingUIState()
        data class Error(val message: String) : BookingUIState()
    }
}