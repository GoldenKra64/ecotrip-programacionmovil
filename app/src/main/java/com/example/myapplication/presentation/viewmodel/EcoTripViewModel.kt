package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.PreferenciaViaje
import com.example.myapplication.data.model.TransporteEcologico
import com.example.myapplication.data.repository.EcoTripDataStoreRepository
import com.example.myapplication.presentation.state.EcoTripUiEvent
import com.example.myapplication.presentation.state.EcoTripUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Fuente única de verdad con resiliencia multinivel:
 * - Nivel 1 (Memoria): [uiState] StateFlow inmutable para la UI y rotación.
 * - Nivel 2 (Proceso): [SavedStateHandle] para destino, duración y campos del formulario.
 * - Nivel 3 (Disco): [EcoTripDataStoreRepository] para preferencias persistentes.
 */
class EcoTripViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataStoreRepository: EcoTripDataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(restaurarEstadoInicial())
    val uiState: StateFlow<EcoTripUiState> = _uiState.asStateFlow()

    init {
        observarPersistenciaEnDisco()
    }

    fun onEvent(event: EcoTripUiEvent) {
        when (event) {
            is EcoTripUiEvent.NombreViajeroChanged ->
                actualizarEstado { it.copy(nombreViajero = event.value, mensajeError = null) }

            is EcoTripUiEvent.CiudadOrigenChanged ->
                actualizarEstado { it.copy(ciudadOrigen = event.value, mensajeError = null) }

            is EcoTripUiEvent.CiudadDestinoChanged ->
                actualizarEstado { it.copy(ciudadDestino = event.value, mensajeError = null) }

            is EcoTripUiEvent.CantidadPasajerosChanged ->
                actualizarEstado {
                    it.copy(
                        cantidadPasajerosInput = desinfectarEntero(event.value),
                        mensajeError = null
                    )
                }

            is EcoTripUiEvent.PresupuestoEstimadoChanged ->
                actualizarEstado {
                    it.copy(
                        presupuestoEstimadoInput = desinfectarDecimal(event.value),
                        mensajeError = null
                    )
                }

            is EcoTripUiEvent.DiasDuracionChanged ->
                actualizarEstado {
                    it.copy(
                        diasDuracionInput = desinfectarEntero(event.value),
                        mensajeError = null
                    )
                }

            is EcoTripUiEvent.TransporteChanged ->
                actualizarEstado { it.copy(transporte = event.value, mensajeError = null) }

            is EcoTripUiEvent.BajaHuellaCarbonoChanged ->
                actualizarEstado { it.copy(bajaHuellaCarbono = event.value, mensajeError = null) }

            is EcoTripUiEvent.EsViajeGrupalChanged ->
                actualizarEstado { it.copy(esViajeGrupal = event.value, mensajeError = null) }

            EcoTripUiEvent.GuardarPreferencias -> guardarPreferencias()

            EcoTripUiEvent.LimpiarMensajeError ->
                actualizarEstado { it.copy(mensajeError = null) }
        }
    }

    private fun observarPersistenciaEnDisco() {
        viewModelScope.launch {
            val preferenciaDisco = dataStoreRepository.preferenciasFlow.first()
            _uiState.update { actual -> fusionarPreferenciaDisco(actual, preferenciaDisco) }
        }
    }

    private fun fusionarPreferenciaDisco(
        actual: EcoTripUiState,
        preferenciaDisco: PreferenciaViaje
    ): EcoTripUiState = actual.copy(
        nombreViajero = preferenciaDisco.nombreViajero.ifBlank { actual.nombreViajero },
        bajaHuellaCarbono = preferenciaDisco.bajaHuellaCarbono,
        transporte = if (actual.isLoading) preferenciaDisco.transporte else actual.transporte,
        ciudadOrigen = actual.ciudadOrigen.ifBlank { preferenciaDisco.ciudadOrigen },
        ciudadDestino = actual.ciudadDestino.ifBlank { preferenciaDisco.ciudadDestino },
        cantidadPasajerosInput = if (actual.isLoading) {
            preferenciaDisco.cantidadPasajeros.toString()
        } else {
            actual.cantidadPasajerosInput
        },
        presupuestoEstimadoInput = if (actual.isLoading) {
            preferenciaDisco.presupuestoEstimado.toString()
        } else {
            actual.presupuestoEstimadoInput
        },
        diasDuracionInput = if (actual.isLoading) {
            preferenciaDisco.diasDuracion.toString()
        } else {
            actual.diasDuracionInput
        },
        esViajeGrupal = if (actual.isLoading) preferenciaDisco.esViajeGrupal else actual.esViajeGrupal,
        isLoading = false
    )

    private fun guardarPreferencias() {
        val estadoActual = _uiState.value
        val errorValidacion = validarFormulario(estadoActual)

        if (errorValidacion != null) {
            actualizarEstado { it.copy(mensajeError = errorValidacion, guardadoExitoso = false) }
            return
        }

        viewModelScope.launch {
            actualizarEstado { it.copy(isSaving = true, mensajeError = null, guardadoExitoso = false) }
            val preferencia = estadoActual.toPreferenciaViaje()
            runCatching {
                dataStoreRepository.guardarPreferencias(preferencia)
                preferencia
            }.onSuccess { preferenciaGuardada ->
                actualizarEstado {
                    fusionarPreferenciaDisco(
                        it.copy(
                            isSaving = false,
                            guardadoExitoso = true,
                            mensajeError = null
                        ),
                        preferenciaGuardada
                    )
                }
            }.onFailure { error ->
                actualizarEstado {
                    it.copy(
                        isSaving = false,
                        guardadoExitoso = false,
                        mensajeError = error.message ?: "No se pudieron guardar las preferencias"
                    )
                }
            }
        }
    }

    private fun validarFormulario(estado: EcoTripUiState): String? = when {
        estado.nombreViajero.isBlank() ->
            "Ingrese el nombre del viajero"

        estado.ciudadOrigen.isBlank() ->
            "Ingrese la ciudad de origen"

        estado.ciudadDestino.isBlank() ->
            "Ingrese el destino del viaje"

        estado.diasDuracion == null || estado.diasDuracion < PreferenciaViaje.DURACION_MINIMA ->
            "La duración debe ser un número entero mayor o igual a ${PreferenciaViaje.DURACION_MINIMA}"

        estado.cantidadPasajeros == null || estado.cantidadPasajeros < PreferenciaViaje.PASAJEROS_MINIMO ->
            "La cantidad de pasajeros debe ser un entero mayor o igual a ${PreferenciaViaje.PASAJEROS_MINIMO}"

        estado.presupuestoEstimado == null || estado.presupuestoEstimado < PreferenciaViaje.PRESUPUESTO_MINIMO ->
            "El presupuesto debe ser un valor numérico válido"

        else -> null
    }

    private fun restaurarEstadoInicial(): EcoTripUiState {
        val transporteRestaurado = savedStateHandle.get<String>(Keys.TRANSPORTE)
            ?.let { runCatching { TransporteEcologico.valueOf(it) }.getOrNull() }
            ?: TransporteEcologico.TREN

        return EcoTripUiState(
            nombreViajero = "",
            ciudadOrigen = savedStateHandle.get(Keys.CIUDAD_ORIGEN).orEmpty(),
            ciudadDestino = savedStateHandle.get(Keys.CIUDAD_DESTINO).orEmpty(),
            cantidadPasajerosInput = savedStateHandle.get(Keys.CANTIDAD_PASAJEROS)
                ?: PreferenciaViaje.PASAJEROS_MINIMO.toString(),
            presupuestoEstimadoInput = savedStateHandle.get(Keys.PRESUPUESTO_ESTIMADO)
                ?: PreferenciaViaje.PRESUPUESTO_MINIMO.toString(),
            diasDuracionInput = savedStateHandle.get(Keys.DIAS_DURACION)
                ?: PreferenciaViaje.DURACION_MINIMA.toString(),
            transporte = transporteRestaurado,
            bajaHuellaCarbono = false,
            esViajeGrupal = savedStateHandle.get(Keys.ES_VIAJE_GRUPAL) ?: false,
            isLoading = true
        )
    }

    private fun actualizarEstado(reducer: (EcoTripUiState) -> EcoTripUiState) {
        _uiState.update { actual ->
            val nuevo = reducer(actual)
            persistirEnSavedState(nuevo)
            nuevo
        }
    }

    private fun persistirEnSavedState(estado: EcoTripUiState) {
        savedStateHandle[Keys.CIUDAD_ORIGEN] = estado.ciudadOrigen
        savedStateHandle[Keys.CIUDAD_DESTINO] = estado.ciudadDestino
        savedStateHandle[Keys.DIAS_DURACION] = estado.diasDuracionInput
        savedStateHandle[Keys.CANTIDAD_PASAJEROS] = estado.cantidadPasajerosInput
        savedStateHandle[Keys.PRESUPUESTO_ESTIMADO] = estado.presupuestoEstimadoInput
        savedStateHandle[Keys.TRANSPORTE] = estado.transporte.name
        savedStateHandle[Keys.ES_VIAJE_GRUPAL] = estado.esViajeGrupal
    }

    private fun desinfectarEntero(valor: String): String =
        valor.filter { it.isDigit() }.ifEmpty { "" }

    private fun desinfectarDecimal(valor: String): String {
        val limpio = valor.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
        val partes = limpio.split('.')
        return when {
            limpio.isEmpty() -> ""
            partes.size <= 1 -> limpio
            else -> partes.first() + "." + partes.drop(1).joinToString("")
        }
    }

    private object Keys {
        const val CIUDAD_ORIGEN = "ciudad_origen"
        const val CIUDAD_DESTINO = "destino"
        const val DIAS_DURACION = "dias_duracion"
        const val CANTIDAD_PASAJEROS = "cantidad_pasajeros"
        const val PRESUPUESTO_ESTIMADO = "presupuesto_estimado"
        const val TRANSPORTE = "transporte"
        const val ES_VIAJE_GRUPAL = "es_viaje_grupal"
    }
}
