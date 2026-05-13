package com.example.smartmedicinemanager;

public class Medicine {
    private int id;
    private String name;
    private String dosage;
    private String time;
    private int pillCount;
    private String expiryDate;

    public Medicine(int id, String name, String dosage, String time, int pillCount, String expiryDate) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.pillCount = pillCount;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
    public int getPillCount() { return pillCount; }
    public String getExpiryDate() { return expiryDate; }

    public void setId(int id) { this.id = id; }
}