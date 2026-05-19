package com.example.myapplication.navigation

import com.example.myapplication.data.model.TransporteEcologico
import kotlinx.serialization.Serializable

@Serializable
data object FormularioRoute

@Serializable
data class ResumenRoute(
    val nombreViajero: String,
    val destino: String,
    val diasDuracion: Int,
    val transporte: TransporteEcologico,
    val bajaHuellaCarbono: Boolean,
    val esViajeGrupal: Boolean
)