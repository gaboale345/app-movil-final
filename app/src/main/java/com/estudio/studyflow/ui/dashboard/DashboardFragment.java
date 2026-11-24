package com.estudio.studyflow.ui.dashboard;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.estudio.studyflow.MainActivity;
import com.estudio.studyflow.databinding.FragmentDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private SesionesAdapter sesionesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Fecha actual formateada
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaTexto = capitalize(sdf.format(new Date()));
        binding.tvFechaActual.setText(fechaTexto);

        // Setup RecyclerView
        sesionesAdapter = new SesionesAdapter();
        binding.rvSesionesRecientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSesionesRecientes.setAdapter(sesionesAdapter);

        // Botón acción rápida a cronómetro
        binding.btnAccionRapidaTimer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTimer();
            }
        });

        observarDatos();
    }

    private void observarDatos() {
        // Horas Hoy
        viewModel.getMinutosEstudiadosHoy().observe(getViewLifecycleOwner(), minutos -> {
            int mins = minutos != null ? minutos : 0;
            binding.tvHorasHoy.setText(formatearHorasMinutos(mins));
        });

        // Horas Semana
        viewModel.getMinutosEstudiadosSemana().observe(getViewLifecycleOwner(), minutos -> {
            int mins = minutos != null ? minutos : 0;
            binding.tvHorasSemana.setText(formatearHorasMinutos(mins));
        });

        // Racha de Días
        viewModel.getRachaDias().observe(getViewLifecycleOwner(), racha -> {
            int r = racha != null ? racha : 0;
            binding.tvRachaDias.setText(r == 1 ? "1 día" : r + " días");
        });

        // Meta Global
        viewModel.getMetaGlobalSemanaActual().observe(getViewLifecycleOwner(), meta -> {
            if (meta != null && meta.getMinutosObjetivo() > 0) {
                int porcentaje = (int) meta.getPorcentajeAvance();
                binding.progressMetaGlobal.setProgress(Math.min(100, porcentaje));
                binding.tvMetaGlobalPorcentaje.setText(porcentaje + "%");

                float horasAlc = meta.getMinutosAlcanzados() / 60f;
                float horasObj = meta.getMinutosObjetivo() / 60f;
                binding.tvMetaGlobalTiempo.setText(String.format(Locale.getDefault(), "%.1fh alcanzadas de %.1fh", horasAlc, horasObj));

                int color;
                int bgColor;
                if ("CUMPLIDA".equals(meta.getEstado())) {
                    color = Color.parseColor("#2E7D32");
                    bgColor = Color.parseColor("#E8F5E9");
                } else if ("EN_RIESGO".equals(meta.getEstado())) {
                    color = Color.parseColor("#C62828");
                    bgColor = Color.parseColor("#FFEBEE");
                } else {
                    color = Color.parseColor("#F57C00");
                    bgColor = Color.parseColor("#FFF3E0");
                }

                binding.tvMetaEstadoBadge.setText(meta.getEstado().replace("_", " "));
                binding.tvMetaEstadoBadge.setTextColor(color);
                binding.tvMetaEstadoBadge.setBackgroundTintList(ColorStateList.valueOf(bgColor));
                binding.progressMetaGlobal.setIndicatorColor(color);
            } else {
                binding.progressMetaGlobal.setProgress(0);
                binding.tvMetaGlobalPorcentaje.setText("0%");
                binding.tvMetaGlobalTiempo.setText("Sin meta fijada para esta semana");
                binding.tvMetaEstadoBadge.setText("PENDIENTE");
            }
        });

        // Próxima Sesión Planificada
        viewModel.getProximaSesion().observe(getViewLifecycleOwner(), sesion -> {
            if (sesion != null) {
                binding.cardProximaSesion.setVisibility(View.VISIBLE);
                SimpleDateFormat horaFormat = new SimpleDateFormat("EEE dd, HH:mm", Locale.getDefault());
                String fechaHora = horaFormat.format(new Date(sesion.fechaInicio));
                binding.tvProximaSesionInfo.setText(sesion.nombreMateria + " • " + fechaHora + " (" + (sesion.duracionSegundos / 60) + " min)");
            } else {
                binding.cardProximaSesion.setVisibility(View.GONE);
            }
        });

        // Sesiones Recientes
        viewModel.getSesionesRecientes().observe(getViewLifecycleOwner(), sesiones -> {
            if (sesiones != null && !sesiones.isEmpty()) {
                sesionesAdapter.submitList(sesiones);
                binding.rvSesionesRecientes.setVisibility(View.VISIBLE);
                binding.tvSinSesiones.setVisibility(View.GONE);
            } else {
                binding.rvSesionesRecientes.setVisibility(View.GONE);
                binding.tvSinSesiones.setVisibility(View.VISIBLE);
            }
        });
    }

    private String formatearHorasMinutos(int totalMinutos) {
        if (totalMinutos < 60) {
            return totalMinutos + "m";
        }
        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return horas + "h " + minutos + "m";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarRacha();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
