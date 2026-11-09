package cl.inacap.aforocontrol.modelo

object LogicaAforo {
    const val UMBRAL_AMARILLO = 0.7

    fun semaforo(e: EstadoNodo): Semaforo = when {
        e.forzado || e.aforo >= e.maximo -> Semaforo.ROJO
        e.aforo >= e.maximo * UMBRAL_AMARILLO -> Semaforo.AMARILLO
        else -> Semaforo.VERDE
    }

    fun barreraCerrada(e: EstadoNodo): Boolean = e.forzado || e.aforo >= e.maximo

    fun puedeEntrar(e: EstadoNodo): Boolean = !barreraCerrada(e)

    fun puedeSalir(e: EstadoNodo): Boolean = e.aforo > 0
}
