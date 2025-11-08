package com.example.fastyrecipes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fastyrecipes.controller.FastyController

class RecetasViewModelFactory(
    private val controller: FastyController
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecetasViewModel::class.java)) {
            return RecetasViewModel(controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}