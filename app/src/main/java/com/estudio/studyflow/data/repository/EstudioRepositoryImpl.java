package com.estudio.studyflow.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.estudio.studyflow.data.AppExecutors;
import com.estudio.studyflow.data.local.AppDatabase;
import com.estudio.studyflow.data.local.dao.EstudioDao;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.data.local.entity.SesionConMateria;
import com.estudio.studyflow.data.local.entity.SesionEstudio;
import com.estudio.studyflow.domain.repository.EstudioRepository;

import java.util.Calendar;
import java.util.List;

public class EstudioRepositoryImpl implements EstudioRepository {

    private static volatile EstudioRepositoryImpl sInstance;
    private final EstudioDao estudioDao;
    private final AppExecutors executors;

    public EstudioRepositoryImpl(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.estudioDao = database.estudioDao();
        this.executors = AppExecutors.getInstance();
    }

    public static EstudioRepositoryImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (EstudioRepositoryImpl.class) {
                if (sInstance == null) {
                    sInstance = new EstudioRepositoryImpl(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    // ==========================================
    // MATERIAS
    // ==========================================

    @Override
    public LiveData<List<Materia>> getMateriasActivas() {
        return estudioDao.getMateriasActivas();
    }

    @Override
    public LiveData<List<Materia>> getTodasLasMaterias() {
        return estudioDao.getTodasLasMaterias();
    }

    @Override
    public LiveData<Materia> getMateriaPorId(long id) {
        return estudioDao.getMateriaPorId(id);
    }

    @Override
    public void insertMateria(Materia materia, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.insertMateria(materia);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void updateMateria(Materia materia, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.updateMateria(materia);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void deleteMateria(Materia materia, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.deleteMateria(materia);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void setEstadoActivaMateria(long idMateria, boolean activa, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.setEstadoActivaMateria(idMateria, activa);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public LiveData<Integer> getCantidadMateriasActivas() {
        return estudioDao.getCantidadMateriasActivas();
    }

    // ==========================================
    // SESIONES
    // ==========================================

    @Override
    public void insertSesion(SesionEstudio sesion, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.insertSesion(sesion);
            // Actualizar metas semanales automáticamente (RF08)
            actualizarMetasSemanalesInterno(sesion.getIdMateria());
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void updateSesion(SesionEstudio sesion, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.updateSesion(sesion);
            actualizarMetasSemanalesInterno(sesion.getIdMateria());
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void deleteSesion(SesionEstudio sesion, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.deleteSesion(sesion);
            actualizarMetasSemanalesInterno(sesion.getIdMateria());
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public LiveData<List<SesionConMateria>> getSesionesConMateria() {
        return estudioDao.getSesionesConMateria();
    }

    @Override
    public LiveData<SesionConMateria> getProximaSesionPlanificada() {
        return estudioDao.getProximaSesionPlanificada(System.currentTimeMillis());
    }

    @Override
    public LiveData<List<SesionConMateria>> getSesionesPlanificadas() {
        return estudioDao.getSesionesPlanificadas();
    }

    @Override
    public LiveData<List<SesionConMateria>> getSesionesRecientes(int limite) {
        return estudioDao.getSesionesRecientes(limite);
    }

    @Override
    public LiveData<Integer> getMinutosEstudiadosHoy() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioHoy = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long finHoy = cal.getTimeInMillis();

        return estudioDao.getMinutosEstudiadosRangoLiveData(inicioHoy, finHoy);
    }

    @Override
    public LiveData<Integer> getMinutosEstudiadosSemana() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioSemana = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long finSemana = cal.getTimeInMillis();

        return estudioDao.getMinutosEstudiadosRangoLiveData(inicioSemana, finSemana);
    }

    @Override
    public List<SesionConMateria> getSesionesParaReporte(long inicio, long fin) {
        return estudioDao.getSesionesParaReporte(inicio, fin);
    }

    @Override
    public int calcularRachaDiasSync() {
        List<Long> dias = estudioDao.getDiasConEstudioSync();
        if (dias == null || dias.isEmpty()) {
            return 0;
        }

        long hoyDia = System.currentTimeMillis() / 86400000L;
        int racha = 0;
        long diaEsperado = hoyDia;

        // Si hoy no ha estudiado aún, puede ser que la racha continúe desde ayer
        if (!dias.contains(hoyDia)) {
            diaEsperado = hoyDia - 1;
        }

        for (Long dia : dias) {
            if (dia == diaEsperado) {
                racha++;
                diaEsperado--;
            } else if (dia < diaEsperado) {
                break;
            }
        }
        return racha;
    }

    // ==========================================
    // METAS
    // ==========================================

    @Override
    public LiveData<List<MetaSemanal>> getMetasPorSemana(int anio, int semana) {
        return estudioDao.getMetasPorSemana(anio, semana);
    }

    @Override
    public LiveData<MetaSemanal> getMetaGlobalPorSemana(int anio, int semana) {
        return estudioDao.getMetaGlobalPorSemana(anio, semana);
    }

    @Override
    public void upsertMeta(MetaSemanal meta, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            estudioDao.upsertMeta(meta);
            actualizarMetasSemanalesInterno(meta.getIdMateria() != null ? meta.getIdMateria() : 0L);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    @Override
    public void recalcularProgresoMetas(int anio, int semana, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            actualizarTodasLasMetasSync(anio, semana);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    private void actualizarMetasSemanalesInterno(long idMateria) {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int semana = cal.get(Calendar.WEEK_OF_YEAR);
        actualTodasLasMetasSync(anio, semana);
    }

    private void actualTodasLasMetasSync(int anio, int semana) {
        actualizarTodasLasMetasSync(anio, semana);
    }

    private void actualizarTodasLasMetasSync(int anio, int semana) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, anio);
        cal.set(Calendar.WEEK_OF_YEAR, semana);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioSemana = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long finSemana = cal.getTimeInMillis();

        // 1. Meta Global
        MetaSemanal metaGlobal = estudioDao.getMetaGlobalSync(anio, semana);
        if (metaGlobal != null) {
            int minutosTotales = estudioDao.getMinutosEstudiadosRangoSync(inicioSemana, finSemana);
            metaGlobal.setMinutosAlcanzados(minutosTotales);
            metaGlobal.setEstado(evaluarEstadoSemaforo(minutosTotales, metaGlobal.getMinutosObjetivo()));
            estudioDao.upsertMeta(metaGlobal);
        }
    }

    /**
     * Regla de semaforización (RF08, CP06, CP07):
     * - CUMPLIDA (Verde): alcanzado >= objetivo
     * - EN_PROGRESO (Amarillo): avance adecuado según el día de la semana
     * - EN_RIESGO (Rojo): avance muy por debajo de la cuota esperada (>2 días restantes y avance < 25%)
     */
    public static String evaluarEstadoSemaforo(int minutosAlcanzados, int minutosObjetivo) {
        if (minutosObjetivo <= 0) return "CUMPLIDA";
        float avance = ((float) minutosAlcanzados / (float) minutosObjetivo) * 100f;
        if (avance >= 100f) {
            return "CUMPLIDA";
        }

        Calendar cal = Calendar.getInstance();
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK); // Domingo=1, Lunes=2, ..., Viernes=6, Sábado=7

        // Si es viernes o más tarde y el avance es menor al 30% -> EN_RIESGO (CP07)
        if (diaSemana >= Calendar.FRIDAY && avance < 30f) {
            return "EN_RIESGO";
        }

        return "EN_PROGRESO";
    }
}
