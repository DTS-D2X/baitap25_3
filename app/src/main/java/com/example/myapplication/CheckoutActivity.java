package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.databinding.ActivityCheckoutBinding;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.model.Product;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int orderId = getIntent().getIntExtra("ORDER_ID", -1);

        if (orderId != -1) {
            loadInvoice(orderId);
        }

        binding.btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadInvoice(int orderId) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            
            // 1. Cập nhật trạng thái Order thành "Paid"
            Order order = db.appDao().getOrderById(orderId);
            if (order != null) {
                order.status = "Paid";
                db.appDao().updateOrder(order);
            }

            // 2. Lấy chi tiết hóa đơn
            List<OrderDetail> details = db.appDao().getOrderDetails(orderId);
            double total = 0;

            final StringBuilder itemsText = new StringBuilder();
            for (OrderDetail detail : details) {
                Product product = db.appDao().getProductById(detail.productId);
                String productName = (product != null) ? product.name : "Sản phẩm #" + detail.productId;
                double subTotal = detail.price * detail.quantity;
                total += subTotal;
                
                itemsText.append(String.format(Locale.getDefault(), "%s x %d\n%,.0f VNĐ\n\n", 
                        productName, detail.quantity, subTotal));
            }

            final double finalTotal = total;
            runOnUiThread(() -> {
                binding.tvOrderId.setText("Mã đơn hàng: #" + orderId + " (ĐÃ THANH TOÁN)");
                
                TextView tvItems = new TextView(this);
                tvItems.setText(itemsText.toString());
                tvItems.setTextSize(16);
                binding.llOrderItems.addView(tvItems);

                binding.tvTotalAmount.setText(String.format(Locale.getDefault(), "Tổng cộng: %,.0f VNĐ", finalTotal));
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
