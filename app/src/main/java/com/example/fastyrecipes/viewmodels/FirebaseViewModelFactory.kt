package com.example.fastyrecipes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fastyrecipes.controller.FirebaseAuthController
import com.example.fastyrecipes.controller.FirebaseController

class FirebaseViewModelFactory(
    private val firebaseController: FirebaseController,
    private val firebaseAuthController: FirebaseAuthController
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecetasViewModel::class.java)) {
            return RecetasViewModel(firebaseController, firebaseAuthController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}