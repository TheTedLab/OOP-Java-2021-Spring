package main.ServiceThird;

import java.util.concurrent.atomic.AtomicInteger;

public class ArrivedShip {
    private int day;
    private int time;
    private int month;
    private String name;
    private String type;
    private AtomicInteger weight;
    private double waitTime;
    private int delay = 0;
    private int unloadTime = 0;
    private String unloadStartDay;
    private String unloadStartTime;
    private volatile boolean isCame = false;

    public ArrivedShip(int day, int time, int month, String name,
                       String type, AtomicInteger weight, double waitTime) {
        this.day = day;
        this.time = time;
        this.month = month;
        this.name = name;
        this.type = type;
        this.weight = weight;
        this.waitTime = waitTime;
        this.unloadStartDay = "NO DAY";
        this.unloadStartTime = "NO TIME";
    }

    public void processed() {
        this.weight.getAndSet(0);
    }

    @Override
    public String toString() {
        return "ArrivedShip{" +
                "day=" + day +
                ", time=" + time +
                ", month=" + month +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", weight=" + weight +
                ", waitTime=" + waitTime +
                ", delay=" + delay +
                ", unloadTime=" + unloadTime +
                ", unloadStartDay='" + unloadStartDay + '\'' +
                ", unloadStartTime='" + unloadStartTime + '\'' +
                ", isCame=" + isCame +
                '}';
    }

    public int getIntTime() {
        return time;
    }

    public int getIntDay() {
        return day;
    }

    public int getIntMonth() {
        return 3;
    }

    public int getDay() {
        return day;
    }

    public int getTime() {
        return time;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeight() {
        return weight.getAndAdd(0);
    }

    public void setWeight(int weight) {
        this.weight.getAndSet(weight);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void resetWaitTime(int craneProductivity) {
        this.waitTime = (double) this.weight.intValue() / (double) craneProductivity;
    }

    public String getType() {
        return type;
    }

    public boolean isCame() {
        return isCame;
    }

    public void setCame(boolean came) {
        isCame = came;
    }

    public String getName() {
        return name;
    }

    public double getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(double waitTime) {
        this.waitTime = waitTime;
    }

    public String getUnloadStart() {
        return unloadStartDay + " " + unloadStartTime;
    }

    public void setUnloadStart(int unloadStartDay, int unloadStartTime) {
        if (unloadStartDay < 10) {
            this.unloadStartDay = "2021-03-0" + unloadStartDay;
        } else {
            this.unloadStartDay = "2021-03-" + unloadStartDay;
        }
        if (unloadStartTime < 60) {
            if (unloadStartTime < 10) {
                this.unloadStartTime = "00:0" + unloadStartTime;
            } else {
                this.unloadStartTime = "00:" + unloadStartTime;
            }
        } else {
            int hours = unloadStartTime / 60;
            int minutes = unloadStartTime - hours * 60;
            if (hours < 10 && minutes < 10) {
                this.unloadStartTime = "0" + hours + ":0"
                        + minutes;
            } else if (hours < 10) {
                this.unloadStartTime = "0" + hours + ":"
                        + minutes;
            } else if (minutes < 10) {
                this.unloadStartTime = hours + ":0"
                        + minutes;
            } else {
                this.unloadStartTime = hours + ":"
                        + minutes;
            }
        }
    }

    public int getIntUnloadStartTime() {
        int day = Integer.parseInt(unloadStartDay.substring(8, 10));
        int hours = Integer.parseInt(unloadStartTime.substring(0, 2));
        int min = Integer.parseInt(unloadStartTime.substring(3, 5));
        return day * 1440 + hours * 60 + min;
    }

    public void resetUnloadTime() {
        unloadTime = 0;
    }

    public void setUnloadTime(int day, int min) {
        unloadTime = (day * 1440 + min) - getIntUnloadStartTime();
    }

    public int getUnloadTime() {
        return unloadTime;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getHours() {
        return time / 60;
    }

    public int getMinutes() {
        return (time - getHours() * 60);
    }

    public void unload(int craneProductivity) {
        weight.addAndGet(-craneProductivity);
    }
}
