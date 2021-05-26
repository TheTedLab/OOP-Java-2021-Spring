package main.ServiceFirst;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/serviceFirst")
public class ServiceFirstController {

    @GetMapping("/schedule")
    public ResponseEntity<List<Ship>> getSchedule() {
        return new ResponseEntity<>(ScheduleGenerator.generateSchedule(100), HttpStatus.OK);
    }
}
