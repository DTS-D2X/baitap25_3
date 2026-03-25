package com.example.myapplication.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication.model.Category;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.model.Product;
import com.example.myapplication.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Category.class, Product.class, Order.class, OrderDetail.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppDao appDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "shopping_db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                AppDao dao = INSTANCE.appDao();

                // Dữ liệu mẫu
                dao.insertUser(new User("admin", "123"));

                List<Category> categories = new ArrayList<>();
                categories.add(new Category("Điện thoại"));
                categories.add(new Category("Laptop"));
                dao.insertInitialCategories(categories);

                List<Product> products = new ArrayList<>();
                products.add(new Product("iPhone 15", 20000000.0, "Apple Smartphone", 1));
                products.add(new Product("Samsung S23", 18000000.0, "Samsung Smartphone", 1));
                products.add(new Product("MacBook M2", 30000000.0, "Apple Laptop", 2));
                products.add(new Product("Dell XPS", 25000000.0, "Dell Laptop", 2));
                dao.insertInitialProducts(products);
            });
        }
    };
}
