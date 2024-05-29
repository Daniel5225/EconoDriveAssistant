package by.econodrive.econodriveassistant;

public class FuelEntry {
    private String id;
    private String date;
    private double volume;
    private double price;
    private String userId;
    private int mileage;

    public FuelEntry() {
        // Конструктор по умолчанию необходим для DataSnapshot.getValue(FuelEntry.class)
    }

    public FuelEntry(String id, String date, double volume, double price, int mileage, String userId) {
        this.id = id;
        this.date = date;
        this.volume = volume;
        this.price = price;
        this.mileage = mileage; // Инициализация пробега
        this.userId = userId;
    }

    // Getters and setters for all fields
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

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
