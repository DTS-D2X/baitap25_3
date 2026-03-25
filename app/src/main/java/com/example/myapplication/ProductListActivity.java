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
import com.example.myapplication.databinding.ActivityProductListBinding;
import com.example.myapplication.model.Product;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductListActivity extends AppCompatActivity {

    private ActivityProductListBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int categoryId = getIntent().getIntExtra("CATEGORY_ID", -1);

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));

        executorService.execute(() -> {
            List<Product> products;
            if (categoryId != -1) {
                products = AppDatabase.getDatabase(this).appDao().getProductsByCategory(categoryId);
            } else {
                products = AppDatabase.getDatabase(this).appDao().getAllProducts();
            }

            runOnUiThread(() -> {
                ProductAdapter adapter = new ProductAdapter(products);
                binding.rvProducts.setAdapter(adapter);
            });
        });
    }

    class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private List<Product> products;

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = products.get(position);
            holder.tvName.setText(product.name);
            holder.tvPrice.setText(String.format("%,.0f VNĐ", product.price));
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProductName);
                tvPrice = itemView.findViewById(R.id.tvProductPrice);
            }
        }
    }
}
