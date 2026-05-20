package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PreferenciaViaje(
    val nombreViajero: String,
    val transporte: TransporteEcologico,
    val bajaHuellaCarbono: Boolean
)