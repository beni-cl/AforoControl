package cl.inacap.aforocontrol.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.inacap.aforocontrol.modelo.Evento
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaHistorial(eventos: List<Evento>, alVolver: () -> Unit) {
    BackHandler(onBack = alVolver)
    val formato = remember { SimpleDateFormat("dd-MM HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Historial",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = alVolver) { Text("Volver") }
        }
        Text("Últimos 50 eventos, en tiempo real", fontSize = 12.sp, color = Gris)
        Spacer(modifier = Modifier.height(12.dp))

        if (eventos.isEmpty()) {
            Text("Todavía no hay eventos.", color = Gris)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(eventos) { ev ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(nombreEvento(ev), fontWeight = FontWeight.Bold, color = colorEvento(ev))
                        val fecha = ev.fecha?.let { formato.format(it) } ?: "..."
                        Text("$fecha · aforo ${ev.aforo} · ${ev.usuario}", fontSize = 12.sp, color = Gris)
                    }
                }
            }
        }
    }
}

fun nombreEvento(ev: Evento): String = when (ev.tipo) {
    "entrada" -> "Entrada"
    "entrada_rechazada" -> "Entrada rechazada (barrera cerrada)"
    "salida" -> "Salida"
    "vibracion" -> "Alerta: golpe en la puerta"
    "cmd_forzar_cierre" -> "Comando: forzar cierre"
    "cmd_quitar_cierre" -> "Comando: quitar cierre"
    "cmd_reset" -> "Comando: resetear contador"
    "cmd_maximo" -> "Comando: aforo máximo = ${ev.detalle}"
    "cmd_reconocer_alerta" -> "Comando: alerta reconocida"
    else -> ev.tipo
}

fun colorEvento(ev: Evento) = when {
    ev.tipo.startsWith("cmd_") -> Azul
    ev.tipo == "vibracion" || ev.tipo == "entrada_rechazada" -> Naranjo
    ev.tipo == "entrada" -> Verde
    else -> Gris
}
