package com.example.myapplication.presentation.state

import com.example.myapplication.data.model.PreferenciaViaje
import com.example.myapplication.data.model.TransporteEcologico

/**
 * Estado de presentación (UDF): la UI solo lee y emite eventos.
 * Los campos numéricos se mantienen como [String] para edición segura con [toIntOrNull].
 */
data class EcoTripUiState(
    val nombreViajero: String = "",
    val ciudadOrigen: String = "",
    val ciudadDestino: String = "",
    val cantidadPasajerosInput: String = PreferenciaViaje.PASAJEROS_MINIMO.toString(),
    val presupuestoEstimadoInput: String = PreferenciaViaje.PRESUPUESTO_MINIMO.toString(),
    val diasDuracionInput: String = PreferenciaViaje.DURACION_MINIMA.toString(),
    val transporte: TransporteEcologico = TransporteEcologico.TREN,
    val bajaHuellaCarbono: Boolean = false,
    val esViajeGrupal: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null
) {
    val cantidadPasajeros: Int?
        get() = cantidadPasajerosInput.toIntOrNull()

    val diasDuracion: Int?
        get() = diasDuracionInput.toIntOrNull()

    val presupuestoEstimado: Double?
        get() = presupuestoEstimadoInput.toDoubleOrNull()

    fun toPreferenciaViaje(): PreferenciaViaje = PreferenciaViaje(
        nombreViajero = nombreViajero.trim(),
        ciudadOrigen = ciudadOrigen.trim(),
        ciudadDestino = ciudadDestino.trim(),
        cantidadPasajeros = cantidadPasajeros
            ?: PreferenciaViaje.PASAJEROS_MINIMO,
        presupuestoEstimado = presupuestoEstimado
            ?: PreferenciaViaje.PRESUPUESTO_MINIMO,
        diasDuracion = diasDuracion
            ?: PreferenciaViaje.DURACION_MINIMA,
        transporte = transporte,
        bajaHuellaCarbono = bajaHuellaCarbono,
        esViajeGrupal = esViajeGrupal
    )

    fun preferenciaValidadaParaResumen(): PreferenciaViaje? {
        val preferencia = toPreferenciaViaje()
        return preferencia.takeIf { preferencia.esFormularioCompleto() && mensajeError == null }
    }
}
