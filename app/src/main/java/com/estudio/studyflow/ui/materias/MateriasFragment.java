package com.estudio.studyflow.ui.materias;

import android.app.AlertDialog;
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

import com.estudio.studyflow.R;
import com.estudio.studyflow.data.local.entity.Materia;
import com.estudio.studyflow.databinding.DialogMateriaBinding;
import com.estudio.studyflow.databinding.FragmentMateriasBinding;

public class MateriasFragment extends Fragment implements MateriasAdapter.OnMateriaActionListener {

    private FragmentMateriasBinding binding;
    private MateriasViewModel viewModel;
    private MateriasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMateriasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MateriasViewModel.class);

        configurarRecyclerView();
        configurarFAB();
        observarViewModel();
    }

    private void configurarRecyclerView() {
        adapter = new MateriasAdapter(this);
        binding.rvMaterias.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMaterias.setAdapter(adapter);
    }

    private void configurarFAB() {
        binding.fabAgregarMateria.setOnClickListener(v -> mostrarDialogMateria(null));
    }

    private void observarViewModel() {
        viewModel.getTodasLasMaterias().observe(getViewLifecycleOwner(), materias -> {
            if (materias != null && !materias.isEmpty()) {
                adapter.submitList(materias);
                binding.rvMaterias.setVisibility(View.VISIBLE);
                binding.tvSinMaterias.setVisibility(View.GONE);
            } else {
                adapter.submitList(null);
                binding.rvMaterias.setVisibility(View.GONE);
                binding.tvSinMaterias.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMensajeFeedback().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditar(Materia materia) {
        mostrarDialogMateria(materia);
    }

    @Override
    public void onToggleActiva(Materia materia) {
        if (materia.isEstaActiva()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Archivar Materia")
                    .setMessage("¿Deseas archivar la materia \"" + materia.getNombre() + "\"? No aparecerá en las opciones de estudio activas.")
                    .setPositiveButton("Archivar", (d, w) -> viewModel.archivarMateria(materia.getIdMateria()))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            viewModel.reactivarMateria(materia.getIdMateria());
        }
    }

    @Override
    public void onEliminar(Materia materia) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Materia")
                .setMessage("¿Estás seguro de eliminar \"" + materia.getNombre() + "\"? Esto eliminará también todas sus sesiones asociadas permanentemente.")
                .setPositiveButton("Eliminar", (d, w) -> viewModel.eliminarMateria(materia))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogMateria(@Nullable Materia materiaAEditar) {
        if (getContext() == null) return;

        DialogMateriaBinding dialogBinding = DialogMateriaBinding.inflate(getLayoutInflater());
        boolean esEdicion = (materiaAEditar != null);

        if (esEdicion) {
            dialogBinding.tvDialogMateriaTitulo.setText("Editar Materia");
            dialogBinding.etNombreMateria.setText(materiaAEditar.getNombre());
            dialogBinding.etCodigoMateria.setText(materiaAEditar.getCodigo());
            dialogBinding.etCreditosMateria.setText(materiaAEditar.getCreditos() > 0 ? String.valueOf(materiaAEditar.getCreditos()) : "");
            dialogBinding.etDocenteMateria.setText(materiaAEditar.getDocente());

            String color = materiaAEditar.getColorHex();
            if ("#E91E63".equalsIgnoreCase(color)) {
                dialogBinding.rgColores.check(R.id.rb_color_rosa);
            } else if ("#4CAF50".equalsIgnoreCase(color)) {
                dialogBinding.rgColores.check(R.id.rb_color_verde);
            } else if ("#9C27B0".equalsIgnoreCase(color)) {
                dialogBinding.rgColores.check(R.id.rb_color_morado);
            } else {
                dialogBinding.rgColores.check(R.id.rb_color_azul);
            }
        } else {
            dialogBinding.tvDialogMateriaTitulo.setText("Registrar Nueva Materia");
            dialogBinding.rgColores.check(R.id.rb_color_azul);
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = dialogBinding.etNombreMateria.getText() != null
                            ? dialogBinding.etNombreMateria.getText().toString().trim()
                            : "";

                    if (nombre.isEmpty()) {
                        Toast.makeText(requireContext(), "El nombre de la materia es obligatorio.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String codigo = dialogBinding.etCodigoMateria.getText() != null
                            ? dialogBinding.etCodigoMateria.getText().toString().trim()
                            : "";
                    String creditosStr = dialogBinding.etCreditosMateria.getText() != null
                            ? dialogBinding.etCreditosMateria.getText().toString().trim()
                            : "";
                    int creditos = 0;
                    if (!creditosStr.isEmpty()) {
                        try {
                            creditos = Integer.parseInt(creditosStr);
                        } catch (NumberFormatException ignored) {}
                    }

                    String docente = dialogBinding.etDocenteMateria.getText() != null
                            ? dialogBinding.etDocenteMateria.getText().toString().trim()
                            : "";

                    String colorHex = "#2196F3";
                    int checkedColorId = dialogBinding.rgColores.getCheckedRadioButtonId();
                    if (checkedColorId == R.id.rb_color_rosa) {
                        colorHex = "#E91E63";
                    } else if (checkedColorId == R.id.rb_color_verde) {
                        colorHex = "#4CAF50";
                    } else if (checkedColorId == R.id.rb_color_morado) {
                        colorHex = "#9C27B0";
                    }

                    if (esEdicion) {
                        materiaAEditar.setNombre(nombre);
                        materiaAEditar.setCodigo(codigo);
                        materiaAEditar.setCreditos(creditos);
                        materiaAEditar.setDocente(docente);
                        materiaAEditar.setColorHex(colorHex);
                        viewModel.actualizarMateria(materiaAEditar);
                    } else {
                        viewModel.registrarMateria(nombre, codigo, colorHex, creditos, docente);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
