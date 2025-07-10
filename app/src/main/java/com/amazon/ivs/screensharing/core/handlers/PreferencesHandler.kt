package com.amazon.ivs.screensharing.core.handlers

import android.content.Context
import androidx.core.content.edit
import com.amazon.ivs.screensharing.appContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val PREFERENCES_NAME = "StagesRTPreferences"

object PreferencesHandler {
    private val sharedPreferences by lazy { appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE) }

    var token by stringPreference()

    private fun stringPreference() = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) =
            sharedPreferences.getString(property.name, null)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            sharedPreferences.edit { putString(property.name, value) }
        }
    }
}
