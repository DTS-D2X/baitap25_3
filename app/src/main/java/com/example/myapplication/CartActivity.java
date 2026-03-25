package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityCartBinding;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.model.Product;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Order pendingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        loadCart();

        binding.btnCheckout.setOnClickListener(v -> {
            if (pendingOrder != null) {
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("ORDER_ID", pendingOrder.id);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            pendingOrder = db.appDao().getPendingOrder(sessionManager.getUserId());

            if (pendingOrder != null) {
                List<OrderDetail> details = db.appDao().getOrderDetails(pendingOrder.id);
                List<CartItem> cartItems = new ArrayList<>();
                double total = 0;

                for (OrderDetail detail : details) {
                    Product product = db.appDao().getProductById(detail.productId);
                    if (product != null) {
                        cartItems.add(new CartItem(product.name, detail.price, detail.quantity));
                        total += (detail.price * detail.quantity);
                    }
                }

                final double finalTotal = total;
                runOnUiThread(() -> {
                    binding.rvCartItems.setAdapter(new CartAdapter(cartItems));
                    binding.tvCartTotal.setText(String.format(Locale.getDefault(), "Tổng cộng: %,.0f VNĐ", finalTotal));
                    binding.btnCheckout.setEnabled(!cartItems.isEmpty());
                });
            } else {
                runOnUiThread(() -> {
                    binding.tvCartTotal.setText("Tổng cộng: 0 VNĐ");
                    binding.btnCheckout.setEnabled(false);
                });
            }
        });
    }

    static class CartItem {
        String name;
        double price;
        int quantity;

        CartItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        private List<CartItem> items;

        CartAdapter(List<CartItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", item.price));
            holder.tvQuantity.setText("x" + item.quantity);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvQuantity;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCartItemName);
                tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
                tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            }
        }
    }
}
