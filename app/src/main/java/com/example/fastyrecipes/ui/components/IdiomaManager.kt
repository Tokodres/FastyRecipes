package com.example.fastyrecipes.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object IdiomaManager {

    fun cambiarIdioma(context: Context, codigoIdioma: String): Context {
        val locale = when (codigoIdioma) {
            "en" -> Locale.ENGLISH
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "pt" -> Locale("pt")
            else -> Locale("es")
        }

        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        return context
    }

    fun obtenerNombreIdioma(codigo: String): String {
        return when (codigo) {
            "en" -> "English"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "pt" -> "Português"
            else -> "Español"
        }
    }

    fun obtenerCodigoIdioma(nombre: String): String {
        return when (nombre) {
            "English" -> "en"
            "Français" -> "fr"
            "Deutsch" -> "de"
            "Italiano" -> "it"
            "Português" -> "pt"
            else -> "es"
        }
    }

    fun obtenerIdiomaActual(context: Context): String {
        return Locale.getDefault().language
    }
}