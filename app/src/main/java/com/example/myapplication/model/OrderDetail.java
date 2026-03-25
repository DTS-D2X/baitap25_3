package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "order_details")
public class OrderDetail {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int orderId;
    public int productId;
    public int quantity;
    public double price;

    public OrderDetail(int orderId, int productId, int quantity, double price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}
