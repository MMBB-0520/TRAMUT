package com.example.tramut.rooms.repo

import android.util.Log
import com.example.tramut.rooms.dao.UsersDAO
import com.example.tramut.rooms.entity.Member
import com.example.tramut.rooms.entity.Users
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.filter
import kotlin.collections.map

class UsersRepo(
    private val usersDao: UsersDAO
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllUsers(): Flow<List<Users>> = usersDao.getAllUsers()

    suspend fun getUserByLoginIdAndRole(loginId: String, role: String) =
        usersDao.getUserByLoginIdAndRole(loginId, role)

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
                auth.confirmPasswordReset(oobCode, newPassword).await()
                true
            } catch (e: Exception) {
                Log.e("UsersRepo", "Reset failed", e)
                false
            }
        }

    suspend fun resendPassword(email: String): Boolean =
        withContext(Dispatchers.IO) {

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.example.tramut",
                    true,
                    "1"
                )
                .build()

            auth.sendPasswordResetEmail(email, actionCodeSettings).await()
            true
        }

    suspend fun checkStudentExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Student") != null

    suspend fun checkStaffExists(loginId: String) =
        usersDao.getUserByLoginIdAndRole(loginId, "Staff") != null


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

    suspend fun validateMembersWithDuplicates(members: List<Member>): MembersValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                // 使用 it.id 替代 it.first
                val loginIds = members.map { it.id }.filter { it.isNotBlank() }

                // 检查重复
                val duplicateIds = loginIds.groupingBy { it }
                    .eachCount()
                    .filter { it.value > 1 }
                    .keys

                if (duplicateIds.isNotEmpty()) {
                    return@withContext MembersValidationResult.DuplicatesFound(duplicateIds.toList())
                }

                val validationResults = validateMultipleMembers(loginIds)
                val invalidResults = validationResults.filter { it.value is ValidationResult.Error }

                if (invalidResults.isNotEmpty()) {
                    return@withContext MembersValidationResult.InvalidIds(
                        invalidIds = invalidResults.keys.toList(),
                        errorMessage = "Invalid IDs found"
                    )
                }

                // 返回新的 Member 对象列表
                val validMembers = members.filter { it.id.isNotBlank() }.map { m ->
                    val result = validationResults[m.id]
                    if (result is ValidationResult.Success) {
                        Member(id = m.id, name = result.user.username)
                    } else m
                }

                return@withContext MembersValidationResult.Success(validMembers)
            } catch (e: Exception) {
                return@withContext MembersValidationResult.Error("Validation failed: ${e.message}")
            }
        }
    }
}

sealed class ValidationResult {
    data class Success(val user: Users) : ValidationResult()
    data class Error(val message: String, val loginId: String) : ValidationResult()
}

sealed class MembersValidationResult {
    data class Success(val members: List<Member>) : MembersValidationResult()
    data class DuplicatesFound(val duplicates: List<String>) : MembersValidationResult()
    data class InvalidIds(val invalidIds: List<String>, val errorMessage: String) : MembersValidationResult()
    data class Error(val errorMessage: String) : MembersValidationResult()

}