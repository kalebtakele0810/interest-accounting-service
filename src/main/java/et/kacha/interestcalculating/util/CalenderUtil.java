package et.kacha.interestcalculating.util;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class CalenderUtil {
    public LocalDate getLastDayOfMonth(LocalDate currentDate ) {
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();

        // Get the first day of the next month
        LocalDate firstDayOfNextMonth = LocalDate.of(year, month + 1, 1);

        // Subtract one day to get the last day of the current month
        return firstDayOfNextMonth.minusDays(1);
    }

}
