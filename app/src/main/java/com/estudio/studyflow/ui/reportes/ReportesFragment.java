package com.estudio.studyflow.ui.reportes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.estudio.studyflow.databinding.FragmentReportesBinding;
import com.estudio.studyflow.domain.usecase.ObtenerEstadisticasUseCase;
import com.estudio.studyflow.ui.dashboard.SesionesAdapter;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportesFragment extends Fragment {

    private FragmentReportesBinding binding;
    private ReportesViewModel viewModel;
    private SesionesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportesViewModel.class);

        configurarRecyclerView();
        configurarBotones();
        observarViewModel();
    }

    private void configurarRecyclerView() {
        adapter = new SesionesAdapter();
        binding.rvHistorialReportes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistorialReportes.setAdapter(adapter);
    }

    private void configurarBotones() {
        binding.btnExportarCsv.setOnClickListener(v -> viewModel.exportarCSV());
        binding.btnExportarTxt.setOnClickListener(v -> viewModel.exportarTXT());
    }

    private void observarViewModel() {
        viewModel.getResumenLiveData().observe(getViewLifecycleOwner(), resumen -> {
            if (resumen != null) {
                actualizarMetricas(resumen);
                configurarGraficoBarras(resumen);
                configurarGraficoCircular(resumen);
            }
        });

        viewModel.getHistorialSesiones().observe(getViewLifecycleOwner(), sesiones -> {
            if (sesiones != null) {
                adapter.submitList(sesiones);
            }
        });

        viewModel.getReporteExportadoLiveData().observe(getViewLifecycleOwner(), contenido -> {
            if (contenido != null && !contenido.isEmpty()) {
                compartirReporte(contenido);
            }
        });
    }

    private void actualizarMetricas(ObtenerEstadisticasUseCase.ResumenEstadistico resumen) {
        float promedioHoras = resumen.promedioDiarioMinutos / 60f;
        binding.tvPromedioDiario.setText(String.format(Locale.getDefault(), "%.1f hrs", promedioHoras));

        String topMat = (resumen.materiaTopNombre != null && !resumen.materiaTopNombre.isEmpty())
                ? resumen.materiaTopNombre
                : "Sin actividad";
        binding.tvMateriaTop.setText(topMat);
    }

    private void configurarGraficoBarras(ObtenerEstadisticasUseCase.ResumenEstadistico resumen) {
        String[] dias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        List<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < dias.length; i++) {
            int mins = (resumen.minutosPorDia != null) ? resumen.minutosPorDia.getOrDefault(dias[i], 0) : 0;
            float horas = mins / 60f;
            barEntries.add(new BarEntry(i, horas));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Horas por Día");
        barDataSet.setColor(Color.parseColor("#8E24AA"));
        barDataSet.setValueTextSize(11f);
        barDataSet.setValueTextColor(Color.parseColor("#4A148C"));

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.55f);
        binding.chartBarrasDias.setData(barData);

        XAxis xAxis = binding.chartBarrasDias.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dias));
        xAxis.setDrawGridLines(false);

        binding.chartBarrasDias.getAxisRight().setEnabled(false);
        binding.chartBarrasDias.getDescription().setEnabled(false);
        binding.chartBarrasDias.animateY(800);
        binding.chartBarrasDias.invalidate();
    }

    private void configurarGraficoCircular(ObtenerEstadisticasUseCase.ResumenEstadistico resumen) {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int[] defaultColors = {
                Color.parseColor("#8E24AA"),
                Color.parseColor("#1E88E5"),
                Color.parseColor("#43A047"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#FB8C00"),
                Color.parseColor("#00ACC1")
        };
        int defaultColorIdx = 0;

        if (resumen.minutosPorMateria != null && !resumen.minutosPorMateria.isEmpty()) {
            for (Map.Entry<String, Integer> entry : resumen.minutosPorMateria.entrySet()) {
                if (entry.getValue() > 0) {
                    float horas = entry.getValue() / 60f;
                    pieEntries.add(new PieEntry(horas, entry.getKey()));

                    String hex = (resumen.coloresPorMateria != null) ? resumen.coloresPorMateria.get(entry.getKey()) : null;
                    if (hex != null) {
                        try {
                            colors.add(Color.parseColor(hex));
                        } catch (Exception e) {
                            colors.add(defaultColors[defaultColorIdx % defaultColors.length]);
                            defaultColorIdx++;
                        }
                    } else {
                        colors.add(defaultColors[defaultColorIdx % defaultColors.length]);
                        defaultColorIdx++;
                    }
                }
            }
        }

        if (pieEntries.isEmpty()) {
            pieEntries.add(new PieEntry(1f, "Sin datos aún"));
            colors.add(Color.parseColor("#BDBDBD"));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(pieDataSet);
        binding.chartCircularMaterias.setData(pieData);
        binding.chartCircularMaterias.getDescription().setEnabled(false);
        binding.chartCircularMaterias.setCenterText("Distribución");
        binding.chartCircularMaterias.setCenterTextSize(13f);
        binding.chartCircularMaterias.setHoleRadius(42f);
        binding.chartCircularMaterias.animateY(800);
        binding.chartCircularMaterias.invalidate();
    }

    private void compartirReporte(String contenido) {
        if (getContext() == null) return;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, contenido);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Exportar Reporte StudyFlow");
        startActivity(shareIntent);
        Toast.makeText(requireContext(), "Reporte generado con éxito", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
