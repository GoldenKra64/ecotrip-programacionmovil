package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo inmutable y serializable para navegación tipada y persistencia.
 * Valores por defecto robustos para evitar estados inválidos al restaurar.
 */
@Serializable
data class PreferenciaViaje(
    val nombreViajero: String = "",
    val ciudadOrigen: String = "",
    val ciudadDestino: String = "",
    val cantidadPasajeros: Int = PASAJEROS_MINIMO,
    val presupuestoEstimado: Double = PRESUPUESTO_MINIMO,
    val diasDuracion: Int = DURACION_MINIMA,
    val transporte: TransporteEcologico = TransporteEcologico.TREN,
    val bajaHuellaCarbono: Boolean = false,
    val esViajeGrupal: Boolean = false
) {
    val esViajeGrupalDerivado: Boolean
        get() = esViajeGrupal || cantidadPasajeros > 1

    fun esFormularioCompleto(): Boolean =
        nombreViajero.isNotBlank() &&
            ciudadOrigen.isNotBlank() &&
            ciudadDestino.isNotBlank() &&
            diasDuracion >= DURACION_MINIMA &&
            cantidadPasajeros >= PASAJEROS_MINIMO &&
            presupuestoEstimado >= PRESUPUESTO_MINIMO

    companion object {
        const val PASAJEROS_MINIMO = 1
        const val DURACION_MINIMA = 1
        const val PRESUPUESTO_MINIMO = 0.0

        val Default = PreferenciaViaje()
    }
}
