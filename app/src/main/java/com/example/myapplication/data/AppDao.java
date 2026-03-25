package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.Category;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.model.Product;
import com.example.myapplication.model.User;

import java.util.List;

@Dao
public interface AppDao {
    // Auth
    @Query("SELECT * FROM users WHERE username = :user AND password = :pass LIMIT 1")
    User login(String user, String pass);

    @Insert
    void insertUser(User user);

    // Categories & Products
    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE categoryId = :catId")
    List<Product> getProductsByCategory(int catId);

    @Query("SELECT * FROM products WHERE id = :prodId")
    Product getProductById(int prodId);

    // Orders
    @Query("SELECT * FROM orders WHERE userId = :uId AND status = 'Pending' LIMIT 1")
    Order getPendingOrder(int uId);
    
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    Order getOrderById(int orderId);

    @Insert
    long createOrder(Order order);

    @Update
    void updateOrder(Order order);

    @Insert
    void insertOrderDetail(OrderDetail detail);

    @Query("SELECT * FROM order_details WHERE orderId = :oId")
    List<OrderDetail> getOrderDetails(int oId);
    
    // Initial Data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInitialCategories(List<Category> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInitialProducts(List<Product> products);
}
