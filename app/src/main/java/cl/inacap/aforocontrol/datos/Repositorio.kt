package cl.inacap.aforocontrol.datos

import cl.inacap.aforocontrol.modelo.EstadoNodo
import cl.inacap.aforocontrol.modelo.Evento
import cl.inacap.aforocontrol.modelo.LogicaAforo
import cl.inacap.aforocontrol.modelo.Rol
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class Repositorio {
    private val db = FirebaseFirestore.getInstance()
    private val nodo = db.collection("nodo").document("sala1")
    private val eventos = db.collection("eventos")

    fun obtenerRol(correo: String, respuesta: (Rol?) -> Unit) {
        db.collection("roles").document(correo).get()
            .addOnSuccessListener { doc ->
                val rol = when (doc.getString("rol")) {
                    "admin" -> Rol.ADMIN
                    "guardia" -> Rol.GUARDIA
                    else -> null
                }
                respuesta(rol)
            }
            .addOnFailureListener { respuesta(null) }
    }

    fun escucharNodo(alCambiar: (EstadoNodo) -> Unit): ListenerRegistration =
        nodo.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) alCambiar(aEstado(doc))
        }

    fun escucharEventos(alCambiar: (List<Evento>) -> Unit): ListenerRegistration =
        eventos.orderBy("fecha", Query.Direction.DESCENDING).limit(50)
            .addSnapshotListener { resultado, _ ->
                if (resultado == null) return@addSnapshotListener
                val lista = resultado.documents.map { d ->
                    Evento(
                        tipo = d.getString("tipo") ?: "",
                        aforo = d.getLong("aforo")?.toInt() ?: 0,
                        usuario = d.getString("usuario") ?: "",
                        detalle = d.getString("detalle") ?: "",
                        fecha = d.getTimestamp("fecha", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()
                    )
                }
                alCambiar(lista)
            }

    fun crearNodoSiNoExiste() {
        nodo.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                nodo.set(
                    mapOf(
                        "aforo" to 0,
                        "maximo" to 10,
                        "forzado" to false,
                        "alertaVibracion" to false,
                        "actualizado" to FieldValue.serverTimestamp()
                    )
                )
            }
        }
    }

    fun simularEntrada(usuario: String) {
        db.runTransaction { t ->
            val e = aEstado(t.get(nodo))
            if (LogicaAforo.puedeEntrar(e)) {
                t.update(nodo, mapOf("aforo" to e.aforo + 1, "actualizado" to FieldValue.serverTimestamp()))
                Pair("entrada", e.aforo + 1)
            } else {
                Pair("entrada_rechazada", e.aforo)
            }
        }.addOnSuccessListener { (tipo, aforo) -> registrarEvento(tipo, aforo, usuario) }
    }

    fun simularSalida(usuario: String) {
        db.runTransaction { t ->
            val e = aEstado(t.get(nodo))
            if (LogicaAforo.puedeSalir(e)) {
                t.update(nodo, mapOf("aforo" to e.aforo - 1, "actualizado" to FieldValue.serverTimestamp()))
                e.aforo - 1
            } else {
                -1
            }
        }.addOnSuccessListener { aforo -> if (aforo >= 0) registrarEvento("salida", aforo, usuario) }
    }

    fun simularGolpe(usuario: String, aforoActual: Int) {
        nodo.update(mapOf<String, Any>("alertaVibracion" to true, "actualizado" to FieldValue.serverTimestamp()))
        registrarEvento("vibracion", aforoActual, usuario)
    }

    fun forzarCierre(activar: Boolean, usuario: String, aforoActual: Int) {
        nodo.update(mapOf<String, Any>("forzado" to activar, "actualizado" to FieldValue.serverTimestamp()))
        registrarEvento(if (activar) "cmd_forzar_cierre" else "cmd_quitar_cierre", aforoActual, usuario)
    }

    fun resetear(usuario: String) {
        nodo.update(
            mapOf<String, Any>(
                "aforo" to 0,
                "forzado" to false,
                "alertaVibracion" to false,
                "actualizado" to FieldValue.serverTimestamp()
            )
        )
        registrarEvento("cmd_reset", 0, usuario)
    }

    fun cambiarMaximo(nuevo: Int, usuario: String, aforoActual: Int) {
        nodo.update(mapOf<String, Any>("maximo" to nuevo, "actualizado" to FieldValue.serverTimestamp()))
        registrarEvento("cmd_maximo", aforoActual, usuario, detalle = nuevo.toString())
    }

    fun reconocerAlerta(usuario: String, aforoActual: Int) {
        nodo.update(mapOf<String, Any>("alertaVibracion" to false, "actualizado" to FieldValue.serverTimestamp()))
        registrarEvento("cmd_reconocer_alerta", aforoActual, usuario)
    }

    private fun registrarEvento(tipo: String, aforo: Int, usuario: String, detalle: String = "") {
        eventos.add(
            mapOf(
                "tipo" to tipo,
                "aforo" to aforo,
                "usuario" to usuario,
                "detalle" to detalle,
                "fecha" to FieldValue.serverTimestamp()
            )
        )
    }

    private fun aEstado(doc: DocumentSnapshot) = EstadoNodo(
        aforo = doc.getLong("aforo")?.toInt() ?: 0,
        maximo = doc.getLong("maximo")?.toInt() ?: 10,
        forzado = doc.getBoolean("forzado") ?: false,
        alertaVibracion = doc.getBoolean("alertaVibracion") ?: false
    )
}
