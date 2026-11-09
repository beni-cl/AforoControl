package cl.inacap.aforocontrol

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.inacap.aforocontrol.datos.Repositorio
import cl.inacap.aforocontrol.modelo.EstadoNodo
import cl.inacap.aforocontrol.modelo.Evento
import cl.inacap.aforocontrol.modelo.Rol
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

enum class Pantalla { LOGIN, PANEL, HISTORIAL }

class AforoViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repo = Repositorio()
    private var escuchaNodo: ListenerRegistration? = null
    private var escuchaEventos: ListenerRegistration? = null

    var pantalla by mutableStateOf(Pantalla.LOGIN)
        private set
    var correo by mutableStateOf("")
        private set
    var rol by mutableStateOf<Rol?>(null)
        private set
    var estado by mutableStateOf(EstadoNodo())
        private set
    var eventos by mutableStateOf<List<Evento>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val esAdmin: Boolean
        get() = rol == Rol.ADMIN

    init {
        val actual = auth.currentUser?.email
        if (actual != null) cargarRol(actual.lowercase())
    }

    fun ingresar(correoIngresado: String, clave: String) {
        val limpio = correoIngresado.trim().lowercase()
        if (limpio.isEmpty() || clave.isEmpty()) {
            error = "Completa el correo y la contraseña"
            return
        }
        cargando = true
        error = null
        auth.signInWithEmailAndPassword(limpio, clave)
            .addOnSuccessListener { cargarRol(limpio) }
            .addOnFailureListener {
                cargando = false
                error = "Correo o contraseña incorrectos"
            }
    }

    private fun cargarRol(email: String) {
        cargando = true
        repo.obtenerRol(email) { rolLeido ->
            cargando = false
            if (rolLeido == null) {
                error = "Este usuario no tiene un rol asignado"
                auth.signOut()
            } else {
                correo = email
                rol = rolLeido
                pantalla = Pantalla.PANEL
                if (rolLeido == Rol.ADMIN) repo.crearNodoSiNoExiste()
                escuchaNodo = repo.escucharNodo { estado = it }
                escuchaEventos = repo.escucharEventos { eventos = it }
            }
        }
    }

    fun salir() {
        detenerEscuchas()
        auth.signOut()
        rol = null
        correo = ""
        pantalla = Pantalla.LOGIN
    }

    fun irA(destino: Pantalla) {
        pantalla = destino
    }

    fun simularEntrada() = repo.simularEntrada(correo)
    fun simularSalida() = repo.simularSalida(correo)
    fun simularGolpe() = repo.simularGolpe(correo, estado.aforo)

    fun forzarCierre() {
        if (esAdmin) repo.forzarCierre(!estado.forzado, correo, estado.aforo)
    }

    fun resetear() {
        if (esAdmin) repo.resetear(correo)
    }

    fun cambiarMaximo(nuevo: Int) {
        if (esAdmin && nuevo in 1..100) repo.cambiarMaximo(nuevo, correo, estado.aforo)
    }

    fun reconocerAlerta() {
        if (esAdmin) repo.reconocerAlerta(correo, estado.aforo)
    }

    private fun detenerEscuchas() {
        escuchaNodo?.remove()
        escuchaEventos?.remove()
        escuchaNodo = null
        escuchaEventos = null
    }

    override fun onCleared() {
        detenerEscuchas()
    }
}
