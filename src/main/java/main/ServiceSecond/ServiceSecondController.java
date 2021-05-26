package main.ServiceSecond;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import main.ServiceFirst.Ship;
import main.ServiceFirst.cargoType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

@RestController
@RequestMapping("/serviceSecond")
public class ServiceSecondController {
    private RestTemplate restTemplate = new RestTemplate();
    private String serviceFirstURL = "http://localhost:8080/serviceFirst/schedule";
    private String sourceFolderPath = "src/main/resources/";

    @GetMapping(value = {"/getSchedule", "/getSchedule/{customId}"})
    public ResponseEntity<String> getSchedule(@PathVariable(required = false) String customId) {

        //Просмотр уже имеющихся расписаний
        File directory = new File(sourceFolderPath);
        FilenameFilter filenameFilter = (file, path) -> path.startsWith("schedule-");
        File[] schedules = directory.listFiles(filenameFilter);

        //Определение id расписания - генерация или кастомное
        String scheduleId;
        //Кастомное
        if (customId != null) {
            for (File file : schedules) {
                String fileNumberString =
                        file.getName().substring(9, file.getName().lastIndexOf('.'));
                if (fileNumberString.equals(customId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "schedule-" + customId + " already exists");
                }
                if (!isNumeric(customId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "custom Id not a number");
                }
            }
            scheduleId = customId;
        } else {
            //Генерация
            int maxScheduleNumber = 0;
            for (File file : schedules) {
                int fileNumber = Integer.parseInt(
                        file.getName().substring(9, file.getName().lastIndexOf('.')));
                if (fileNumber > maxScheduleNumber) {
                    maxScheduleNumber = fileNumber;
                }
            }
            scheduleId = String.valueOf((maxScheduleNumber + 1));
        }
        //Запрос генерации у первого сервиса
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(serviceFirstURL, String.class);
        Type shipsListType = new TypeToken<List<Ship>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Ship> schedule = gson.fromJson(responseEntity.getBody(), shipsListType);

        //Название расписания
        String scheduleName = sourceFolderPath + "schedule-" + scheduleId + ".json";

        try (FileWriter fileWriter = new FileWriter(scheduleName)) {
            fileWriter.write(gson.toJson(schedule));
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(scheduleName, HttpStatus.OK);
    }

    @GetMapping("/getScheduleByName/{filename}")
    public ResponseEntity<String> getScheduleByName(@PathVariable String filename) {
        StringBuilder fileNameResult = new StringBuilder();
        File file = new File(sourceFolderPath + filename);
        if (file.exists()) {
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    fileNameResult.append(scanner.nextLine());
                }
                return new ResponseEntity<>(fileNameResult.toString(), HttpStatus.OK);
            } catch (FileNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getSchedulesList")
    public ResponseEntity<String> getSchedulesList() {
        //Просмотр имен всех уже имеющихся расписаний
        File directory = new File(sourceFolderPath);
        FilenameFilter filenameFilter = (file, path) -> path.startsWith("schedule-");
        File[] arrFiles = directory.listFiles(filenameFilter);

        //Формирование списка расписаний
        StringBuilder filenamesBuilder = new StringBuilder();
        for (File file : arrFiles) {
            filenamesBuilder.append(file.getName()).append('\n');
        }
        return new ResponseEntity<>(filenamesBuilder.toString(), HttpStatus.OK);
    }

    @GetMapping("/getReportsList")
    public ResponseEntity<String> getReportsList() {
        //Просмотр имен всех уже имеющихся отчетов
        File directory = new File(sourceFolderPath);
        FilenameFilter filenameFilter = (file, path) -> path.startsWith("report-");
        File[] arrFiles = directory.listFiles(filenameFilter);

        //Формирование списка отчетов
        StringBuilder filenamesBuilder = new StringBuilder();
        for (File file : arrFiles) {
            filenamesBuilder.append(file.getName()).append('\n');
        }
        return new ResponseEntity<>(filenamesBuilder.toString(), HttpStatus.OK);
    }

    @PostMapping("/saveReport/{filename}")
    public ResponseEntity<String> saveReport(@RequestBody String report,
                                             @PathVariable String filename) {
        String reportName = sourceFolderPath + filename + ".json";
        try {
            FileWriter fileWriter = new FileWriter(reportName);
            fileWriter.write(report);
            fileWriter.close();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(reportName, HttpStatus.OK);
    }

    @GetMapping("/addShip/{filename}")
    public ResponseEntity<Boolean> addShip(@PathVariable String filename) {
        try (FileReader fileReader = new FileReader(sourceFolderPath + filename)) {
            //Чтение расписания из json
            Type shipsListType = new TypeToken<List<Ship>>() {}.getType();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<Ship> schedule = gson.fromJson(fileReader, shipsListType);

            //Добавление кораблей
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nEnter the number of ships to add: ");
            if (scanner.hasNextInt()) {
                int addingShips = scanner.nextInt();
                while (addingShips <= 0) {
                    System.out.println("Invalid value of ships, must be greater than zero.");
                    System.out.println("Try again!");
                    addingShips = scanner.nextInt();
                }

                for (int i = 0; i < addingShips; i++) {
                    System.out.println("\nEnter ship arrival day: ");
                    System.out.print("Day: ");
                    int day = scanner.nextInt();
                    while (day < 1 || day > 31) {
                        System.out.println("Invalid day value, must be between 1 and 30.");
                        System.out.println("Try again!");
                        day = scanner.nextInt();
                    }

                    System.out.println("\nEnter ship arrival time: ");
                    System.out.print("Hours: ");
                    int hours = scanner.nextInt();
                    while (hours < 0 || hours > 23) {
                        System.out.println("Invalid hour value, must be between 0 and 23.");
                        System.out.println("Try again!");
                        hours = scanner.nextInt();
                    }
                    System.out.print("Minutes: ");
                    int minutes = scanner.nextInt();
                    while (minutes < 0 || minutes > 59) {
                        System.out.println("Invalid minute value, must be between 0 and 59.");
                        System.out.println("Try again!");
                        minutes = scanner.nextInt();
                    }
                    int time = hours * 60 + minutes;

                    scanner.nextLine();
                    System.out.print("\nEnter ship name: ");
                    String name = scanner.nextLine();

                    System.out.println("\nEnter ship cargo type (FRIABLE, FLUID, CONTAINER): ");
                    String type = scanner.nextLine();
                    while (!type.equals("FRIABLE") && !type.equals("FLUID") && !type.equals("CONTAINER")) {
                        System.out.println("Invalid cargo type value, must be FRIABLE, FLUID or CONTAINER.");
                        System.out.println("Try again!");
                        type = scanner.nextLine();
                    }

                    System.out.println("\nEnter ship weight: ");
                    int weight = scanner.nextInt();
                    while (weight < 0 || weight > 10000) {
                        System.out.println("Invalid weight value, must be between 0 and 10000.");
                        System.out.println("Try again!");
                        weight = scanner.nextInt();
                    }

                    double waitTime = (double) weight / (double) 100;

                    switch (type) {
                        case "FRIABLE" -> schedule.add(new Ship(day, time, name, cargoType.FRIABLE,
                                weight, waitTime));
                        case "FLUID" -> schedule.add(new Ship(day, time, name, cargoType.FLUID,
                                weight, waitTime));
                        case "CONTAINER" -> schedule.add(new Ship(day, time, name, cargoType.CONTAINER,
                                weight, waitTime));
                    }
                }
            }

            try (FileWriter fileWriter = new FileWriter(sourceFolderPath + filename)) {
                fileWriter.write(gson.toJson(schedule));
            } catch (Exception err) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
