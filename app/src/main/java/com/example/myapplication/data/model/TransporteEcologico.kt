package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransporteEcologico(
    val etiqueta: String
) {
    TREN("Tren"),
    BICICLETA("Bicicleta"),
    VEHICULO_ELECTRICO("Vehículo eléctrico")
}