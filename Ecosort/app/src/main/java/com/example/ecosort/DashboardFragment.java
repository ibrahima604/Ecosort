package com.example.ecosort;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        setupToolbar(view);
        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            ((AdminActivity) requireActivity()).showAdminMenu();
        });
    }
}