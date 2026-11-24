package com.estudio.studyflow.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.estudio.studyflow.MainActivity;
import com.estudio.studyflow.R;
import com.estudio.studyflow.StudyFlowApplication;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StudyTimerService extends Service {

    public static final String ACTION_START_LIBRE = "ACTION_START_LIBRE";
    public static final String ACTION_START_POMODORO = "ACTION_START_POMODORO";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";

    public static final String EXTRA_MATERIA_ID = "EXTRA_MATERIA_ID";
    public static final String EXTRA_MATERIA_NOMBRE = "EXTRA_MATERIA_NOMBRE";
    public static final String EXTRA_TEMA = "EXTRA_TEMA";

    public static final int NOTIFICATION_ID = 1001;

    public enum TimerMode { LIBRE, POMODORO }
    public enum TimerStatus { IDLE, RUNNING, PAUSED, FINISHED }
    public enum PomodoroPhase { TRABAJO, DESCANSO_CORTO, DESCANSO_LARGO }

    // Constantes Pomodoro en segundos (25 min trabajo, 5 min descanso, 15 min descanso largo)
    public static final int POMODORO_TRABAJO_SEC = 25 * 60;
    public static final int POMODORO_DESCANSO_CORTO_SEC = 5 * 60;
    public static final int POMODORO_DESCANSO_LARGO_SEC = 15 * 60;
    public static final int CICLOS_PARA_DESCANSO_LARGO = 4;

    // LiveData observables por los ViewModels y Activities
    private static final MutableLiveData<TimerStatus> timerStatusLiveData = new MutableLiveData<>(TimerStatus.IDLE);
    private static final MutableLiveData<TimerMode> timerModeLiveData = new MutableLiveData<>(TimerMode.LIBRE);
    private static final MutableLiveData<PomodoroPhase> pomodoroPhaseLiveData = new MutableLiveData<>(PomodoroPhase.TRABAJO);
    private static final MutableLiveData<Long> secondsElapsedLiveData = new MutableLiveData<>(0L);
    private static final MutableLiveData<Long> effectiveSecondsLiveData = new MutableLiveData<>(0L);
    private static final MutableLiveData<Long> pomodoroRemainingSecLiveData = new MutableLiveData<>(0L);
    private static final MutableLiveData<Integer> pomodoroCycleLiveData = new MutableLiveData<>(1);
    private static final MutableLiveData<Long> currentMateriaIdLiveData = new MutableLiveData<>(0L);
    private static final MutableLiveData<String> currentMateriaNombreLiveData = new MutableLiveData<>("");
    private static final MutableLiveData<String> currentTemaLiveData = new MutableLiveData<>("");

    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;
    private long secondsElapsed = 0L;
    private long effectiveSeconds = 0L;
    private long pomodoroRemainingSec = POMODORO_TRABAJO_SEC;
    private int currentCycle = 1;
    private TimerMode currentMode = TimerMode.LIBRE;
    private PomodoroPhase currentPhase = PomodoroPhase.TRABAJO;
    private long sessionStartTime = 0L;

    // Getters públicos de LiveData
    public static LiveData<TimerStatus> getTimerStatus() { return timerStatusLiveData; }
    public static LiveData<TimerMode> getTimerMode() { return timerModeLiveData; }
    public static LiveData<PomodoroPhase> getPomodoroPhase() { return pomodoroPhaseLiveData; }
    public static LiveData<Long> getSecondsElapsed() { return secondsElapsedLiveData; }
    public static LiveData<Long> getEffectiveSeconds() { return effectiveSecondsLiveData; }
    public static LiveData<Long> getPomodoroRemainingSeconds() { return pomodoroRemainingSecLiveData; }
    public static LiveData<Integer> getPomodoroCycle() { return pomodoroCycleLiveData; }
    public static LiveData<Long> getCurrentMateriaId() { return currentMateriaIdLiveData; }
    public static LiveData<String> getCurrentMateriaNombre() { return currentMateriaNombreLiveData; }
    public static LiveData<String> getCurrentTema() { return currentTemaLiveData; }

    public static long getSessionStartTimeMs() {
        return System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            long materiaId = intent.getLongExtra(EXTRA_MATERIA_ID, 0L);
            String materiaNombre = intent.getStringExtra(EXTRA_MATERIA_NOMBRE);
            String tema = intent.getStringExtra(EXTRA_TEMA);

            if (materiaId > 0) {
                currentMateriaIdLiveData.postValue(materiaId);
            }
            if (materiaNombre != null) {
                currentMateriaNombreLiveData.postValue(materiaNombre);
            }
            if (tema != null) {
                currentTemaLiveData.postValue(tema);
            }

            switch (action) {
                case ACTION_START_LIBRE:
                    startTimer(TimerMode.LIBRE);
                    break;
                case ACTION_START_POMODORO:
                    startTimer(TimerMode.POMODORO);
                    break;
                case ACTION_PAUSE:
                    pauseTimer();
                    break;
                case ACTION_RESUME:
                    resumeTimer();
                    break;
                case ACTION_STOP:
                    stopTimer();
                    break;
            }
        }
        return START_STICKY;
    }

    private void startTimer(TimerMode mode) {
        this.currentMode = mode;
        this.isRunning = true;
        this.secondsElapsed = 0L;
        this.effectiveSeconds = 0L;
        this.currentCycle = 1;
        this.currentPhase = PomodoroPhase.TRABAJO;
        this.pomodoroRemainingSec = POMODORO_TRABAJO_SEC;
        this.sessionStartTime = System.currentTimeMillis();

        timerModeLiveData.postValue(currentMode);
        pomodoroPhaseLiveData.postValue(currentPhase);
        pomodoroCycleLiveData.postValue(currentCycle);
        secondsElapsedLiveData.postValue(secondsElapsed);
        effectiveSecondsLiveData.postValue(effectiveSeconds);
        pomodoroRemainingSecLiveData.postValue(pomodoroRemainingSec);
        timerStatusLiveData.postValue(TimerStatus.RUNNING);

        startForegroundNotification();
        startScheduler();
    }

    private void pauseTimer() {
        if (isRunning) {
            isRunning = false;
            stopScheduler();
            timerStatusLiveData.postValue(TimerStatus.PAUSED);
            updateNotification();
        }
    }

    private void resumeTimer() {
        if (!isRunning && timerStatusLiveData.getValue() == TimerStatus.PAUSED) {
            isRunning = true;
            timerStatusLiveData.postValue(TimerStatus.RUNNING);
            startScheduler();
            updateNotification();
        }
    }

    private void stopTimer() {
        isRunning = false;
        stopScheduler();
        timerStatusLiveData.postValue(TimerStatus.FINISHED);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void startScheduler() {
        stopScheduler();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void tick() {
        if (!isRunning) return;

        secondsElapsed++;
        secondsElapsedLiveData.postValue(secondsElapsed);

        if (currentMode == TimerMode.LIBRE) {
            effectiveSeconds++;
            effectiveSecondsLiveData.postValue(effectiveSeconds);
        } else {
            // Modo Pomodoro
            if (currentPhase == PomodoroPhase.TRABAJO) {
                effectiveSeconds++;
                effectiveSecondsLiveData.postValue(effectiveSeconds);
            }

            pomodoroRemainingSec--;
            pomodoroRemainingSecLiveData.postValue(pomodoroRemainingSec);

            if (pomodoroRemainingSec <= 0) {
                transicionarFasePomodoro();
            }
        }

        updateNotification();
    }

    private void transicionarFasePomodoro() {
        dispararAlertaSonoroVibratoria();

        if (currentPhase == PomodoroPhase.TRABAJO) {
            if (currentCycle % CICLOS_PARA_DESCANSO_LARGO == 0) {
                currentPhase = PomodoroPhase.DESCANSO_LARGO;
                pomodoroRemainingSec = POMODORO_DESCANSO_LARGO_SEC;
            } else {
                currentPhase = PomodoroPhase.DESCANSO_CORTO;
                pomodoroRemainingSec = POMODORO_DESCANSO_CORTO_SEC;
            }
        } else {
            currentPhase = PomodoroPhase.TRABAJO;
            currentCycle++;
            pomodoroRemainingSec = POMODORO_TRABAJO_SEC;
            pomodoroCycleLiveData.postValue(currentCycle);
        }

        pomodoroPhaseLiveData.postValue(currentPhase);
        pomodoroRemainingSecLiveData.postValue(pomodoroRemainingSec);
    }

    @SuppressWarnings("deprecation")
    private void dispararAlertaSonoroVibratoria() {
        try {
            // Vibración
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    vibratorManager.getDefaultVibrator().vibrate(
                            VibrationEffect.createWaveform(new long[]{0, 400, 200, 400}, -1)
                    );
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }

            // Alerta Sonora
            Uri alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alertUri);
            if (ringtone != null) {
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
                ringtone.play();
            }
        } catch (Exception ignored) {
        }
    }

    private void startForegroundNotification() {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private Notification buildNotification() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title;
        String contentText;
        String nombreMateria = currentMateriaNombreLiveData.getValue();
        if (nombreMateria == null || nombreMateria.isEmpty()) {
            nombreMateria = "Estudio";
        }

        if (currentMode == TimerMode.LIBRE) {
            title = "StudyFlow • " + nombreMateria;
            long mins = effectiveSeconds / 60;
            long secs = effectiveSeconds % 60;
            contentText = String.format(Locale.getDefault(), "Tiempo Efectivo: %02d:%02d (%s)",
                    mins, secs, isRunning ? "En marcha" : "Pausado");
        } else {
            String faseNombre;
            if (currentPhase == PomodoroPhase.TRABAJO) {
                faseNombre = "Trabajo";
            } else if (currentPhase == PomodoroPhase.DESCANSO_CORTO) {
                faseNombre = "Descanso Corto";
            } else {
                faseNombre = "Descanso Largo";
            }
            title = String.format(Locale.getDefault(), "Pomodoro (Ciclo %d) • %s", currentCycle, nombreMateria);
            long mins = pomodoroRemainingSec / 60;
            long secs = pomodoroRemainingSec % 60;
            contentText = String.format(Locale.getDefault(), "%s: %02d:%02d (%s)",
                    faseNombre, mins, secs, isRunning ? "Activo" : "Pausado");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, StudyFlowApplication.TIMER_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingOpen)
                .setOnlyAlertOnce(true)
                .setOngoing(isRunning);

        // Botón Pausar / Reanudar
        if (isRunning) {
            Intent pauseIntent = new Intent(this, StudyTimerService.class).setAction(ACTION_PAUSE);
            PendingIntent pendingPause = PendingIntent.getService(
                    this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.addAction(android.R.drawable.ic_media_pause, "Pausar", pendingPause);
        } else {
            Intent resumeIntent = new Intent(this, StudyTimerService.class).setAction(ACTION_RESUME);
            PendingIntent pendingResume = PendingIntent.getService(
                    this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.addAction(android.R.drawable.ic_media_play, "Reanudar", pendingResume);
        }

        // Botón Finalizar
        Intent stopIntent = new Intent(this, StudyTimerService.class).setAction(ACTION_STOP);
        PendingIntent pendingStop = PendingIntent.getService(
                this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Finalizar", pendingStop);

        return builder.build();
    }

    public long getStartTime() {
        return sessionStartTime;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
