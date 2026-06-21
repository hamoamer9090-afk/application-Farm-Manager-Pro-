package com.example.firebase

data class UserRole(
    var email: String = "",
    var role: String = "USER",
    var permissions: Map<String, Boolean> = emptyMap()
)

data class FarmRecord(
    var id: String = "",
    var owner_uid: String = "",
    var owner_email: String = "",
    var name: String = "",
    var location: String = "",
    var password: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var admins: Map<String, Boolean> = emptyMap()
)
