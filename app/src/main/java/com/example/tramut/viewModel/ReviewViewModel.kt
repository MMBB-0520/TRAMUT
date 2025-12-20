package com.example.tramut.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.entity.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReviewViewModel : ViewModel(){
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings
    private val db = FirebaseFirestore.getInstance()

    fun submitReview(
        booking: Booking,
        category: String,
        description: String
    ) {

        val user = FirebaseAuth.getInstance().currentUser ?: run {
            return
        }
        val docRef = db.collection("reviews").document() // 先生成 ID

        val reviewData = hashMapOf(
            "reviewId" to docRef.id,
            "userId" to user.uid,
            "bookingId" to booking.bookingId,
            "facility" to booking.facility,
            "venue" to booking.venue,
            "date" to booking.date,
            "category" to category,
            "description" to description,
            "status" to "Unresolved"
        )

        db.collection("reviews")
            .add(reviewData)
}
    fun fetchMyReviews() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("Review", "用户未登录")
            _reviews.value = emptyList()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("reviews") // 确保这里的名字和数据库一模一样
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error -> // 建议用监听器，实时更新
                if (error != null) {
                    Log.e("Review", "获取失败: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    Log.d("Review", "抓取到原始数据: ${doc.data}")
                    // 注意：不要用 doc.id 覆盖 userId，除非你想保存文档 ID
                    doc.toObject(Review::class.java)
                } ?: emptyList()

                _reviews.value = list
                Log.d("Review", "列表长度: ${list.size}")
            }
    }

    fun fetchMyBookings(userId: String?) {
        if (userId.isNullOrBlank()) {
            _bookings.value = emptyList()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        // 不使用 toObject，而是手动从 map 中提取你要的字段
                        Booking(
                            bookingId = doc.id, // 或者 doc.getString("bookingId") ?: ""
                            facility = doc.getString("facility") ?: "",
                            date = doc.getString("date") ?: "",
                            venue = doc.getString("venue") ?: "",
                            // members 留空，不拿它，这样就不会因为类型不匹配崩溃
                        )
                    } catch (e: Exception) {
                        Log.e("Review", "解析单个文档失败: ${e.message}")
                        null
                    }
                }
                _bookings.value = list
            }
            .addOnFailureListener {
                _bookings.value = emptyList()
            }
    }


    fun filterByStatus(
        reviews: List<Review>,
        selectedTab: String
    ): List<Review> {
        return if (selectedTab == "All") {
            reviews
        } else {
            reviews.filter { it.status == selectedTab }
        }
    }
}