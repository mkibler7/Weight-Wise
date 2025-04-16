package com.example.project2_option3_michaelkibler;

public class WeighIn {
    private Long weighInID, userID;
    private String date;
    private double weight, difference;

    public WeighIn(Long weighInID, Long userID, String date, double weight, double difference) {
        this.weighInID = weighInID;
        this.userID = userID;
        this.date = date;
        this.weight = weight;
        this.difference = difference;
    }

    public String getDate() {
        return date;
    }

    public Long getWeighInID() {
        return weighInID;
    }

    public Long getUserID() {
        return userID;
    }

    public double getWeight() {
        return weight;
    }

    public double getDifference() {
        return difference;
    }
}