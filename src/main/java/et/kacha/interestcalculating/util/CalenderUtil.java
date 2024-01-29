package et.kacha.interestcalculating.util;

import java.util.Calendar;
import java.util.Date;

public class CalenderUtil {
    public static Calendar getLastDayOfMonth(Calendar calendar) {
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        return calendar;
    }

    public static Calendar getFirstDayOfMonth(Calendar currentDate) {
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        return currentDate;
    }
}
