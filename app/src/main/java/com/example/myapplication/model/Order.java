package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String status; // "Pending" or "Paid"

    public Order(int userId, String status) {
        this.userId = userId;
        this.status = status;
    }
}
