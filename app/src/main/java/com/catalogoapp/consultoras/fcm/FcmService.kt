package com.catalogoapp.consultoras.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.catalogoapp.consultoras.R
import com.catalogoapp.consultoras.data.repository.PedidoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser == null) return
        CoroutineScope(Dispatchers.IO).launch {
            PedidoRepository().registrarDispositivo(token)
        }
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        super.onMessageReceived(mensaje)
        val titulo = mensaje.notification?.title ?: getString(R.string.app_name)
        val cuerpo = mensaje.notification?.body ?: ""
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val canalId = "pedidos"
        val administrador = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Actualizaciones de pedidos", NotificationManager.IMPORTANCE_DEFAULT)
            administrador.createNotificationChannel(canal)
        }
        val notificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .build()
        administrador.notify(System.currentTimeMillis().toInt(), notificacion)
    }
}
