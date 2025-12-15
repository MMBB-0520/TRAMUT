package com.example.myfacilitybookingsystem.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.AdminUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("admins")

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun logout() = auth.signOut()

    suspend fun getAdminDetails(adminId: String): AdminUser? {
        return try {
            val doc = collection.document(adminId).get().await()
            if (doc.exists()) doc.toObject(AdminUser::class.java) else null
        } catch (e: Exception) { null }
    }

    suspend fun loginAdmin(loginIdInput: String, passwordInput: String): AdminUser? {
        return try {
            // FIX: Changed "login_id" -> "admin_id" and "password" -> "admin_password"
            // to match your Firebase fields exactly.
            val query = collection
                .whereEqualTo("admin_id", loginIdInput)
                .whereEqualTo("admin_password", passwordInput)
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents[0]
                // We map the document data to your Data Class here
                doc.toObject(AdminUser::class.java)?.copy(id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}