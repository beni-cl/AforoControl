package cl.inacap.aforocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.inacap.aforocontrol.ui.PantallaHistorial
import cl.inacap.aforocontrol.ui.PantallaLogin
import cl.inacap.aforocontrol.ui.PantallaPanel
import cl.inacap.aforocontrol.ui.TemaAforo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaAforo {
                App() } }
    }
}

@Composable
fun App(vm: AforoViewModel = viewModel()) {
    when (vm.pantalla) {
        Pantalla.LOGIN -> PantallaLogin(
            cargando = vm.cargando,
            error = vm.error,
            alIngresar = vm::ingresar
        )
        Pantalla.PANEL -> PantallaPanel(vm)
        Pantalla.HISTORIAL -> PantallaHistorial(
            eventos = vm.eventos,
            alVolver = { vm.irA(Pantalla.PANEL) }
        )
    }
}
