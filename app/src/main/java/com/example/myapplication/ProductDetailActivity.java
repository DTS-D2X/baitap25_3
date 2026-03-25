package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityProductDetailBinding;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.model.Product;
import com.example.myapplication.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);

        executorService.execute(() -> {
            currentProduct = AppDatabase.getDatabase(this).appDao().getProductById(productId);
            if (currentProduct != null) {
                runOnUiThread(() -> {
                    binding.tvDetailName.setText(currentProduct.name);
                    binding.tvDetailPrice.setText(String.format("%,.0f VNĐ", currentProduct.price));
                    binding.tvDetailDescription.setText(currentProduct.description);
                });
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return;
            }
            addToCart();
        });
    }

    private void addToCart() {
        executorService.execute(() -> {
            int userId = sessionManager.getUserId();
            AppDatabase db = AppDatabase.getDatabase(this);
            
            // 1. Kiểm tra Order hiện tại (Pending)
            Order pendingOrder = db.appDao().getPendingOrder(userId);
            long orderId;
            if (pendingOrder == null) {
                Order newOrder = new Order(userId, "Pending");
                orderId = db.appDao().createOrder(newOrder);
            } else {
                orderId = pendingOrder.id;
            }

            // 2. Thêm vào OrderDetails
            OrderDetail detail = new OrderDetail((int) orderId, currentProduct.id, 1, currentProduct.price);
            db.appDao().insertOrderDetail(detail);

            runOnUiThread(() -> {
                showContinueShoppingDialog((int) orderId);
            });
        });
    }

    private void showContinueShoppingDialog(int orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Đã thêm sản phẩm vào giỏ hàng. Bạn có muốn tiếp tục mua sắm không?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    Intent intent = new Intent(this, ProductListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .setNegativeButton("Thanh toán", (dialog, which) -> {
                    Intent intent = new Intent(this, CheckoutActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                })
                .show();
    }
}
