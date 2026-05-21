package com.example.myapplication.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.TransporteEcologico
import com.example.myapplication.navigation.ResumenRoute
import com.example.myapplication.presentation.state.EcoTripUiEvent
import com.example.myapplication.presentation.viewmodel.EcoTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: EcoTripViewModel,
    onNavigateToResumen: (ResumenRoute) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            val preferencia = uiState.toPreferenciaViaje()
            onNavigateToResumen(
                ResumenRoute(
                    nombreViajero = preferencia.nombreViajero,
                    destino = preferencia.ciudadDestino,
                    diasDuracion = preferencia.diasDuracion,
                    transporte = preferencia.transporte,
                    bajaHuellaCarbono = preferencia.bajaHuellaCarbono,
                    esViajeGrupal = preferencia.esViajeGrupal
                )
            )
            viewModel.onEvent(EcoTripUiEvent.ResetearEstadoNavegacion)
        }
    }

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EcoTripUiEvent.LimpiarMensajeError)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("EcoTrip") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(EcoTripUiEvent.GuardarPreferencias) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar y Continuar")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Información del Viajero",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.nombreViajero,
                    onValueChange = { viewModel.onEvent(EcoTripUiEvent.NombreViajeroChanged(it)) },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Text(
                    "Ruta",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.ciudadOrigen,
                    onValueChange = { viewModel.onEvent(EcoTripUiEvent.CiudadOrigenChanged(it)) },
                    label = { Text("Origen") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = uiState.ciudadDestino,
                    onValueChange = { viewModel.onEvent(EcoTripUiEvent.CiudadDestinoChanged(it)) },
                    label = { Text("Destino") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium
                )

                Text(
                    "Detalles",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.cantidadPasajerosInput,
                        onValueChange = { viewModel.onEvent(EcoTripUiEvent.CantidadPasajerosChanged(it)) },
                        label = { Text("Pasajeros") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = uiState.diasDuracionInput,
                        onValueChange = { viewModel.onEvent(EcoTripUiEvent.DiasDuracionChanged(it)) },
                        label = { Text("Días") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = uiState.presupuestoEstimadoInput,
                    onValueChange = { viewModel.onEvent(EcoTripUiEvent.PresupuestoEstimadoChanged(it)) },
                    label = { Text("Presupuesto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium
                )

                Text(
                    "Transporte Ecológico",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                Column(Modifier.selectableGroup()) {
                    TransporteEcologico.entries.forEach { transporte ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (uiState.transporte == transporte),
                                    onClick = { viewModel.onEvent(EcoTripUiEvent.TransporteChanged(transporte)) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (uiState.transporte == transporte),
                                onClick = null
                            )
                            Text(
                                text = transporte.etiqueta,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Text(
                    "Opciones de Sostenibilidad",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Baja Huella de Carbono", style = MaterialTheme.typography.bodyLarge)
                                Text("Priorizar rutas ecológicas", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = uiState.bajaHuellaCarbono,
                                onCheckedChange = { viewModel.onEvent(EcoTripUiEvent.BajaHuellaCarbonoChanged(it)) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Viaje Grupal", style = MaterialTheme.typography.bodyLarge)
                                Text("Activar coordinación de grupo", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = uiState.esViajeGrupal,
                                onCheckedChange = { viewModel.onEvent(EcoTripUiEvent.EsViajeGrupalChanged(it)) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
