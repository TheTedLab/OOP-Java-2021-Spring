package main.ServiceThird;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Port {

    private static final int MARCH = 3;
    private static final int CRANE_PRICE = 30000;

    public static void simulate(String scheduleFileName, String reportFileName) {

        //Объект сбора статистики для отчета
        ReportStatistics reportStatistics;

        //Конфигурация
        int containerCraneProductivity = 100;
        int fluidCraneProductivity = 200;
        int friableCraneProductivity = 500;

        //Преобразование из json-файла
        Gson gson = new Gson();
        Type shipsListType = new TypeToken<List<ArrivedShip>>() {
        }.getType();
        List<ArrivedShip> ships = gson.fromJson(scheduleFileName, shipsListType);

        //Генерация опоздания в расписании
        Random random = new Random();
        generateLate(ships, random);

        //Сортировка кораблей
        ships.sort(new DayAndTimeComparator());

        //Обновление времени ожидания разгрузки
        updateWaitTime(containerCraneProductivity, fluidCraneProductivity,
                friableCraneProductivity, ships);

        //Вывод расписания
        printShips(ships, "Ships Schedule: ");

        //Генерация задежки разгрузки
        generateDelay(containerCraneProductivity, fluidCraneProductivity,
                friableCraneProductivity, ships, random);

        //Количество кранов
        int numOfContainerCranes = 1;
        int numOfFluidCranes = 1;
        int numOfFriableCranes = 1;

        //Учет штрафов
        int containerPenalty = 0;
        int fluidPenalty = 0;
        int friablePenalty = 0;
        int allPenalty = 0;

        //Изначальные веса кораблей
        List<Integer> weights = new ArrayList<>(ships.size());
        for (ArrivedShip ship : ships) {
            weights.add(ship.getWeight());
        }

        do {

            //Добавление кранов
            if (containerPenalty > CRANE_PRICE || fluidPenalty > CRANE_PRICE || friablePenalty > CRANE_PRICE) {
                if (containerPenalty > CRANE_PRICE) {
                    numOfContainerCranes++;
                }
                if (fluidPenalty > CRANE_PRICE) {
                    numOfFluidCranes++;
                }
                if (friablePenalty > CRANE_PRICE) {
                    numOfFriableCranes++;
                }
            } else if (allPenalty > CRANE_PRICE) {
                int maxCranePenalty =
                        Math.max(Math.max(containerPenalty, fluidPenalty), friablePenalty);
                if (maxCranePenalty == containerPenalty) {
                    numOfContainerCranes++;
                } else if (maxCranePenalty == fluidPenalty) {
                    numOfFluidCranes++;
                } else {
                    numOfFriableCranes++;
                }
            }

            //Ресет штрафов
            containerPenalty = 0;
            fluidPenalty = 0;
            friablePenalty = 0;

            //Установка всех грузов, отмена прибытия и обновление времени разгрузки
            for (int i = 0; i < ships.size(); i++) {
                ships.get(i).setWeight(weights.get(i));
                ships.get(i).setCame(false);
                ships.get(i).resetUnloadTime();
            }

            //Учет ожидания разгрузки
            int[] shipsDelays = new int[ships.size()];

            //Создание очередей на ожидание
            ArrayDeque<ArrivedShip> containerShips = new ArrayDeque<>();
            ArrayDeque<ArrivedShip> fluidShips = new ArrayDeque<>();
            ArrayDeque<ArrivedShip> friableShips = new ArrayDeque<>();

            //Создание рабочих очередей
            ArrayDeque<ArrivedShip> containerQueue = new ArrayDeque<>();
            ArrayDeque<ArrivedShip> fluidQueue = new ArrayDeque<>();
            ArrayDeque<ArrivedShip> friableQueue = new ArrayDeque<>();

            //Сортировка полного списка по очередям
            for (ArrivedShip ship : ships) {
                String shipType = ship.getType();
                switch (shipType) {
                    case "CONTAINER" -> containerShips.add(ship);
                    case "FLUID" -> fluidShips.add(ship);
                    case "FRIABLE" -> friableShips.add(ship);
                }
            }

            int numOfContainerShips = containerShips.size();
            int numOfFluidShips = fluidShips.size();
            int numOfFriableShips = friableShips.size();

            //Создание барьера
            int numOfCranes = numOfContainerCranes + numOfFluidCranes + numOfFriableCranes;
            CyclicBarrier barrier = new CyclicBarrier(numOfCranes + 1);

            //Список всех потоков
            List<Thread> threads = new ArrayList<>(numOfCranes);

            //Создание кранов (каждый кран в своем потоке)
            List<Crane> containerCranes = createCranes(containerCraneProductivity, numOfContainerCranes,
                    barrier, threads, "ContainerThread ");

            List<Crane> fluidCranes = createCranes(fluidCraneProductivity, numOfFluidCranes,
                    barrier, threads, "FluidThread ");

            List<Crane> friableCranes = createCranes(friableCraneProductivity, numOfFriableCranes,
                    barrier, threads, "FriableThread ");

            int containerQueueLength;
            int fluidQueueLength;
            int friableQueueLength;
            int allQueueLength = 0;
            int indexLastCameShip = 0;
            //Моделирование порта
            for (int days = 1; days < 31; days++) {
                System.out.println("Day: " + days);
                for (int min = 0; min < 1440; min++) {

                    //Проверка прибытия кораблей
                    indexLastCameShip = checkShipsArrival(ships, indexLastCameShip, days, min);

                    //Распределение кранов по кораблям
                    setCraneToShip(containerShips, containerQueue, containerCranes, days, min);

                    setCraneToShip(fluidShips, fluidQueue, fluidCranes, days, min);

                    setCraneToShip(friableShips, friableQueue, friableCranes, days, min);

                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    containerQueueLength = 0;
                    fluidQueueLength = 0;
                    friableQueueLength = 0;

                    //Начисление штрафов
                    for (int i = 0; i < ships.size(); i++) {
                        if (ships.get(i).isCame() && ships.get(i).getWeight() == weights.get(i)) {
                            shipsDelays[i] += 1;
                            String shipType = ships.get(i).getType();
                            switch (shipType) {
                                case "CONTAINER" -> containerQueueLength++;
                                case "FLUID" -> fluidQueueLength++;
                                case "FRIABLE" -> friableQueueLength++;
                            }
                            if (shipsDelays[i] % 60 == 0) {
                                switch (shipType) {
                                    case "CONTAINER" -> containerPenalty += 100;
                                    case "FLUID" -> fluidPenalty += 100;
                                    case "FRIABLE" -> friablePenalty += 100;
                                }
                            }
                        } else if (!ships.get(i).isCame()) {
                            break;
                        }
                    }

                    //Подсчет времени разгрузки
                    for (ArrivedShip ship : ships) {
                        if (ship.isCame() && ship.getWeight() == 0
                                && ship.getUnloadTime() == 0) {
                            ship.setUnloadTime(days, min);
                        } else if (!ship.isCame()) {
                            break;
                        }
                    }

                    //Подсчет всех длин очередей
                    allQueueLength += (containerQueueLength + fluidQueueLength + friableQueueLength);
                }
            }

            //Подсчет средней длины очереди
            double averageQueueLength = (double) allQueueLength / (30 * 1440);

            //Подсчет всех штрафов
            allPenalty = containerPenalty + fluidPenalty + friablePenalty;

            //Подсчет разгруженных судов и среднего ожидания в очереди
            int unloadedShips = 0;
            int allQueueTime = 0;
            for (int i = 0; i < ships.size(); i++) {
                if (ships.get(i).getWeight() == 0) {
                    unloadedShips++;
                }
                allQueueTime += shipsDelays[i];
            }
            double averageWaitTime = (double) allQueueTime / unloadedShips;

            //Остановка кранов после моделирования
            for (int i = 0; i < numOfContainerCranes; i++) {
                containerCranes.get(i).setDone(true);
            }

            for (int i = 0; i < numOfFluidCranes; i++) {
                fluidCranes.get(i).setDone(true);
            }

            for (int i = 0; i < numOfFriableCranes; i++) {
                friableCranes.get(i).setDone(true);
            }

            //Ресет барьера, на случай если потоки уснули в нем
            barrier.reset();

            //Ожидание завершения всех потоков
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Вывод списка обработанных кораблей
            printShips(ships, "Ships List: ");

            //Создание объекта статистики текущего цикла
            reportStatistics = new ReportStatistics(ships, shipsDelays,
                    containerCraneProductivity, fluidCraneProductivity, friableCraneProductivity,
                    ships.size(), numOfContainerShips, numOfFluidShips, numOfFriableShips,
                    numOfContainerCranes, numOfFluidCranes, numOfFriableCranes);

            //Сбор статистики
            reportStatistics.collectStatistics(containerPenalty, fluidPenalty, friablePenalty,
                    allPenalty, unloadedShips, averageWaitTime, averageQueueLength,
                    getAverageDelay(ships),getMaxDelay(ships));

            //Вывод статистики в консоль
            ArrivedShip reportShip;
            for (int i = 0; i < ships.size(); i++) {
                reportShip = ships.get(i);
                System.out.println("\n" + i + "       Name: " + reportShip.getName());
                System.out.println("        Type: " + reportShip.getType());
                System.out.println("     Arrival: " + getStringDate(reportShip.getDay(),
                                                                    reportShip.getMonth()) + " "
                                                    + getStringTime(reportShip.getHours(),
                                                                    reportShip.getMinutes()));
                System.out.println("   Wait Time: " + getShipsDelay(shipsDelays[i]));
                System.out.println("Unload Start: " + reportShip.getUnloadStart());
                System.out.println(" Unload Time: " + getUnloadTime(ships, i,
                                                                    reportShip.getIntMonth()));
            }

            System.out.println("\n*CONFIG*");
            System.out.println("---PRODUCTIVITY---");
            System.out.println("Container Productivity: " + containerCraneProductivity);
            System.out.println("    Fluid Productivity: " + fluidCraneProductivity);
            System.out.println("  Friable Productivity: " + friableCraneProductivity);
            System.out.println("---SHIPS---");
            System.out.println("Number Of Ships: " + ships.size());
            System.out.println("Container Ships: " + numOfContainerShips);
            System.out.println("    Fluid Ships: " + numOfFluidShips);
            System.out.println("  Friable Ships: " + numOfFriableShips);
            System.out.println("\n*STATISTICS*");
            System.out.println("---CRANES---");
            System.out.println("Container Cranes: " + numOfContainerCranes);
            System.out.println("    Fluid Cranes: " + numOfFluidCranes);
            System.out.println("  Friable Cranes: " + numOfFriableCranes);
            System.out.println("---PENALTIES---");
            System.out.println("Container Penalty: " + containerPenalty);
            System.out.println("    Fluid Penalty: " + fluidPenalty);
            System.out.println("  Friable Penalty: " + friablePenalty);
            System.out.println("      ALL PENALTY: " + allPenalty);
            System.out.println("---SHIPS---");
            System.out.println("      Unloaded Ships: " + unloadedShips);
            System.out.printf("   Average Wait Time: %.2f\n", averageWaitTime);
            System.out.printf("Average Queue Length: %.2f\n", averageQueueLength);
            System.out.printf("Average Unload Delay: %.2f\n", getAverageDelay(ships));
            System.out.println("    Max Unload Delay: " + getMaxDelay(ships));
        } while (allPenalty > CRANE_PRICE);
        //Если штрафы минимизированы, конец симуляции

        //Итоги по кранам
        System.out.println("TOTAL CRANES: ");
        System.out.println("CONTAINER CRANES: " + numOfContainerCranes);
        System.out.println("    FLUID CRANES: " + numOfFluidCranes);
        System.out.println("  FRIABLE CRANES: " + numOfFriableCranes);

        //Отправка отчета на POST эндпойнт 2 сервиса
        RestTemplate restTemplate = new RestTemplate();
        Gson gsonReport = new GsonBuilder().setPrettyPrinting().create();
        restTemplate.postForEntity("http://localhost:8080/serviceSecond/saveReport/" + reportFileName,
                gsonReport.toJson(reportStatistics), String.class);
        System.out.println("Finished!");
    }

    private static int getMaxDelay(List<ArrivedShip> ships) {
        int maxDelay = 0;
        for (ArrivedShip ship: ships) {
            if (ship.getDelay() > maxDelay) {
                maxDelay = ship.getDelay();
            }
        }
        return maxDelay;
    }

    private static double getAverageDelay(List<ArrivedShip> ships) {
        int allDelay = 0;
        for (ArrivedShip ship: ships) {
            allDelay += ship.getDelay();
        }
        return (double) allDelay / (double) ships.size();
    }

    private static int checkShipsArrival(List<ArrivedShip> ships, int indexLastCameShip, int days, int min) {
        for (int i = indexLastCameShip; i < ships.size(); i++) {
            if (ships.get(i).getIntDay() == days &&
                    ships.get(i).getIntTime() == min &&
                    ships.get(i).getMonth() == MARCH) {
                ships.get(i).setCame(true);
            } else {
                indexLastCameShip = i;
                break;
            }
        }
        return indexLastCameShip;
    }

    private static String getShipsDelay(int shipsDelay) {
        int delayDays = shipsDelay / 1440;
        int delayHours = (shipsDelay - delayDays * 1440) / 60;
        int delayMin = (shipsDelay - delayDays * 1440 - delayHours * 60);
        String days = getStringTimeOrDayParam(delayDays);
        return (days + ":" + getStringTime(delayHours, delayMin));
    }

    private static String getUnloadTime(List<ArrivedShip> ships, int i, int month) {
        if (month == MARCH && ships.get(i).isCame()) {
            int unloadTime = ships.get(i).getUnloadTime();
            int unloadHours = unloadTime / 60;
            int unloadMinutes = unloadTime - unloadHours * 60;
            return getStringTime(unloadHours, unloadMinutes);
        } else {
            return "NO UNLOAD TIME!";
        }
    }

    private static String getStringTimeOrDayParam(int timeParam) {
        String hours;
        if (timeParam < 10) {
            hours = "0" + timeParam;
        } else {
            hours = Integer.toString(timeParam);
        }
        return hours;
    }

    private static String getStringDate(int day, int month) {
        String strDay = getStringTimeOrDayParam(day);
        String strMonth = getStringTimeOrDayParam(month);
        return "2021-" + strMonth + "-" + strDay;
    }

    private static String getStringTime(int hoursParam, int minutesParam) {
        String hours = getStringTimeOrDayParam(hoursParam);
        String minutes = getStringTimeOrDayParam(minutesParam);
        return (hours + ":" + minutes);
    }

    private static void updateWaitTime(int containerCraneProductivity, int fluidCraneProductivity,
                                       int friableCraneProductivity, List<ArrivedShip> ships) {
        for (ArrivedShip ship: ships) {
            String shipType = ship.getType();
            switch (shipType) {
                case "CONTAINER" -> ship.resetWaitTime(containerCraneProductivity);
                case "FLUID" -> ship.resetWaitTime(fluidCraneProductivity);
                case "FRIABLE" -> ship.resetWaitTime(friableCraneProductivity);
            }
        }
    }

    private static void printShips(List<ArrivedShip> ships, String printName) {
        System.out.println(printName);
        int counter = 0;
        for (ArrivedShip ship : ships) {
            counter++;
            System.out.println(counter + ". " + ship);
        }
    }

    private static void generateDelay(int containerProductivity, int fluidProductivity,
                                      int friableProductivity, List<ArrivedShip> ships, Random random) {
        for (ArrivedShip ship: ships) {
            int delay = random.nextInt(1440);
            ship.setDelay(delay);
            int shipCraneProductivity = 0;
            if (ship.getMonth() == MARCH) {
                String shipType = ship.getType();
                switch (shipType) {
                    case "CONTAINER" -> shipCraneProductivity = containerProductivity;
                    case "FLUID" -> shipCraneProductivity = fluidProductivity;
                    case "FRIABLE" -> shipCraneProductivity = friableProductivity;
                }
                ship.setWeight(ship.getWeight() + delay * shipCraneProductivity);
            }
        }
    }

    private static void generateLate(List<ArrivedShip> ships, Random random) {
        for (ArrivedShip ship: ships) {
            int shipDay = ship.getIntDay();
            int newDay = shipDay;
            if (shipDay > 7) {
                int lateness = random.nextInt(14) - 7;
                newDay += lateness;
            }
            int month = (newDay > 30) ? 4 : 3;
            int day = 0;
            if (newDay > 30) {
                newDay -= 30;
            }
            day += newDay;
            ship.setDay(day);
            ship.setMonth(month);
        }
    }

    private static class DayAndTimeComparator implements Comparator<ArrivedShip> {

        @Override
        public int compare(ArrivedShip o1, ArrivedShip o2) {
            int monthCompare = Integer.compare(o1.getMonth(), o2.getMonth());
            if (monthCompare < 0) {
                return -1;
            } else if (monthCompare > 0) {
                return 1;
            } else {
                int dayCompare = Integer.compare(o1.getDay(), o2.getDay());
                if (dayCompare < 0) {
                    return -1;
                } else if (dayCompare > 0) {
                    return 1;
                } else {
                    return Integer.compare(o1.getTime(), o2.getTime());
                }
            }
        }
    }

    private static List<Crane> createCranes(int craneProductivity, int numOfCranes,
                                            CyclicBarrier barrier,
                                            List<Thread> threads,
                                            String threadType) {
        List<Crane> containerCranes = new ArrayList<>();
        for (int i = 0; i < numOfCranes; i++) {
            Crane crane = new Crane(barrier, craneProductivity);
            Thread thread = new Thread(crane);
            thread.setName(threadType + (i + 1));
            containerCranes.add(crane);
            threads.add(thread);
            thread.start();
        }
        return containerCranes;
    }

    private static void setCraneToShip(ArrayDeque<ArrivedShip> shipsList, ArrayDeque<ArrivedShip> shipsQueue,
                                       List<Crane> cranesList, int day, int min) {
        //Цикл по кранам
        for (Crane currentCrane : cranesList) {
            //Если кран свободен
            if (currentCrane.ship == null) {
                //Если рабочая очередь пустая
                if (shipsQueue.isEmpty()) {
                    //Если корабли не закончились
                    if (!shipsList.isEmpty()) {
                        //Берем корабль из списка
                        ArrivedShip currentShip = shipsList.getFirst();
                        //Если он пришел, ставим кран на корабль, заносим в рабочую очередь
                        if (currentShip.isCame()) {
                            shipsList.pollFirst();
                            shipsQueue.addLast(currentShip);
                            currentCrane.setShip(currentShip);
                            //Отметка о начале разгрузки
                            currentShip.setUnloadStart(day, min);
                        } else {
                            break;
                        }
                    }
                } else {
                    //Если рабочая очередь не пустая, ставим второй кран на корабль
                    ArrivedShip currentShip = shipsQueue.getFirst();
                    shipsQueue.pollFirst();
                    currentCrane.setShip(currentShip);
                }
            }
        }
    }

    private static class Crane implements Runnable {
        private final CyclicBarrier barrier;
        private final int craneProductivity;
        private ArrivedShip ship = null;
        private volatile boolean done = false;

        public Crane(CyclicBarrier barrier, int craneProductivity) {
            this.barrier = barrier;
            this.craneProductivity = craneProductivity;
        }

        @Override
        public void run() {
            while (!done) {
                if (ship != null) {
                    if (ship.getWeight() > 0) {
                        ship.unload(craneProductivity);
                        if (ship.getWeight() < 0) {
                            ship.processed();
                            setShip(null);
                        }
                    } else {
                        ship.processed();
                        setShip(null);
                    }
                }

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    done = true;
                }
            }
        }
        public void setShip(ArrivedShip ship) {
            this.ship = ship;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }
}
