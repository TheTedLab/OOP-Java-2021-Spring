package main.ServiceFirst;

import main.exception.IllegalAmountOfShipsException;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ScheduleGenerator {

    private static final NameGenerator nameGenerator = new NameGenerator(10);
    private static final int maxWeight = 10000;
    private static final int craneProductivity = 100;
    private static final Random random = new Random();

    public static List<Ship> generateSchedule(int ships) {
        if (ships > 0) {
            List<Ship> schedule = new LinkedList<>();

            for (int i = 0; i < ships; i++) {
                int currentWeight = generateWeight();
                double currentWaitTime = generateWaitTime(currentWeight);
                schedule.add(new Ship(generateDay(), generateTime(), generateName(),
                        generateCargoType(), currentWeight, currentWaitTime));
            }

            return schedule;
        } else {
            throw new IllegalAmountOfShipsException();
        }
    }

    private static int generateDay() {
        int minDay = 1;
        int maxDay = 31;
        return minDay + random.nextInt(maxDay - minDay);
    }

    private static int generateTime() {
        return random.nextInt(24) * 60 + random.nextInt(60);
    }

    private static String generateName() {
        return nameGenerator.getName();
    }

    private static cargoType generateCargoType() {
        int randomType = random.nextInt(3);
        return cargoType.getType(randomType);
    }

    private static int generateWeight() {
        return random.nextInt(maxWeight);
    }

    private static double generateWaitTime(int weight) {
        return (double) weight / (double) craneProductivity;
    }
}
