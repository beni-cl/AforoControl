package cl.inacap.aforocontrol.modelo

import java.util.Date

enum class Rol { ADMIN, GUARDIA }

enum class Semaforo { VERDE, AMARILLO, ROJO }

data class EstadoNodo(
    val aforo: Int = 0,
    val maximo: Int = 10,
    val forzado: Boolean = false,
    val alertaVibracion: Boolean = false
)

data class Evento(
    val tipo: String,
    val aforo: Int,
    val usuario: String,
    val detalle: String,
    val fecha: Date?
)
