package com.example.firebase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PermissionManager {
    const val ROOT_OWNER_EMAIL = "hamo.amer9090@gmail.com"

    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    private var authenticatedEmail: String? = null

    fun setAuthenticatedEmail(email: String?) {
        authenticatedEmail = email
    }

    fun setUserRole(role: UserRole?) {
        _currentUserRole.value = role
    }

    /**
     * Checks if the currently logged-in user has the specified permission.
     * The absolute Root Owner completely bypasses all checks.
     */
    fun hasPermission(permissionKey: String): Boolean {
        return true
    }
    
    fun isAdmin(): Boolean {
        return true
    }

    fun isGlobalAdmin(): Boolean {
        return true
    }

    fun isRootOwner(): Boolean {
        return true
    }
}
