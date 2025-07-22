package com.soltrak

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soltrak.ui.navigation.AppNavHost
import com.soltrak.ui.navigation.AppScreen
import com.soltrak.ui.theme.SoltrakTheme
import com.soltrak.utils.PreferencesManager
import com.soltrak.utils.RFIDHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cipherlab.rfidapi.RfidManager
import com.soltrak.ui.screen.rfid.inventoryScreen.InventoryViewModel
import java.util.Calendar

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ModuleViewModel by viewModels()

    @Inject
    lateinit var rfidHelper: RFIDHelper

    @Inject
    lateinit var prefsManager: PreferencesManager

    private fun saveProgressToPref(progress: Int, status: String) {
        prefsManager.progressPercentage = progress
        prefsManager.importStatusText = status
    }

    private fun loadProgressFromPref(): Pair<Int, String> {
        return prefsManager.progressPercentage to prefsManager.importStatusText
    }

    private fun clearProgressPref() {
        prefsManager.clear()
    }

    private fun readZipFile(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                viewModel.isLoading.value = true
                val datFiles = mutableListOf<File>()

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry

                        while (entry != null) {
                            if (!entry.isDirectory && entry.name.endsWith(".dat")) {
                                val fileName = entry.name.substringAfterLast('/')
                                val tempFile = File.createTempFile(fileName, ".dat", cacheDir)

                                FileOutputStream(tempFile).use { out ->
                                    val buffer = ByteArray(8 * 1024)
                                    var len: Int
                                    while (zipInputStream.read(buffer).also { len = it } != -1) {
                                        out.write(buffer, 0, len)
                                    }
                                }

                                datFiles.add(tempFile)
                            }

                            zipInputStream.closeEntry()
                            entry = zipInputStream.nextEntry
                        }
                    }
                }

                if (datFiles.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "No .dat file found inside the ZIP!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    clearProgressPref()
                    return@launch
                }

                for ((index, file) in datFiles.withIndex()) {
                    val progress = ((index + 1) * 100) / datFiles.size
                    val statusText = "Importing:(${index + 1}/${datFiles.size})"

                    viewModel.progressPercentage.value = progress
                    viewModel.importStatusText.value = statusText
                    saveProgressToPref(progress, statusText)

                    viewModel.importModulesFromJson(FileInputStream(file))
                }

                viewModel.importStatusText.value = "Data imported successfully."
                viewModel.progressPercentage.value = 100

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Data imported successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                viewModel.importStatusText.value = "Import failed"
                viewModel.progressPercentage.value = 0

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error reading ZIP file: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                viewModel.isLoading.value = false
                clearProgressPref()

                if (viewModel.isLocalDbEmpty()) {
                    viewModel.progressPercentage.value = 0
                    viewModel.importStatusText.value = "No data found in local database"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "No data found in file.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) return it.getString(index)
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (savedProgress, savedStatus) = loadProgressFromPref()
        viewModel.progressPercentage.value = savedProgress
        viewModel.importStatusText.value = savedStatus

        setContent {
            SoltrakTheme {
                val context = LocalContext.current
                var isUnlocked by remember { mutableStateOf(false) }
                var sessionChecked by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    isUnlocked = prefsManager.isUnlocked
                    sessionChecked = true
                }

                if (sessionChecked) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isUnlocked) {
                            SoltrakAppScreen(
                                viewModel = viewModel,
                                finish = { finish() },
                                readZipFile = { uri -> readZipFile(uri) },
                                clearProgressPref = { clearProgressPref() },
                                getFileNameFromUri = { context, uri -> getFileNameFromUri(context, uri) }
                            )
                        } else {
                            PasswordOverlay(
                                correctPassword = "Hotmail.com",
                                onPasswordCorrect = {
                                    isUnlocked = true
                                    prefsManager.isUnlocked = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoltrakAppScreen(
    viewModel: ModuleViewModel,
    finish: () -> Unit,
    readZipFile: (Uri) -> Unit,
    clearProgressPref: () -> Unit,
    getFileNameFromUri: (Context, Uri) -> String?
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.7f
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val isLoading by viewModel.isLoading
    val progressPercentage by viewModel.progressPercentage
    val importStatusText by viewModel.importStatusText
    val toastMessage by viewModel.toastMessage.observeAsState()

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentDrawerItem = getDrawerItemForRoute(currentRoute)

    var showImportDialog by remember { mutableStateOf(false) }
    val invalidFileDialog = remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    var showFullScreenProgress by remember { mutableStateOf(false) }
    var fullScreenProgressText by remember { mutableStateOf("") }

    var showExportCompleteDialog by remember { mutableStateOf(false) }
    var exportedFilePath by remember { mutableStateOf("") }

    var resetTriggered by remember { mutableStateOf(false) }
    var scanMode by remember { mutableStateOf("EPC") }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            val pattern = Regex("^DatabaseForHHD_\\d{12}\\.zip$")
            if (fileName != null && pattern.matches(fileName)) {
                selectedUri = it
                fullScreenProgressText = "Importing data..."
                viewModel.deleteAll()
                clearProgressPref()
                readZipFile(it)
            } else {
                invalidFileDialog.value = true
            }
        }
    }

    val drawerItems = listOf(
        DrawerItem("Scan Barcode", iconRes = R.drawable.barcode) {
            navController.navigate(AppScreen.BarcodeScanScreen.route)
        },
        DrawerItem("Check RFID", iconRes = R.drawable.rfid) {
            navController.navigate(AppScreen.InventoryScreen.route)
        },
        DrawerItem("Import Data", iconRes = R.drawable.data_import) {
            showImportDialog = true
        },
        DrawerItem("Export Data", iconRes = R.drawable.data_import) {
            showFullScreenProgress = true
            fullScreenProgressText = "Exporting data..."
            viewModel.exportUidHistoryToZip(
                onExportComplete = { path ->
                    showFullScreenProgress = false
                    exportedFilePath = path
                    showExportCompleteDialog = true
                },
                onExportFailed = {
                    showFullScreenProgress = false
                    Toast.makeText(context, "Export failed", Toast.LENGTH_LONG).show()
                }
            )
        },
        DrawerItem("Settings", iconVector = Icons.Default.Settings) {
            navController.navigate(AppScreen.SettingsScreen.route)
        },
        DrawerItem("Support", iconVector = Icons.Default.Info) {
            navController.navigate(AppScreen.SupportScreen.route)
        }
    )

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
    val emoji = when (greeting) {
        "Good Morning" -> "\uD83D\uDD06"
        "Good Afternoon" -> "\u2600\uFE0F"
        "Good Evening" -> "\uD83C\uDF1D"
        else -> "\uD83C\uDF03"
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.takeIf { it.isNotEmpty() }?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        navController.navigate(AppScreen.BarcodeScanScreen.route) {
            popUpTo(0)
        }
    }

    BackHandler {
        if (currentRoute == AppScreen.BarcodeScanScreen.route) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                finish()
            } else {
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = currentTime
            }
        } else {
            navController.popBackStack()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLoading || !showFullScreenProgress,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(drawerWidth)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bizorbit_logo),
                        contentDescription = "BizOrbit Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$greeting $emoji",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "BizOrbit Technologies",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            item.iconVector?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            } ?: item.iconRes?.let {
                                Icon(
                                    painter = painterResource(id = it),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == getRouteFromItem(item),
                        onClick = {
                            item.onClick()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                var isMenuExpanded by remember { mutableStateOf(false) }

                TopAppBar(
                    title = { Text(currentDrawerItem?.label ?: "Soltrak") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }

                            Text(
                                text = "$progressPercentage%",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (currentRoute == AppScreen.InventoryScreen.route) {

                                IconButton(onClick = {
                                    resetTriggered = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset",
                                        tint = Color.White
                                    )
                                }

                                Box {
                                    IconButton(onClick = { isMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Menu",
                                            tint = Color.White
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = isMenuExpanded,
                                        onDismissRequest = { isMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(selected = scanMode == "EPC", onClick = null)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("EPC")
                                                }
                                            },
                                            onClick = {
                                                scanMode = "EPC"
                                                isMenuExpanded = false
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(selected = scanMode == "TID", onClick = null)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("TID")
                                                }
                                            },
                                            onClick = {
                                                scanMode = "TID"
                                                isMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
            ,
            content = { innerPadding ->
                AppNavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    isLoading = isLoading,
                    progressPercent = progressPercentage,
                    importStatusText = importStatusText,
                    viewModel = viewModel,
                    onPickFile = { showImportDialog = true },
                    onExportFile = {},
                    resetTriggered = resetTriggered,
                    scanMode = scanMode,
                    onResetConsumed = { resetTriggered = false }
                )
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Caution: Irreversible Operation") },
            text = { Text("Importing a new database will erase all current data. Do you want to continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importFileLauncher.launch(arrayOf("application/zip"))
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("No") }
            }
        )
    }

    if (invalidFileDialog.value) {
        AlertDialog(
            onDismissRequest = { invalidFileDialog.value = false },
            title = { Text("Invalid File") },
            text = { Text("Invalid file selected. Please choose a valid file to import.") },
            confirmButton = {
                TextButton(onClick = { invalidFileDialog.value = false }) { Text("OK") }
            }
        )
    }

    if (isLoading || showFullScreenProgress) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                .pointerInput(Unit) {}
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$fullScreenProgressText$progressPercentage%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }


    if (showExportCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showExportCompleteDialog = false },
            title = { Text("Export Complete") },
            text = { Text("Exported successfully to:\n$exportedFilePath") },
            confirmButton = {
                TextButton(onClick = { showExportCompleteDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}


fun getDrawerItemForRoute(route: String?): DrawerItem? {
    return when (route) {
        AppScreen.BarcodeScanScreen.route -> DrawerItem(
            "Scan Barcode",
            iconRes = R.drawable.barcode
        ) {}

        AppScreen.InventoryScreen.route -> DrawerItem("Check RFID", iconRes = R.drawable.rfid) {}
        AppScreen.SettingsScreen.route -> DrawerItem(
            "Settings",
            iconVector = Icons.Default.Settings
        ) {}

        AppScreen.SupportScreen.route -> DrawerItem("Support", iconVector = Icons.Default.Info) {}
        else -> null
    }
}

fun getRouteFromItem(item: DrawerItem): String? {
    return when (item.label) {
        "Scan Barcode" -> AppScreen.BarcodeScanScreen.route
        "Check RFID" -> AppScreen.InventoryScreen.route
        "Settings" -> AppScreen.SettingsScreen.route
        "Support" -> AppScreen.SupportScreen.route
        else -> null
    }
}


data class DrawerItem(
    val label: String,
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null,
    val onClick: () -> Unit
)

@Composable
fun PasswordOverlay(
    correctPassword: String = "Hotmail.com",
    onPasswordCorrect: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter Password", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorText = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    isError = errorText != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.Black)
                )

                if (errorText != null) {
                    Text(
                        errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    if (passwordInput == correctPassword) {
                        onPasswordCorrect()
                    } else {
                        errorText = "Incorrect password"
                    }
                }) {
                    Text("Continue")
                }
            }
        }
    }
}





