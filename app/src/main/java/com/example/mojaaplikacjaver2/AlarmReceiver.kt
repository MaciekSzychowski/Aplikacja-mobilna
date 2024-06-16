package com.example.mojaaplikacjaver2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver(){                           // klasa odpowiedzialna za wyswietlanie powiadomien w konkretnym momencie
    override fun onReceive(context: Context, intent: Intent?) {                 // dokumentacja https://developer.android.com/develop/ui/views/notifications/build-notification?hl=pl
        val builder = NotificationCompat.Builder(
            context, "monthlyReminder").setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Przypomnienie").setContentText("Wprowadź dawki otrzymane w tym miesiącu").setPriority(
            NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)  // tworzenie obiektu managera powiadowmien
        if (ActivityCompat.checkSelfPermission(context,                     // automatycznie wygenerowane przez andriod studio, sprawdzające czy mamy pozwolenie za wyswietlenie powiadomienia
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1001, builder.build())

    }
}