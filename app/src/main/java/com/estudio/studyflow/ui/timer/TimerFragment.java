package com.estudio.studyflow.ui.timer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.estudio.studyflow.R;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.databinding.DialogFinalizarSesionBinding;
import com.estudio.studyflow.databinding.DialogRegistroManualBinding;
import com.estudio.studyflow.databinding.FragmentTimerBinding;
import com.estudio.studyflow.service.StudyTimerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;
    private TimerViewModel viewModel;

    private List<Materia> listaMaterias = new ArrayList<>();
    private Materia materiaSeleccionada = null;
    private boolean esModoPomodoro = false;

    private StudyTimerService.TimerStatus currentStatus = StudyTimerService.TimerStatus.IDLE;
    private StudyTimerService.PomodoroPhase currentPhase = StudyTimerService.PomodoroPhase.TRABAJO;
    private int currentCycle = 1;
    private long lastEffectiveSeconds = 0;
    private long lastElapsedSeconds = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TimerViewModel.class);

        configurarUI();
        observarViewModel();
    }

    private void configurarUI() {
        binding.toggleModo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                esModoPomodoro = (checkedId == R.id.btn_modo_pomodoro);
                actualizarVisibilidadModo();
            }
        });

        binding.actvMateria.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaMaterias.size()) {
                materiaSeleccionada = listaMaterias.get(position);
                binding.tilMateria.setError(null);
            }
        });

        binding.btnIniciar.setOnClickListener(v -> iniciarSesion());

        binding.btnPausarReanudar.setOnClickListener(v -> {
            if (getContext() == null) return;
            if (currentStatus == StudyTimerService.TimerStatus.RUNNING) {
                viewModel.pausarTimer(getContext());
            } else if (currentStatus == StudyTimerService.TimerStatus.PAUSED) {
                viewModel.reanudarTimer(getContext());
            }
        });

        binding.btnFinalizar.setOnClickListener(v -> finalizarSesion());

        binding.btnRegistroManual.setOnClickListener(v -> mostrarDialogRegistroManual());
    }

    private void actualizarVisibilidadModo() {
        if (esModoPomodoro) {
            binding.tvPomodoroBadge.setVisibility(View.VISIBLE);
            actualizarPomodoroBadge();
        } else {
            binding.tvPomodoroBadge.setVisibility(View.GONE);
        }
    }

    private void observarViewModel() {
        viewModel.getMateriasActivas().observe(getViewLifecycleOwner(), materias -> {
            listaMaterias = materias != null ? materias : new ArrayList<>();
            configurarDropdownMaterias();
        });

        viewModel.getTimerStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) status = StudyTimerService.TimerStatus.IDLE;
            currentStatus = status;
            actualizarEstadoControles(status);
        });

        viewModel.getTimerMode().observe(getViewLifecycleOwner(), mode -> {
            if (mode != null && currentStatus != StudyTimerService.TimerStatus.IDLE) {
                esModoPomodoro = (mode == StudyTimerService.TimerMode.POMODORO);
                binding.toggleModo.check(esModoPomodoro ? R.id.btn_modo_pomodoro : R.id.btn_modo_libre);
                actualizarVisibilidadModo();
            }
        });

        viewModel.getPomodoroPhase().observe(getViewLifecycleOwner(), phase -> {
            if (phase != null) {
                currentPhase = phase;
                actualizarPomodoroBadge();
            }
        });

        viewModel.getPomodoroCycle().observe(getViewLifecycleOwner(), cycle -> {
            if (cycle != null) {
                currentCycle = cycle;
                actualizarPomodoroBadge();
            }
        });

        viewModel.getSecondsElapsed().observe(getViewLifecycleOwner(), seconds -> {
            if (seconds != null) {
                lastElapsedSeconds = seconds;
                if (!esModoPomodoro && currentStatus != StudyTimerService.TimerStatus.IDLE) {
                    binding.tvTimerDisplay.setText(formatearTiempo(seconds));
                }
            }
        });

        viewModel.getEffectiveSeconds().observe(getViewLifecycleOwner(), effectiveSec -> {
            if (effectiveSec != null) {
                lastEffectiveSeconds = effectiveSec;
                binding.tvTiempoEfectivoLabel.setText(
                        String.format(Locale.getDefault(), "Tiempo efectivo: %s", formatearTiempo(effectiveSec))
                );
            }
        });

        viewModel.getPomodoroRemainingSeconds().observe(getViewLifecycleOwner(), remSec -> {
            if (remSec != null && esModoPomodoro && currentStatus != StudyTimerService.TimerStatus.IDLE) {
                binding.tvTimerDisplay.setText(formatearTiempo(remSec));
            }
        });

        viewModel.getCurrentMateriaNombre().observe(getViewLifecycleOwner(), nombre -> {
            if (nombre != null && !nombre.isEmpty() && currentStatus != StudyTimerService.TimerStatus.IDLE) {
                binding.actvMateria.setText(nombre, false);
            }
        });

        viewModel.getCurrentTema().observe(getViewLifecycleOwner(), tema -> {
            if (tema != null && !tema.isEmpty() && currentStatus != StudyTimerService.TimerStatus.IDLE) {
                binding.etTema.setText(tema);
            }
        });

        viewModel.getSesionGuardadaExito().observe(getViewLifecycleOwner(), exito -> {
            if (Boolean.TRUE.equals(exito)) {
                Toast.makeText(requireContext(), "Sesión guardada en el historial.", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMensaje().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarDropdownMaterias() {
        if (getContext() == null) return;
        List<String> nombres = new ArrayList<>();
        for (Materia m : listaMaterias) {
            nombres.add(m.getNombre());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres);
        binding.actvMateria.setAdapter(adapter);

        if (materiaSeleccionada != null) {
            binding.actvMateria.setText(materiaSeleccionada.getNombre(), false);
        }
    }

    private void actualizarEstadoControles(StudyTimerService.TimerStatus status) {
        boolean enSesion = (status != StudyTimerService.TimerStatus.IDLE);

        binding.tilMateria.setEnabled(!enSesion);
        binding.tilTema.setEnabled(!enSesion);
        binding.toggleModo.setEnabled(!enSesion);
        binding.btnRegistroManual.setEnabled(!enSesion);

        if (status == StudyTimerService.TimerStatus.IDLE) {
            binding.btnIniciar.setVisibility(View.VISIBLE);
            binding.btnPausarReanudar.setVisibility(View.GONE);
            binding.btnFinalizar.setVisibility(View.GONE);
            binding.tvTimerDisplay.setText(esModoPomodoro ? "25:00" : "00:00");
            binding.tvTiempoEfectivoLabel.setText("Tiempo efectivo: 00:00");
        } else if (status == StudyTimerService.TimerStatus.RUNNING) {
            binding.btnIniciar.setVisibility(View.GONE);
            binding.btnPausarReanudar.setVisibility(View.VISIBLE);
            binding.btnPausarReanudar.setText("PAUSAR");
            binding.btnFinalizar.setVisibility(View.VISIBLE);
        } else if (status == StudyTimerService.TimerStatus.PAUSED) {
            binding.btnIniciar.setVisibility(View.GONE);
            binding.btnPausarReanudar.setVisibility(View.VISIBLE);
            binding.btnPausarReanudar.setText("REANUDAR");
            binding.btnFinalizar.setVisibility(View.VISIBLE);
        }
    }

    private void actualizarPomodoroBadge() {
        if (!esModoPomodoro) return;
        String texto;
        if (currentPhase == StudyTimerService.PomodoroPhase.TRABAJO) {
            texto = String.format(Locale.getDefault(), "Trabajo • Ciclo %d/4", currentCycle);
        } else if (currentPhase == StudyTimerService.PomodoroPhase.DESCANSO_CORTO) {
            texto = String.format(Locale.getDefault(), "Descanso Corto • Ciclo %d/4", currentCycle);
        } else {
            texto = "Descanso Largo • Ciclo 4/4";
        }
        binding.tvPomodoroBadge.setText(texto);
    }

    private void iniciarSesion() {
        if (getContext() == null) return;
        if (materiaSeleccionada == null) {
            String inputTexto = binding.actvMateria.getText().toString().trim();
            for (Materia m : listaMaterias) {
                if (m.getNombre().equalsIgnoreCase(inputTexto)) {
                    materiaSeleccionada = m;
                    break;
                }
            }
        }

        if (materiaSeleccionada == null) {
            binding.tilMateria.setError("Selecciona una materia para iniciar");
            return;
        }

        binding.tilMateria.setError(null);
        String tema = binding.etTema.getText() != null ? binding.etTema.getText().toString().trim() : "";

        if (esModoPomodoro) {
            viewModel.iniciarModoPomodoro(getContext(), materiaSeleccionada.getIdMateria(), materiaSeleccionada.getNombre(), tema);
        } else {
            viewModel.iniciarModoLibre(getContext(), materiaSeleccionada.getIdMateria(), materiaSeleccionada.getNombre(), tema);
        }
    }

    private void finalizarSesion() {
        if (getContext() == null) return;

        long materiaId = (materiaSeleccionada != null) ? materiaSeleccionada.getIdMateria() : 0;
        String materiaNombre = (materiaSeleccionada != null) ? materiaSeleccionada.getNombre() : "General";
        String tema = binding.etTema.getText() != null ? binding.etTema.getText().toString().trim() : "Sesión de estudio";
        long tiempoEfectivo = lastEffectiveSeconds;
        long duracionTotal = lastElapsedSeconds;
        String modalidad = esModoPomodoro ? "POMODORO" : "LIBRE";

        viewModel.detenerServicio(getContext());

        mostrarDialogFinalizar(materiaId, materiaNombre, tema, duracionTotal, tiempoEfectivo, modalidad);
    }

    private void mostrarDialogFinalizar(long materiaId, String materiaNombre, String tema,
                                        long duracionTotal, long tiempoEfectivo, String modalidad) {
        if (getContext() == null) return;

        DialogFinalizarSesionBinding dialogBinding = DialogFinalizarSesionBinding.inflate(getLayoutInflater());

        long minutosEfectivos = tiempoEfectivo / 60;
        String resumen = String.format(
                Locale.getDefault(),
                "Materia: %s\nTiempo efectivo: %d min (%s)\nModalidad: %s",
                materiaNombre,
                minutosEfectivos,
                formatearTiempo(tiempoEfectivo),
                modalidad
        );
        dialogBinding.tvResumenTiempoSesion.setText(resumen);

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    int satisfaccion = Math.round(dialogBinding.ratingBarConcentracion.getRating());
                    if (satisfaccion < 1) satisfaccion = 1;
                    if (satisfaccion > 5) satisfaccion = 5;

                    String notas = dialogBinding.etNotasReflexion.getText() != null
                            ? dialogBinding.etNotasReflexion.getText().toString().trim()
                            : "";

                    long fechaInicio = System.currentTimeMillis() - (duracionTotal * 1000L);
                    viewModel.guardarSesionFinalizada(
                            materiaId,
                            tema.isEmpty() ? materiaNombre : tema,
                            fechaInicio,
                            duracionTotal,
                            tiempoEfectivo,
                            modalidad,
                            satisfaccion,
                            notas
                    );
                })
                .setNegativeButton("Descartar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void mostrarDialogRegistroManual() {
        if (getContext() == null) return;

        DialogRegistroManualBinding dialogBinding = DialogRegistroManualBinding.inflate(getLayoutInflater());

        List<String> nombres = new ArrayList<>();
        for (Materia m : listaMaterias) {
            nombres.add(m.getNombre());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres);
        dialogBinding.actvManualMateria.setAdapter(adapter);

        final Materia[] materiaElegida = {null};
        dialogBinding.actvManualMateria.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaMaterias.size()) {
                materiaElegida[0] = listaMaterias.get(position);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Registrar", (dialog, which) -> {
                    if (materiaElegida[0] == null) {
                        String input = dialogBinding.actvManualMateria.getText().toString().trim();
                        for (Materia m : listaMaterias) {
                            if (m.getNombre().equalsIgnoreCase(input)) {
                                materiaElegida[0] = m;
                                break;
                            }
                        }
                    }

                    if (materiaElegida[0] == null) {
                        Toast.makeText(requireContext(), "Debes seleccionar una materia", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String duracionStr = dialogBinding.etManualDuracionMin.getText() != null
                            ? dialogBinding.etManualDuracionMin.getText().toString().trim()
                            : "";
                    if (duracionStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Ingresa los minutos estudiados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long minutos;
                    try {
                        minutos = Long.parseLong(duracionStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Duración inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (minutos <= 0) {
                        Toast.makeText(requireContext(), "La duración debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String tema = dialogBinding.etManualTema.getText() != null
                            ? dialogBinding.etManualTema.getText().toString().trim()
                            : "";
                    String notas = dialogBinding.etManualNotas.getText() != null
                            ? dialogBinding.etManualNotas.getText().toString().trim()
                            : "";
                    int satisfaccion = Math.round(dialogBinding.ratingBarManual.getRating());
                    if (satisfaccion < 1) satisfaccion = 1;

                    long inicio = System.currentTimeMillis() - (minutos * 60L * 1000L);
                    viewModel.registrarSesionManual(
                            materiaElegida[0].getIdMateria(),
                            tema.isEmpty() ? materiaElegida[0].getNombre() : tema,
                            inicio,
                            minutos,
                            satisfaccion,
                            notas
                    );
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String formatearTiempo(long totalSegundos) {
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;
        if (horas > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
