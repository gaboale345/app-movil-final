package com.estudio.studyflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.estudio.studyflow.databinding.ActivityMainBinding;
import com.estudio.studyflow.ui.dashboard.DashboardFragment;
import com.estudio.studyflow.ui.materias.MateriasFragment;
import com.estudio.studyflow.ui.metas.MetasFragment;
import com.estudio.studyflow.ui.reportes.ReportesFragment;
import com.estudio.studyflow.ui.timer.TimerFragment;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 1001;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        solicitarPermisosNotificacion();
        configurarNavegacion(savedInstanceState);
    }

    private void solicitarPermisosNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_NOTIFICATIONS
                );
            }
        }
    }

    private void configurarNavegacion(Bundle savedInstanceState) {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_timer) {
                selectedFragment = new TimerFragment();
            } else if (itemId == R.id.nav_materias) {
                selectedFragment = new MateriasFragment();
            } else if (itemId == R.id.nav_metas) {
                selectedFragment = new MetasFragment();
            } else if (itemId == R.id.nav_reportes) {
                selectedFragment = new ReportesFragment();
            }

            if (selectedFragment != null) {
                cargarFragment(selectedFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToTimer() {
        if (binding != null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_timer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
