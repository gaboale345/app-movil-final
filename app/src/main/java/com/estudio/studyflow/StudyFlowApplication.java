package com.estudio.studyflow;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class StudyFlowApplication extends Application {

    public static final String TIMER_CHANNEL_ID = "studyflow_timer_channel";
    public static final String ALERTS_CHANNEL_ID = "studyflow_alerts_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel timerChannel = new NotificationChannel(
                    TIMER_CHANNEL_ID,
                    "Temporizador de Estudio",
                    NotificationManager.IMPORTANCE_LOW
            );
            timerChannel.setDescription("Notificación interactiva persistente del cronómetro de estudio");

            NotificationChannel alertsChannel = new NotificationChannel(
                    ALERTS_CHANNEL_ID,
                    "Alertas y Metas de Estudio",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription("Alertas de fin de ciclo Pomodoro y metas en riesgo");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(timerChannel);
                manager.createNotificationChannel(alertsChannel);
            }
        }
    }
}
