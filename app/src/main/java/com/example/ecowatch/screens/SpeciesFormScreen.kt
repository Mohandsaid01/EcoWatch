package com.example.ecowatch.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowatch.R
import com.example.ecowatch.data.model.Species
import com.example.ecowatch.util.LocationHelper
import com.example.ecowatch.util.SensorReader
import com.example.ecowatch.util.Notifier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesFormScreen(
    onSaved: () -> Unit,
    vm: SpeciesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var population by remember { mutableStateOf("") }

    var minTemp by remember { mutableStateOf("") }
    var maxTemp by remember { mutableStateOf("") }
    var minHum by remember { mutableStateOf("") }
    var maxHum by remember { mutableStateOf("") }

    var address by remember { mutableStateOf("") }
    var latStr by remember { mutableStateOf("") }
    var lngStr by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var loadingLoc by remember { mutableStateOf(false) }

    // CAPTEURS
    val sensorReader = remember { SensorReader(context) }
    DisposableEffect(Unit) {
        sensorReader.start()
        onDispose { sensorReader.stop() }
    }
    val currentHum = sensorReader.humidity
    val currentTemp = sensorReader.temperature

    // GEO
    fun fetchLocation() {
        loadingLoc = true
        scope.launch {
            try {
                val loc = LocationHelper.getBestLocation(context)
                loadingLoc = false
                if (loc != null) {
                    latStr = "%.6f".format(loc.latitude)
                    lngStr = "%.6f".format(loc.longitude)
                    val addr = LocationHelper.reverseGeocode(context, loc.latitude, loc.longitude)
                    address = addr ?: "$latStr, $lngStr"
                    error = null
                } else {
                    error = context.getString(R.string.err_location_unavailable)
                }
            } catch (e: Exception) {
                loadingLoc = false
                error = e.message
                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation() else {
            error = context.getString(R.string.err_perm_denied)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickMyPosition() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) fetchLocation()
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // SAVE
    fun save() {
        val minT = minTemp.toDoubleOrNull()
        val maxT = maxTemp.toDoubleOrNull()
        val minH = minHum.toDoubleOrNull()
        val maxH = maxHum.toDoubleOrNull()
        val pop = population.toIntOrNull()
        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()

        when {
            name.isBlank() -> error = context.getString(R.string.err_name_required)
            (minT == null && maxT == null && minH == null && maxH == null) ->
                error = context.getString(R.string.err_need_one_threshold)
            (minT != null && maxT != null && minT > maxT) ->
                error = context.getString(R.string.err_temp_minmax)
            (minH != null && maxH != null && minH > maxH) ->
                error = context.getString(R.string.err_hum_minmax)
            (minH != null && (minH < 0 || minH > 100)) ||
                    (maxH != null && (maxH < 0 || maxH > 100)) ->
                error = context.getString(R.string.err_hum_range)
            else -> {
                error = null
                val species = Species(
                    name = name.trim(),
                    habitat = null,
                    status = status.trim().ifBlank { null },
                    population = pop,
                    minTemp = minT,
                    maxTemp = maxT,
                    minHumidity = minH,
                    maxHumidity = maxH,
                    lat = lat,
                    lng = lng,
                    address = address.ifBlank { null }
                )

                val alerts = vm.evaluateThresholds(
                    species,
                    currentTemp?.toDouble(),
                    currentHum?.toDouble()
                )
                if (alerts.isNotEmpty()) {
                    Notifier.notifyThreshold(
                        context,
                        species,
                        alerts.joinToString(" • ")
                    )
                }

                vm.addOrUpdateSpecies(species)
                onSaved()
            }
        }
    }

    // UI avec bouton fixe en bas
    val scroll = rememberScrollState()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.form_title)) }) },
        bottomBar = {
            Button(
                onClick = { save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = status, onValueChange = { status = it },
                    label = { Text(stringResource(R.string.status_label)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = population, onValueChange = { population = it },
                    label = { Text(stringResource(R.string.population_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text(stringResource(R.string.address_label)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onClickMyPosition() }) {
                    if (loadingLoc) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.my_location))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = latStr, onValueChange = { latStr = it },
                    label = { Text(stringResource(R.string.lat_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = lngStr, onValueChange = { lngStr = it },
                    label = { Text(stringResource(R.string.lng_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = buildString {
                            append(stringResource(R.string.sensor_hum_now))
                            append(" ")
                            append(currentHum?.let { String.format("%.1f %%", it) }
                                ?: stringResource(R.string.sensor_unavailable))
                        }
                    )
                    if (currentTemp != null) {
                        Text("${stringResource(R.string.sensor_temp_now)} ${String.format("%.1f °C", currentTemp)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            currentHum?.let { if (minHum.isBlank()) minHum = "%.1f".format(it) }
                        }) { Text(stringResource(R.string.copy_hum_to_min)) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            currentHum?.let { if (maxHum.isBlank()) maxHum = "%.1f".format(it) }
                        }) { Text(stringResource(R.string.copy_hum_to_max)) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            NumberField(minTemp, { minTemp = it }, stringResource(R.string.temp_min_label))
            Spacer(Modifier.height(8.dp))
            NumberField(maxTemp, { maxTemp = it }, stringResource(R.string.temp_max_label))
            Spacer(Modifier.height(8.dp))
            NumberField(minHum, { minHum = it }, stringResource(R.string.hum_min_label))
            Spacer(Modifier.height(8.dp))
            NumberField(maxHum, { maxHum = it }, stringResource(R.string.hum_max_label))

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(80.dp)) // marge pour ne pas passer sous la bottomBar
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
