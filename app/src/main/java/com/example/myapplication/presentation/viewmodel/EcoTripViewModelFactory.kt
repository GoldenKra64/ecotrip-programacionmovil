package com.example.myapplication.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.myapplication.data.repository.EcoTripDataStoreRepository

/**
 * Factory para inyectar [SavedStateHandle] y [EcoTripDataStoreRepository] sin acoplar la UI a Hilt.
 */
class EcoTripViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(EcoTripViewModel::class.java)) {
            val savedStateHandle: SavedStateHandle = extras.createSavedStateHandle()
            val repository = EcoTripDataStoreRepository(context.applicationContext)
            return EcoTripViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
