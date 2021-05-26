package main.ServiceFirst;

public class Ship {

    private int day;
    private int time;
    private String name;
    private cargoType type;
    private int weight;
    private double waitTime;

    public Ship(int day, int time, String name,
                cargoType type, int weight, double waitTime) {
        this.day = day;
        this.time = time;
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.waitTime = waitTime;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public cargoType getType() {
        return type;
    }

    public void setType(cargoType type) {
        this.type = type;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public double getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(double waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "day=" + day +
                ", time=" + time +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", weight=" + weight +
                ", waitTime=" + waitTime +
                '}';
    }
}
