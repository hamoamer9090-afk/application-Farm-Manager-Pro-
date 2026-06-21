package com.example.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    val currentUserId: String?
        get() {
            val user = auth.currentUser
            PermissionManager.setAuthenticatedEmail(user?.email)
            return user?.uid
        }

    val currentUserEmail: String?
        get() {
            val email = auth.currentUser?.email
            PermissionManager.setAuthenticatedEmail(email)
            return email
        }

    /**
     * Continuously observes the specific user profile.
     * Updates the PermissionManager cache automatically.
     */
    fun observeUserProfile(userId: String): Flow<UserRole?> = callbackFlow {
        val ref = database.reference.child("users").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userRole = snapshot.getValue(UserRole::class.java)
                PermissionManager.setUserRole(userRole)
                trySend(userRole)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "User Profile Observation Cancelled", error.toException())
                trySend(null)
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Fetches the user profile once and initializes PermissionManager.
     */
    suspend fun fetchUserProfile(userId: String): Result<UserRole> {
        return try {
            val snapshot = database.reference.child("users").child(userId).get().await()
            val userRole = snapshot.getValue(UserRole::class.java)
            if (userRole != null) {
                PermissionManager.setUserRole(userRole)
                Result.success(userRole)
            } else {
                Result.failure(Exception("User profile not found in Realtime Database"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching user role", e)
            Result.failure(e)
        }
    }

    /**
     * Updates user profile (Used by Admins to modify roles/permissions).
     * Enforces Root Owner locked state.
     */
    suspend fun updateUserProfile(targetUserId: String, updatedRole: UserRole): Result<Unit> {
        return try {
            val targetRef = database.reference.child("users").child(targetUserId)
            val currentSnapshot = targetRef.get().await()
            val currentUserData = currentSnapshot.getValue(UserRole::class.java)
            
            // Hardcoded Check: Prevent ANY modification of the Root Owner profile unless performed by them.
            if (currentUserData?.email.equals(PermissionManager.ROOT_OWNER_EMAIL, ignoreCase = true) && 
                !currentUserEmail.equals(PermissionManager.ROOT_OWNER_EMAIL, ignoreCase = true)) {
                return Result.failure(Exception("SECURITY EXCEPTION: Cannot modify, downgrade, or overwrite the Root Owner profile."))
            }

            targetRef.setValue(updatedRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating user profile", e)
            Result.failure(e)
        }
    }

    fun observeAllUsers(): Flow<Map<String, UserRole>> = callbackFlow {
        val ref = database.reference.child("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, UserRole>()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val userRole = child.getValue(UserRole::class.java)
                    if (userRole != null) {
                        map[uid] = userRole
                    }
                }
                trySend(map)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "All Users Observation Cancelled", error.toException())
                trySend(emptyMap())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // --- FARMS LAYER --- //

    fun observeFarms(): Flow<List<FarmRecord>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val isGlobalAdmin = PermissionManager.isGlobalAdmin()
        
        // If not a global admin, use a query to fetch only farms owned by the user.
        // This matches the new Firebase rule: (query.orderByChild == 'owner_uid' && query.equalTo == auth.uid)
        val query = if (isGlobalAdmin) {
            database.reference.child("farms")
        } else {
            database.reference.child("farms").orderByChild("owner_uid").equalTo(uid)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<FarmRecord>()
                for (child in snapshot.children) {
                    val farm = child.getValue(FarmRecord::class.java)
                    if (farm != null) {
                        // Even with query, we keep defensive local filtering for admins/other-access
                        if (isGlobalAdmin || farm.owner_uid == uid || farm.admins[uid] == true) {
                            list.add(farm)
                        }
                    }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Farm Observation Cancelled: ${error.message}")
                trySend(emptyList())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun createFarm(farm: FarmRecord): Result<Unit> {
        return try {
            val email = currentUserEmail ?: return Result.failure(Exception("Not Authenticated"))
            
            val farmRef = database.reference.child("farms").push()
            val key = farmRef.key ?: return Result.failure(Exception("Failed to generate database key"))
            
            val farmToSave = farm.copy(
                id = key,
                owner_uid = currentUserId ?: "",
                owner_email = email,
                createdAt = if (farm.createdAt == 0L) System.currentTimeMillis() else farm.createdAt
            )
            
            farmRef.setValue(farmToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error creating farm", e)
            Result.failure(e)
        }
    }

    suspend fun updateFarm(farmId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            database.reference.child("farms").child(farmId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating farm", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFarm(farmId: String): Result<Unit> {
        return try {
            database.reference.child("farms").child(farmId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting farm", e)
            Result.failure(e)
        }
    }

    suspend fun addAdminToFarm(farmId: String, emailOrUid: String): Result<Unit> {
        return try {
            var targetUid = emailOrUid
            if (emailOrUid.contains("@")) {
                // Find user by email
                val usersSnapshot = database.reference.child("users").orderByChild("email").equalTo(emailOrUid).get().await()
                if (usersSnapshot.exists()) {
                    targetUid = usersSnapshot.children.first().key ?: emailOrUid
                } else {
                    return Result.failure(Exception("User not found with this email."))
                }
            }
            
            val updates = mapOf<String, Any>(
                "admins/$targetUid" to true
            )
            database.reference.child("farms").child(farmId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding admin to farm", e)
            Result.failure(e)
        }
    }
}
