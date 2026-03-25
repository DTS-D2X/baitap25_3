package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityCategoryListBinding;
import com.example.myapplication.model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryListActivity extends AppCompatActivity {

    private ActivityCategoryListBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));

        executorService.execute(() -> {
            List<Category> categories = AppDatabase.getDatabase(this).appDao().getAllCategories();
            runOnUiThread(() -> {
                CategoryAdapter adapter = new CategoryAdapter(categories);
                binding.rvCategories.setAdapter(adapter);
            });
        });
    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private List<Category> categories;

        public CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.tvName.setText(category.name);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CategoryListActivity.this, ProductListActivity.class);
                intent.putExtra("CATEGORY_ID", category.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCategoryName);
            }
        }
    }
}
