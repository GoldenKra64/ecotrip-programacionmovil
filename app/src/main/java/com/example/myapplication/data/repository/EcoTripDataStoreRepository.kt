package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.model.PreferenciaViaje
import com.example.myapplication.data.model.TransporteEcologico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.ecoTripDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecotrip_preferencias"
)

/**
 * Persistencia en disco (Nivel 3): preferencias globales del viajero.
 * Según el examen: nombre, huella de carbono y transporte sobreviven al cierre de la app.
 */
class EcoTripDataStoreRepository(
    private val context: Context
) {
    private val dataStore = context.ecoTripDataStore

    val preferenciasFlow: Flow<PreferenciaViaje> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences.toPreferenciaViaje() }

    suspend fun guardarPreferencias(preferencia: PreferenciaViaje) {
        dataStore.edit { preferences ->
            preferences[Keys.NOMBRE_VIAJERO] = preferencia.nombreViajero.trim()
            preferences[Keys.BAJA_HUELLA_CARBONO] = preferencia.bajaHuellaCarbono
            preferences[Keys.TRANSPORTE] = preferencia.transporte.name
            preferences[Keys.CIUDAD_ORIGEN] = preferencia.ciudadOrigen.trim()
            preferences[Keys.CIUDAD_DESTINO] = preferencia.ciudadDestino.trim()
            preferences[Keys.CANTIDAD_PASAJEROS] = preferencia.cantidadPasajeros.toString()
            preferences[Keys.PRESUPUESTO_ESTIMADO] = preferencia.presupuestoEstimado.toString()
            preferences[Keys.DIAS_DURACION] = preferencia.diasDuracion.toString()
            preferences[Keys.ES_VIAJE_GRUPAL] = preferencia.esViajeGrupal
        }
    }

    suspend fun limpiarPreferencias() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toPreferenciaViaje(): PreferenciaViaje {
        val transporteRaw = this[Keys.TRANSPORTE]
        val transporte = transporteRaw
            ?.let { runCatching { TransporteEcologico.valueOf(it) }.getOrNull() }
            ?: TransporteEcologico.TREN

        return PreferenciaViaje(
            nombreViajero = this[Keys.NOMBRE_VIAJERO].orEmpty(),
            ciudadOrigen = this[Keys.CIUDAD_ORIGEN].orEmpty(),
            ciudadDestino = this[Keys.CIUDAD_DESTINO].orEmpty(),
            cantidadPasajeros = this[Keys.CANTIDAD_PASAJEROS]
                ?.toIntOrNull()
                ?.coerceAtLeast(PreferenciaViaje.PASAJEROS_MINIMO)
                ?: PreferenciaViaje.PASAJEROS_MINIMO,
            presupuestoEstimado = this[Keys.PRESUPUESTO_ESTIMADO]
                ?.toDoubleOrNull()
                ?.coerceAtLeast(PreferenciaViaje.PRESUPUESTO_MINIMO)
                ?: PreferenciaViaje.PRESUPUESTO_MINIMO,
            diasDuracion = this[Keys.DIAS_DURACION]
                ?.toIntOrNull()
                ?.coerceAtLeast(PreferenciaViaje.DURACION_MINIMA)
                ?: PreferenciaViaje.DURACION_MINIMA,
            transporte = transporte,
            bajaHuellaCarbono = this[Keys.BAJA_HUELLA_CARBONO] ?: false,
            esViajeGrupal = this[Keys.ES_VIAJE_GRUPAL] ?: false
        )
    }

    private object Keys {
        val NOMBRE_VIAJERO = stringPreferencesKey("nombre_viajero")
        val CIUDAD_ORIGEN = stringPreferencesKey("ciudad_origen")
        val CIUDAD_DESTINO = stringPreferencesKey("ciudad_destino")
        val CANTIDAD_PASAJEROS = stringPreferencesKey("cantidad_pasajeros")
        val PRESUPUESTO_ESTIMADO = stringPreferencesKey("presupuesto_estimado")
        val DIAS_DURACION = stringPreferencesKey("dias_duracion")
        val TRANSPORTE = stringPreferencesKey("transporte")
        val BAJA_HUELLA_CARBONO = booleanPreferencesKey("baja_huella_carbono")
        val ES_VIAJE_GRUPAL = booleanPreferencesKey("es_viaje_grupal")
    }
}
