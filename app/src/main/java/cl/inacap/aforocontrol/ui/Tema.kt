package cl.inacap.aforocontrol.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val Azul = Color(0xFF1565C0)
val Verde = Color(0xFF2E7D32)
val Amarillo = Color(0xFFF9A825)
val Naranjo = Color(0xFFE65100)
val Rojo = Color(0xFFC62828)
val Gris = Color(0xFF607D8B)

@Composable
fun TemaAforo(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Azul)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            contenido()
        }
    }
}
