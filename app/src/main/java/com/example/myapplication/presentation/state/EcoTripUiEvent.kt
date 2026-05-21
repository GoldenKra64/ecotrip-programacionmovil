package com.example.myapplication.presentation.state

import com.example.myapplication.data.model.TransporteEcologico

/**
 * Eventos ascendentes (UDF): la UI notifica intenciones, el ViewModel decide.
 */
sealed interface EcoTripUiEvent {
    data class NombreViajeroChanged(val value: String) : EcoTripUiEvent
    data class CiudadOrigenChanged(val value: String) : EcoTripUiEvent
    data class CiudadDestinoChanged(val value: String) : EcoTripUiEvent
    data class CantidadPasajerosChanged(val value: String) : EcoTripUiEvent
    data class PresupuestoEstimadoChanged(val value: String) : EcoTripUiEvent
    data class DiasDuracionChanged(val value: String) : EcoTripUiEvent
    data class TransporteChanged(val value: TransporteEcologico) : EcoTripUiEvent
    data class BajaHuellaCarbonoChanged(val value: Boolean) : EcoTripUiEvent
    data class EsViajeGrupalChanged(val value: Boolean) : EcoTripUiEvent
    data object GuardarPreferencias : EcoTripUiEvent
    data object LimpiarMensajeError : EcoTripUiEvent
    data object ResetearEstadoNavegacion : EcoTripUiEvent
}
