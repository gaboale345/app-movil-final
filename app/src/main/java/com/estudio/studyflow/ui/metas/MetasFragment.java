package com.estudio.studyflow.ui.metas;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.estudio.studyflow.R;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.data.local.entity.MetaSemanal;
import com.estudio.studyflow.databinding.DialogFijarMetaBinding;
import com.estudio.studyflow.databinding.FragmentMetasBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MetasFragment extends Fragment {

    private FragmentMetasBinding binding;
    private MetasViewModel viewModel;
    private MetasAdapter adapter;

    private List<Materia> listaMaterias = new ArrayList<>();
    private final Map<Long, String> mapaMaterias = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MetasViewModel.class);

        configurarRecyclerView();
        configurarBotones();
        observarViewModel();
    }

    private void configurarRecyclerView() {
        adapter = new MetasAdapter();
        binding.rvMetasMaterias.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMetasMaterias.setAdapter(adapter);
    }

    private void configurarBotones() {
        binding.btnAjustarMetaGlobal.setOnClickListener(v -> mostrarDialogMetaGlobal());
        binding.fabNuevaMetaMateria.setOnClickListener(v -> mostrarDialogMetaMateria());
    }

    private void observarViewModel() {
        viewModel.getMateriasActivas().observe(getViewLifecycleOwner(), materias -> {
            listaMaterias = materias != null ? materias : new ArrayList<>();
            mapaMaterias.clear();
            for (Materia m : listaMaterias) {
                mapaMaterias.put(m.getIdMateria(), m.getNombre());
            }
            adapter.setNombresMaterias(mapaMaterias);
        });

        viewModel.getMetaGlobalSemanaActual().observe(getViewLifecycleOwner(), metaGlobal -> {
            if (metaGlobal != null) {
                actualizarCardMetaGlobal(metaGlobal);
            } else {
                binding.tvMetaGlobalHoras.setText("0.0h de 0.0h objetivo");
                binding.tvMetaGlobalPorcentajeNum.setText("0%");
                binding.progressMetaGlobalScreen.setProgress(0);
                binding.tvMetaGlobalBadge.setText("SIN META");
            }
        });

        viewModel.getMetasSemanaActual().observe(getViewLifecycleOwner(), metas -> {
            List<MetaSemanal> metasPorMateria = new ArrayList<>();
            if (metas != null) {
                for (MetaSemanal meta : metas) {
                    if (meta.getIdMateria() != null) {
                        metasPorMateria.add(meta);
                    }
                }
            }

            if (!metasPorMateria.isEmpty()) {
                adapter.submitList(metasPorMateria);
                binding.rvMetasMaterias.setVisibility(View.VISIBLE);
                binding.tvSinMetas.setVisibility(View.GONE);
            } else {
                adapter.submitList(null);
                binding.rvMetasMaterias.setVisibility(View.GONE);
                binding.tvSinMetas.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getFeedbackMensaje().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarCardMetaGlobal(MetaSemanal meta) {
        float horasObj = meta.getMinutosObjetivo() / 60f;
        float horasAlc = meta.getMinutosAlcanzados() / 60f;
        int porcentaje = (int) meta.getPorcentajeAvance();

        binding.tvMetaGlobalHoras.setText(
                String.format(Locale.getDefault(), "%.1fh alcanzadas de %.1fh objetivo", horasAlc, horasObj)
        );
        binding.tvMetaGlobalPorcentajeNum.setText(porcentaje + "%");
        binding.progressMetaGlobalScreen.setProgress(Math.min(100, porcentaje));

        int color;
        int bgColor;
        String estadoTexto;

        switch (meta.getEstado()) {
            case "CUMPLIDA":
                color = Color.parseColor("#2E7D32");
                bgColor = Color.parseColor("#E8F5E9");
                estadoTexto = "CUMPLIDA";
                break;
            case "EN_RIESGO":
                color = Color.parseColor("#C62828");
                bgColor = Color.parseColor("#FFEBEE");
                estadoTexto = "EN RIESGO";
                break;
            case "EN_PROGRESO":
            default:
                color = Color.parseColor("#F57C00");
                bgColor = Color.parseColor("#FFF3E0");
                estadoTexto = "EN PROGRESO";
                break;
        }

        binding.tvMetaGlobalBadge.setText(estadoTexto);
        binding.tvMetaGlobalBadge.setTextColor(color);
        binding.tvMetaGlobalBadge.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        binding.progressMetaGlobalScreen.setIndicatorColor(color);
    }

    private void mostrarDialogMetaGlobal() {
        if (getContext() == null) return;

        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Ej. 20 horas");

        new AlertDialog.Builder(requireContext())
                .setTitle("Fijar Meta Global Semanal")
                .setMessage("Ingresa la cantidad total de horas que planeas estudiar esta semana:")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String texto = input.getText().toString().trim();
                    if (!texto.isEmpty()) {
                        try {
                            int horas = Integer.parseInt(texto);
                            viewModel.fijarMetaGlobal(horas);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Número inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogMetaMateria() {
        if (getContext() == null) return;
        if (listaMaterias.isEmpty()) {
            Toast.makeText(requireContext(), "Primero debes registrar al menos una materia.", Toast.LENGTH_LONG).show();
            return;
        }

        DialogFijarMetaBinding dialogBinding = DialogFijarMetaBinding.inflate(getLayoutInflater());
        dialogBinding.tvDialogMetaTitulo.setText("Fijar Meta por Asignatura");

        List<String> nombres = new ArrayList<>();
        for (Materia m : listaMaterias) {
            nombres.add(m.getNombre());
        }
        ArrayAdapter<String> adapterMaterias = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres);
        dialogBinding.actvMetaMateria.setAdapter(adapterMaterias);

        final Materia[] seleccionada = {null};
        dialogBinding.actvMetaMateria.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaMaterias.size()) {
                seleccionada[0] = listaMaterias.get(position);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (seleccionada[0] == null) {
                        String texto = dialogBinding.actvMetaMateria.getText().toString().trim();
                        for (Materia m : listaMaterias) {
                            if (m.getNombre().equalsIgnoreCase(texto)) {
                                seleccionada[0] = m;
                                break;
                            }
                        }
                    }

                    if (seleccionada[0] == null) {
                        Toast.makeText(requireContext(), "Selecciona una materia", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String horasStr = dialogBinding.etHorasObjetivo.getText() != null
                            ? dialogBinding.etHorasObjetivo.getText().toString().trim()
                            : "";
                    if (horasStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Ingresa las horas objetivo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int horas = Integer.parseInt(horasStr);
                        viewModel.fijarMetaPorMateria(seleccionada[0].getIdMateria(), horas);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Número de horas inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
