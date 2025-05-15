package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {

    private val deviceAddress = "00:1A:7D:DA:71:13" // Replace with your PC's Bluetooth MAC address
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    private var bondReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            RequestBluetoothPermissions()

            BluetoothAnswerApp { answer: String ->
                sendAnswerViaBluetooth(answer)
            }
        }
    }

    private fun sendAnswerViaBluetooth(answer: String) {
        Thread {
            try {
                val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)

                if (!hasBluetoothConnectPermission()) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Missing BLUETOOTH_CONNECT permission", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                }

                pairDeviceIfNeeded(device) {
                    try {
                        val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                        adapter.cancelDiscovery()
                        socket.connect()

                        val outputStream: OutputStream = socket.outputStream
                        outputStream.write(answer.toByteArray())
                        outputStream.flush()
                        socket.close()

                        runOnUiThread {
                            Toast.makeText(applicationContext, "Answer sent: $answer", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Failed to send answer", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: SecurityException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Missing Bluetooth permission", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun pairDeviceIfNeeded(device: BluetoothDevice, onPaired: () -> Unit) {
        if (!hasBluetoothConnectPermission()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Missing BLUETOOTH_CONNECT permission for pairing", Toast.LENGTH_LONG).show()
            }
            return
        }

        try {
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                onPaired()
            } else {
                bondReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val action = intent?.action
                        if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                            val changedDevice: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            val bondState = intent?.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                            if (changedDevice == device && bondState == BluetoothDevice.BOND_BONDED) {
                                context?.unregisterReceiver(this)
                                bondReceiver = null
                                onPaired()
                            }
                        }
                    }
                }

                registerReceiver(bondReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                device.createBond()
            }
        } catch (se: SecurityException) {
            runOnUiThread {
                Toast.makeText(applicationContext, "BLUETOOTH_CONNECT permission missing or denied", Toast.LENGTH_LONG).show()
            }
            bondReceiver?.let { unregisterReceiver(it) }
            bondReceiver = null
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

// ✅ Permissions Request Composable
@Composable
fun RequestBluetoothPermissions() {
    val context = LocalContext.current

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        Toast.makeText(
            context,
            if (granted) "Permissions granted" else "Some permissions denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}

// ✅ Simple Compose UI
@Composable
fun BluetoothAnswerApp(onSend: (String) -> Unit) {
    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Enter your answer:")
        TextField(
            value = answer,
            onValueChange = { answer = it },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (answer.isNotBlank()) {
                    onSend(answer)
                    answer = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Answer")
        }
    }
}
