package by.econodrive.econodriveassistant;

import java.util.UUID;

public class OtherEntry {
    private String id;
    private String date;
    private String expenseType;
    private double price;
    private String userId;
    private int mileage;

    public OtherEntry() {
        // Пустой конструктор нужен для Firebase
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public OtherEntry(String id, String date, String expenseType, double price, int mileage, String userId) {
        this.id = id;
        this.date = date;
        this.expenseType = expenseType;
        this.price = price;
        this.userId = userId;
        this.mileage = mileage;
    }

    // Геттеры и сеттеры для каждого поля
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

