package com.example.myfacilitybookingsystem.rooms.repo

import com.example.tramut.rooms.dao.UsersDAO
import com.example.tramut.rooms.entity.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersRepo(
    private val usersDao: UsersDAO
){
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
    suspend fun getUserByLoginIdAndRole(loginId: String, role: String) = usersDao.getUserByLoginIdAndRole(loginId, role)


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
                userIC = doc.getString("userIC") ?: "",
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
                    "userIC" to user.userIC,
                    "role" to user.role
                )
            ).await()
        usersDao.insertUser(user)
    }

    // -----------------------------
    // 4️⃣ 忘记密码（用 loginId + IC 验证）
    // -----------------------------
    suspend fun sendPasswordResetEmail(loginId: String, inputIC: String): Boolean = withContext(Dispatchers.IO) {
        val doc = firestore.collection("users").document(loginId).get().await()
        if (!doc.exists()) return@withContext false
        val storedIC = doc.getString("userIC") ?: return@withContext false
        if (storedIC != inputIC) return@withContext false
        val email = doc.getString("email") ?: return@withContext false
        auth.sendPasswordResetEmail(email).await()
        true
    }
    suspend fun findByEmail(email: String) =
        usersDao.findByEmail(email) != null

    // -----------------------------
    // 5️⃣ 检查用户是否存在
    // -----------------------------

    suspend fun checkStudentExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Student") != null

    suspend fun checkStaffExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Staff") != null
}