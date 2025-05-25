package org.jupnp.example.binarylight.server

import android.content.Context
import androidx.core.content.edit
import org.jupnp.model.types.UDN
import java.util.UUID

private const val PREFS_NAME = "example_jupnp"
private const val KEY_UNIQUE_IDENTIFIER = "jupnp-unique-system-identifier"

fun Context.uniqueSystemIdentifier(): UDN {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val identifier = prefs.getString(KEY_UNIQUE_IDENTIFIER, null)
    if (identifier == null) {
        val newIdentifier = UUID.randomUUID().toString()
        prefs.edit {
            putString(KEY_UNIQUE_IDENTIFIER, newIdentifier)
        }

        return UDN(newIdentifier)
    }

    return UDN(identifier)
}