package com.example.matrixcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    companion object {
        init { System.loadLibrary("native-lib") }
    }

    // JNI methods
    external fun addMatrices(rows: Int, cols: Int, a: DoubleArray, b: DoubleArray): DoubleArray
    external fun subtractMatrices(rows: Int, cols: Int, a: DoubleArray, b: DoubleArray): DoubleArray
    external fun multiplyMatrices(
        rowsA: Int, colsA: Int, rowsB: Int, colsB: Int,
        a: DoubleArray, b: DoubleArray
    ): DoubleArray
    external fun divideMatrices(
        rowsA: Int, colsA: Int, rowsB: Int, colsB: Int,
        a: DoubleArray, b: DoubleArray
    ): DoubleArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MatrixCalculatorApp() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MatrixCalculatorApp() {
        var rAText by remember { mutableStateOf("") }
        var cAText by remember { mutableStateOf("") }
        var rBText by remember { mutableStateOf("") }
        var cBText by remember { mutableStateOf("") }
        var aInput by remember { mutableStateOf("") }
        var bInput by remember { mutableStateOf("") }
        var op by remember { mutableStateOf("Add") }
        var result by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Matrix A dimensions", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rAText, onValueChange = { rAText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("rows") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cAText, onValueChange = { cAText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("cols") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Text("Matrix B dimensions", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rBText, onValueChange = { rBText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("rows") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cBText, onValueChange = { cBText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("cols") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = aInput, onValueChange = { aInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Matrix A values (space-separated)") }
            )

            OutlinedTextField(
                value = bInput, onValueChange = { bInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Matrix B values (space-separated)") }
            )

            Text("Operation:", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("Add", "Subtract", "Multiply", "Divide").forEach { choice ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        RadioButton(
                            selected = op == choice,
                            onClick = { op = choice }
                        )
                        Text(choice, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Button(onClick = {
                // parse dims
                val rA = rAText.toIntOrNull()
                val cA = cAText.toIntOrNull()
                val rB = rBText.toIntOrNull()
                val cB = cBText.toIntOrNull()
                if (rA == null || cA == null || rB == null || cB == null ||
                    rA <= 0 || cA <= 0 || rB <= 0 || cB <= 0
                ) {
                    result = "❌ Enter valid positive dimensions"
                    return@Button
                }

                // parse values
                val aList = aInput.trim().split("\\s+".toRegex()).mapNotNull { it.toDoubleOrNull() }
                val bList = bInput.trim().split("\\s+".toRegex()).mapNotNull { it.toDoubleOrNull() }
                if (aList.size != rA * cA || bList.size != rB * cB) {
                    result = "❌ Need exactly ${rA*cA} values for A and ${rB*cB} for B"
                    return@Button
                }

                // dimension checks per operation
                val flatA = aList.toDoubleArray()
                val flatB = bList.toDoubleArray()
                val out: DoubleArray
                val rowsOut: Int
                val colsOut: Int

                when (op) {
                    "Add", "Subtract" -> {
                        if (rA != rB || cA != cB) {
                            result = "❌ A and B must have same dims for Add/Subtract"
                            return@Button
                        }
                        rowsOut = rA; colsOut = cA
                        out = if (op == "Add")
                            addMatrices(rA, cA, flatA, flatB)
                        else
                            subtractMatrices(rA, cA, flatA, flatB)
                    }
                    "Multiply" -> {
                        if (cA != rB) {
                            result = "❌ colsA must equal rowsB for Multiply"
                            return@Button
                        }
                        rowsOut = rA; colsOut = cB
                        out = multiplyMatrices(rA, cA, rB, cB, flatA, flatB)
                    }
                    else -> { // Divide
                        if (rB != cB || cA != rB) {
                            result = "❌ B must be square and colsA==rowsB for Divide"
                            return@Button
                        }
                        rowsOut = rA; colsOut = rB
                        out = divideMatrices(rA, cA, rB, cB, flatA, flatB)
                    }
                }

                // format & display
                result = buildString {
                    for (i in 0 until rowsOut) {
                        for (j in 0 until colsOut) {
                            append(String.format("%.3f", out[i*colsOut + j]))
                            if (j < colsOut - 1) append(" ")
                        }
                        if (i < rowsOut - 1) append("\n")
                    }
                }
            }) {
                Text("Calculate")
            }

            Spacer(Modifier.height(8.dp))
            Text("Result:", style = MaterialTheme.typography.titleMedium)
            Text(result, modifier = Modifier.fillMaxWidth())
        }
    }
}
