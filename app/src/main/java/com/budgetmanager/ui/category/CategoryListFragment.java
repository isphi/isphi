package com.budgetmanager.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.adapter.CategoryAdapter;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.ui.article.ArticleListActivity;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.Executors;

public class CategoryListFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private BudgetDatabase database;
    private SessionManager sessionManager;
    private String currentType = Category.TYPE_EXPENSE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = BudgetDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CategoryAdapter(this);
        rvCategories.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_type);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? Category.TYPE_EXPENSE : Category.TYPE_REVENUE;
                loadCategories();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_category);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditCategoryActivity.class);
            intent.putExtra("type", currentType);
            startActivity(intent);
        });

        loadCategories();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        long userId = sessionManager.getUserId();
        database.categoryDao().getByUserAndType(userId, currentType)
                .observe(getViewLifecycleOwner(), categories -> adapter.setCategories(categories));
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(requireContext(), ArticleListActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        intent.putExtra("category_type", category.getType());
        startActivity(intent);
    }

    @Override
    public void onCategoryEdit(Category category) {
        Intent intent = new Intent(requireContext(), AddEditCategoryActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("type", category.getType());
        startActivity(intent);
    }

    @Override
    public void onCategoryDelete(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la catégorie")
                .setMessage("Voulez-vous vraiment supprimer \"" + category.getName() + "\" et tous ses articles ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() ->
                            database.categoryDao().delete(category));
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
