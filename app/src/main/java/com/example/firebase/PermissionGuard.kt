package com.example.firebase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics

/**
 * A reusable Jetpack Compose wrapper that enforces Firebase Role-Based Access Control.
 * It queries the PermissionManager singleton to determine if the specified feature is allowed.
 *
 * @param permissionKey The specific permission string from the Realtime Database (e.g. "view_farm")
 * @param hideIfDenied If true, the content will be completely invisible and removed from composition.
 *                     If false, the content will be rendered but greyed-out and disabled.
 * @param disabledModifier The modifier applied when the UI is present but disabled.
 * @param content The composable content to render. Receives `hasPermission` state explicitly,
 *                so the wrapped component can optionally disable its internal ClickListeners.
 */
@Composable
fun PermissionGuard(
    permissionKey: String,
    hideIfDenied: Boolean = false,
    disabledModifier: Modifier = Modifier.alpha(0.5f),
    content: @Composable (hasPermission: Boolean) -> Unit
) {
    val currentUserRole by PermissionManager.currentUserRole.collectAsState()
    
    // Evaluate permissions
    val hasPermission = if (currentUserRole?.email.equals(PermissionManager.ROOT_OWNER_EMAIL, ignoreCase = true)) {
        true // Root Owner absolute bypass
    } else {
        currentUserRole?.permissions?.get(permissionKey) == true
    }

    if (hasPermission) {
        content(true)
    } else {
        if (!hideIfDenied) {
            Box(modifier = disabledModifier.semantics { disabled() }) {
                // By passing 'false', the child component can choose to disable its inner buttons.
                content(false)
            }
        }
    }
}
