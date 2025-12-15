package com.example.tramut.rooms.repo

import android.util.Log
import android.util.Log.e
import com.example.tramut.rooms.dao.UsersDAO
import com.example.tramut.rooms.entity.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersRepo(
    private val usersDao: UsersDAO
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // -----------------------------
    // 1️⃣ Room 本地数据
    // -----------------------------
    fun getAllUsers(): Flow<List<Users>> = usersDao.getAllUsers()
    suspend fun insert(user: Users) = usersDao.insertUser(user)
    suspend fun update(user: Users) = usersDao.updateUser(user)
    suspend fun delete(user: Users) = usersDao.deleteUser(user)
    suspend fun deleteAll() = usersDao.deleteAllUsers()
    suspend fun getUserByLoginId(loginId: String) = usersDao.getUserByLoginId(loginId)
    suspend fun getUserByLoginIdAndRole(loginId: String, role: String) =
        usersDao.getUserByLoginIdAndRole(loginId, role)


    // -----------------------------
    // 2️⃣ FirebaseAuth 登录/注册
    // -----------------------------
    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() = auth.signOut()
    fun currentUserEmail(): String? = auth.currentUser?.email

    // -----------------------------
    // 3️⃣ Firestore 同步 Room
    // -----------------------------
    suspend fun syncFromFirebase() = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("users").get().await()
        val users = snapshot.documents.mapNotNull { doc ->
            val loginId = doc.getString("loginId") ?: return@mapNotNull null
            Users(
                loginId = loginId,
                username = doc.getString("username") ?: "",
                email = doc.getString("email") ?: "",
                userIC = doc.getString("IC") ?: "",
                role = doc.getString("role") ?: ""
            )
        }
        usersDao.replaceAllUsers(users)
    }

    suspend fun updateUserFirestore(user: Users) = withContext(Dispatchers.IO) {
        firestore.collection("users")
            .document(user.loginId)
            .set(
                mapOf(
                    "username" to user.username,
                    "email" to user.email,
                    "IC" to user.userIC,
                    "role" to user.role
                )
            ).await()
        usersDao.insertUser(user)
    }

    // -----------------------------
    // 4️⃣ 忘记密码（用 email + IC 验证）
    // -----------------------------

    suspend fun sendPasswordResetEmail(email: String, inputIC: String): Boolean =
        withContext(Dispatchers.IO) {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("IC", inputIC)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext false

            auth.sendPasswordResetEmail(email).await()
            true
        }

    suspend fun resetPassword(oobCode: String, newPassword: String): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                // ✅ 使用 .await() 确保协程等待 Firebase 操作完成
                auth.confirmPasswordReset(oobCode, newPassword).await()
                true
            } catch (e: Exception) {
                Log.e("UsersRepo", "Reset failed", e)
                // ❌ 如果发生错误（例如网络问题、oobCode无效/过期），则返回 false
                false
            }
        }


    // -----------------------------
    // 5️⃣ 检查用户是否存在
    // -----------------------------

    suspend fun checkStudentExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Student") != null

    suspend fun checkStaffExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Staff") != null

}