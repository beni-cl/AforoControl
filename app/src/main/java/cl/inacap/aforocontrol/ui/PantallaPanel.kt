package cl.inacap.aforocontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.inacap.aforocontrol.AforoViewModel
import cl.inacap.aforocontrol.Pantalla
import cl.inacap.aforocontrol.modelo.LogicaAforo
import cl.inacap.aforocontrol.modelo.Semaforo

@Composable
fun PantallaPanel(vm: AforoViewModel) {
    val e = vm.estado
    val semaforo = LogicaAforo.semaforo(e)
    val cerrada = LogicaAforo.barreraCerrada(e)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (vm.esAdmin) "Panel Administrador" else "Panel Guardia",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(vm.correo, fontSize = 13.sp, color = Gris)
            }
            TextButton(onClick = vm::salir) { Text("Salir") }
        }
        Espacio(16)

        if (e.alertaVibracion) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "⚠ Golpe detectado en la puerta",
                        color = Naranjo,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (vm.esAdmin) {
                        TextButton(onClick = vm::reconocerAlerta) { Text("Reconocer") }
                    }
                }
            }
            Espacio(12)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Aforo actual", color = Gris)
                    Text("${e.aforo} / ${e.maximo}", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { (e.aforo.toFloat() / e.maximo).coerceIn(0f, 1f) },
                        color = colorDe(semaforo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                    Espacio(6)
                    Text(textoDe(semaforo), color = colorTextoDe(semaforo), fontWeight = FontWeight.Bold)
                }
                VistaSemaforo(semaforo)
            }
        }
        Espacio(12)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (cerrada) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            val texto = when {
                e.forzado -> "Barrera: CERRADA (forzada por el Admin)"
                cerrada -> "Barrera: CERRADA (aforo lleno)"
                else -> "Barrera: ABIERTA"
            }
            Text(
                texto,
                color = if (cerrada) Rojo else Verde,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(14.dp)
            )
        }

        if (vm.esAdmin) {
            Titulo("Comandos")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = vm::forzarCierre,
                    colors = ButtonDefaults.buttonColors(containerColor = if (e.forzado) Verde else Rojo),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (e.forzado) "Quitar cierre" else "Forzar cierre")
                }
                OutlinedButton(onClick = vm::resetear, modifier = Modifier.weight(1f)) {
                    Text("Resetear")
                }
            }
            Espacio(8)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aforo máximo", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { vm.cambiarMaximo(e.maximo - 1) }) { Text("−") }
                Text(
                    "${e.maximo}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                OutlinedButton(onClick = { vm.cambiarMaximo(e.maximo + 1) }) { Text("+") }
            }
        } else {
            Espacio(12)
            Text("Rol Guardia: solo lectura, sin comandos.", fontSize = 13.sp, color = Gris)
        }

        Titulo("Simulación del nodo IoT")
        Text(
            "Estos botones reemplazan a los sensores del Arduino.",
            fontSize = 12.sp,
            color = Gris
        )
        Espacio(8)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = vm::simularEntrada, modifier = Modifier.weight(1f)) { Text("Entrada") }
            OutlinedButton(onClick = vm::simularSalida, modifier = Modifier.weight(1f)) { Text("Salida") }
            OutlinedButton(onClick = vm::simularGolpe, modifier = Modifier.weight(1f)) { Text("Golpe") }
        }

        Espacio(24)
        Button(onClick = { vm.irA(Pantalla.HISTORIAL) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver historial")
        }
    }
}

@Composable
fun VistaSemaforo(actual: Semaforo) {
    Column(
        modifier = Modifier
            .background(Color(0xFF37474F), RoundedCornerShape(12.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Luz(Rojo, actual == Semaforo.ROJO)
        Luz(Amarillo, actual == Semaforo.AMARILLO)
        Luz(Verde, actual == Semaforo.VERDE)
    }
}

@Composable
private fun Luz(color: Color, encendida: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(color.copy(alpha = if (encendida) 1f else 0.2f), CircleShape)
    )
}

@Composable
private fun Titulo(texto: String) {
    Espacio(20)
    Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Espacio(8)
}

@Composable
private fun Espacio(alto: Int) {
    Spacer(modifier = Modifier.height(alto.dp))
}

fun colorDe(s: Semaforo): Color = when (s) {
    Semaforo.VERDE -> Verde
    Semaforo.AMARILLO -> Amarillo
    Semaforo.ROJO -> Rojo
}

fun colorTextoDe(s: Semaforo): Color = if (s == Semaforo.AMARILLO) Naranjo else colorDe(s)

fun textoDe(s: Semaforo): String = when (s) {
    Semaforo.VERDE -> "Disponible"
    Semaforo.AMARILLO -> "Próximo al límite"
    Semaforo.ROJO -> "Sin acceso"
}
