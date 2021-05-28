package main.ServiceThird;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FilenameFilter;

@RestController
@RequestMapping("/serviceThird")
public class ServiceThirdController {
    private RestTemplate restTemplate = new RestTemplate();
    private String serviceSecondURL = "http://localhost:8080/serviceSecond/getScheduleByName/";
    private String sourceFolderPath = "src/main/resources/";

    @GetMapping(value = {"/simulateSchedule/{scheduleFileName}",
            "/simulateSchedule/{scheduleFileName}/{reportFileName}"})
    public ResponseEntity<Boolean> simulateSchedule(@PathVariable String scheduleFileName,
                                                    @PathVariable(required = false)
                                                            String reportFileName) {
        //Просмотр уже имеющихся отчетов
        File directory = new File(sourceFolderPath);
        FilenameFilter filenameFilter = (file, path) -> path.startsWith("report-");
        File[] reports = directory.listFiles(filenameFilter);

        //Определение id отчета - генерация или кастомный
        String reportId;
        String reportName;
        //Кастомное
        if (reportFileName != null) {
            for (File file : reports) {
                if (file.getName().equals(reportFileName)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            reportFileName + " report already exists");
                }
            }
            reportName = reportFileName;
        } else {
            //Генерация
            int maxReportNumber = 0;
            for (File file : reports) {
                int fileNumber = Integer.parseInt(
                        file.getName().substring(7, file.getName().lastIndexOf('.')));
                if (fileNumber > maxReportNumber) {
                    maxReportNumber = fileNumber;
                }
            }
            reportId = String.valueOf((maxReportNumber + 1));
            reportName = "report-" + reportId;
        }

        //Получение расписания GET эндпоинтом 2 сервиса и отправка на симуляцию в порт
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(serviceSecondURL
                + scheduleFileName, String.class);
        Port.simulate(responseEntity.getBody(), reportName);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
