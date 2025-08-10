package com.example.ecowatch.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecowatch.R
import com.example.ecowatch.data.model.Species
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesListScreen(
    onAddClick: () -> Unit,
    onOpenDetails: (Long) -> Unit = {},
    vm: SpeciesViewModel = viewModel()
) {
    val speciesList by vm.species.collectAsState()

    var query by remember { mutableStateOf("") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_title)) },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            vm.backupAll()
                            snackbarHostState.showSnackbar("Backup OK")
                        }
                    }) { Text("Backup") }

                    TextButton(onClick = {
                        scope.launch {
                            vm.restoreAllReplace()
                            snackbarHostState.showSnackbar("Restore OK")
                        }
                    }) { Text("Restore") }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Text("+") } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Spacer(Modifier.height(12.dp))

            val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
            val hello = if (hour in 5..17) stringResource(R.string.hello_morning)
            else stringResource(R.string.hello_evening)
            Text(
                text = stringResource(R.string.greeting_template, hello),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        vm.onSearchChange(it)
                    },
                    label = { Text(stringResource(R.string.search_hint)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Box {
                    Button(onClick = { sortMenuExpanded = true }) {
                        Text(stringResource(R.string.sort_by))
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_name)) },
                            onClick = { vm.onSortChange(SortBy.NAME); sortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_created)) },
                            onClick = { vm.onSortChange(SortBy.CREATED_AT); sortMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_tempmax)) },
                            onClick = { vm.onSortChange(SortBy.TEMP_MAX); sortMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val isWide = maxWidth >= 600.dp
                if (speciesList.isEmpty()) {
                    Text(stringResource(R.string.empty_list_hint))
                } else if (isWide) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gridItems(speciesList) { s: Species ->
                            SpeciesCard(s, onOpenDetails)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(speciesList) { s: Species ->
                            SpeciesCard(s, onOpenDetails)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeciesCard(s: Species, onOpenDetails: (Long) -> Unit) {
    ElevatedCard(
        onClick = { onOpenDetails(s.id) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(s.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))

            val addr = s.address ?: run {
                val lat = s.lat?.let { "%.5f".format(it) }
                val lng = s.lng?.let { "%.5f".format(it) }
                if (lat != null && lng != null) "$lat, $lng" else "-"
            }
            Text("${stringResource(R.string.address_label)}: $addr")

            Spacer(Modifier.height(2.dp))
            val t = "${s.minTemp ?: "-"}°C – ${s.maxTemp ?: "-"}°C"
            val h = "${s.minHumidity ?: "-"}% – ${s.maxHumidity ?: "-"}%"
            Text("${stringResource(R.string.temp)}: $t")
            Text("${stringResource(R.string.hum)}: $h")
        }
    }
}
