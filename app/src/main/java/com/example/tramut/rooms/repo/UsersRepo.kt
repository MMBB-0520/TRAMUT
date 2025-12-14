package com.example.myfacilitybookingsystem.rooms.repo

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.myfacilitybookingsystem.rooms.dao.UsersDAO
import com.example.myfacilitybookingsystem.rooms.entity.Users
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

    // -----------------------------
    // 5️⃣ 检查用户是否存在
    // -----------------------------

    suspend fun checkStudentExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Student") != null

    suspend fun checkStaffExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Staff") != null

    // -----------------------------
    // 6️⃣ 新增：验证成员功能
    // -----------------------------

    suspend fun validateSingleMember(loginId: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            // 首先从本地Room数据库检查
            val user = usersDao.getUserByLoginId(loginId)

            if (user != null) {
                // 用户存在于本地数据库
                ValidationResult.Success(user)
            } else {
                // 如果本地没有，检查Firestore
                val doc = firestore.collection("users").document(loginId).get().await()

                if (doc.exists()) {
                    // 用户存在于Firestore，同步到本地
                    val firestoreUser = Users(
                        loginId = loginId,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        userIC = doc.getString("userIC") ?: "",
                        role = doc.getString("role") ?: "Student"
                    )
                    usersDao.insertUser(firestoreUser)
                    ValidationResult.Success(firestoreUser)
                } else {
                    ValidationResult.Error("Student ID $loginId not found", loginId)
                }
            }
        } catch (e: Exception) {
            ValidationResult.Error("Error validating student ID: ${e.message}", loginId)
        }
    }

    /**
     * 批量验证多个成员
     * 返回一个Map，key是loginId，value是验证结果
     */
    suspend fun validateMultipleMembers(loginIds: List<String>): Map<String, ValidationResult> = withContext(Dispatchers.IO) {
        try {
            val results = mutableMapOf<String, ValidationResult>()

            // 分批处理，避免一次性查询太多
            val batches = loginIds.chunked(10)

            for (batch in batches) {
                // 先检查本地数据库
                batch.forEach { loginId ->
                    val localUser = usersDao.getUserByLoginId(loginId)
                    if (localUser != null) {
                        results[loginId] = ValidationResult.Success(localUser)
                    }
                }

                // 找出本地没有的用户
                val missingIds = batch.filter { !results.containsKey(it) }

                if (missingIds.isNotEmpty()) {
                    // 从Firestore批量查询
                    val querySnapshot = firestore.collection("users")
                        .whereIn("loginId", missingIds)
                        .get()
                        .await()

                    val foundDocs = querySnapshot.documents
                    val foundIds = foundDocs.mapNotNull { it.getString("loginId") }.toSet()

                    // 处理找到的用户
                    foundDocs.forEach { doc ->
                        val loginId = doc.getString("loginId") ?: return@forEach
                        val user = Users(
                            loginId = loginId,
                            username = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: "",
                            userIC = doc.getString("userIC") ?: "",
                            role = doc.getString("role") ?: "Student"
                        )
                        // 同步到本地数据库
                        usersDao.insertUser(user)
                        results[loginId] = ValidationResult.Success(user)
                    }

                    // 处理未找到的用户
                    val notFoundIds = missingIds.filter { it !in foundIds }
                    notFoundIds.forEach { loginId ->
                        results[loginId] = ValidationResult.Error("Student ID $loginId not found", loginId)
                    }
                }
            }

            return@withContext results
        } catch (e: Exception) {
            // 出错时返回所有ID都无效
            loginIds.associateWith {
                ValidationResult.Error("Validation error: ${e.message}", it)
            }
        }
    }

    suspend fun validateMembersWithDuplicates(members: List<Pair<String, String>>): MembersValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                // 提取loginIds并过滤空值
                val loginIds = members.map { it.first }.filter { it.isNotBlank() }

                // 检查重复
                val duplicateIds = loginIds.groupingBy { it }
                    .eachCount()
                    .filter { it.value > 1 }
                    .keys

                if (duplicateIds.isNotEmpty()) {
                    return@withContext MembersValidationResult.DuplicatesFound(
                        duplicates = duplicateIds.toList()
                    )
                }

                // 批量验证
                val validationResults = validateMultipleMembers(loginIds)

                // 检查无效的ID
                val invalidResults = validationResults.filter { it.value is ValidationResult.Error }

                if (invalidResults.isNotEmpty()) {
                    val invalidIds = invalidResults.keys.toList()
                    val errorMessages = invalidResults.values
                        .filterIsInstance<ValidationResult.Error>()
                        .map { it.message }
                        .joinToString(", ")

                    return@withContext MembersValidationResult.InvalidIds(
                        invalidIds = invalidIds,
                        errorMessage = errorMessages
                    )
                }

                // 获取有效的用户信息
                val validMembers = members.filter { it.first.isNotBlank() }.map { member ->
                    val result = validationResults[member.first]
                    if (result is ValidationResult.Success) {
                        // 使用Firestore中的用户名（如果提供了成员姓名，可以进行比较验证）
                        Pair(member.first, result.user.username)
                    } else {
                        Pair(member.first, member.second)
                    }
                }

                return@withContext MembersValidationResult.Success(validMembers)
            } catch (e: Exception) {
                return@withContext MembersValidationResult.Error(
                    errorMessage = "Validation failed: ${e.message}"
                )
            }
        }
    }
}

sealed class ValidationResult {
    data class Success(val user: Users) : ValidationResult()
    data class Error(val message: String, val loginId: String) : ValidationResult()
}

sealed class MembersValidationResult {
    data class Success(val members: List<Pair<String, String>>) : MembersValidationResult()
    data class DuplicatesFound(val duplicates: List<String>) : MembersValidationResult()
    data class InvalidIds(val invalidIds: List<String>, val errorMessage: String) : MembersValidationResult()
    data class Error(val errorMessage: String) : MembersValidationResult()
}