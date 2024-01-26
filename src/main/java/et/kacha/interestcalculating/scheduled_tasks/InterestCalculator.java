package et.kacha.interestcalculating.scheduled_tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//import java.util.Calendar;
//import java.util.Date;

@Service
@Slf4j
public class InterestCalculator {

    @Scheduled(cron = "*/5 * * * * *", zone = "GMT+3")
    public void searchProducts() {

        /*Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, -168);
        Date weekBack = cal.getTime();*/
        log.info("Calculating interest for customer");

    }
}
