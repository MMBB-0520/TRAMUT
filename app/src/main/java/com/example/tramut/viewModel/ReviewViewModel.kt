package com.example.tramut.viewModel


import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.entity.Review
import com.google.firebase.auth.FirebaseAuth
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
        userId: String?,
        booking: Booking,
        category: String,
        description: String,
        onReviewSubmitted: () -> Unit
    ) {
        if (userId.isNullOrBlank()) {
            Log.e("Review", "User ID is null or blank")
            return
        }
        val docRef = db.collection("reviews").document()
        val reviewData = hashMapOf(
            "reviewId" to docRef.id,
            "bookingId" to booking.bookingId,
            "bookingDate" to booking.date,
            "venue" to booking.venue,
            "venueType" to booking.venue,
            "loginId" to userId,
            "issueCategory" to category,
            "comment" to description,
            "status" to "Unresolved",
            "department" to booking.facility
        )


        db.collection("reviews")
            .add(reviewData)
            .addOnSuccessListener {
                onReviewSubmitted()
            }
            .addOnFailureListener {}
    }
    fun fetchMyReviews(userId: String?) {
        if (userId.isNullOrBlank()) {
            _reviews.value = emptyList()
            return
        }


        FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("loginId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Review", "获取失败: ${error.message}")
                    return@addSnapshotListener
                }


                val list = snapshot?.documents?.mapNotNull { doc ->
                    Log.d("Review", "抓取到原始数据: ${doc.data}")
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
                        Booking(
                            bookingId = doc.id,
                            facility = doc.getString("facility") ?: "",
                            date = doc.getString("date") ?: "",
                            venue = doc.getString("venue") ?: "",

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

