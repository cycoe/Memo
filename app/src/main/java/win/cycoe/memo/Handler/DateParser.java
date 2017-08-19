package win.cycoe.memo.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cycoe on 17-8-17.
 */

public class DateParser {

    private final String[] DATEPREFIX = {"今天", "昨天", "前天"};

    /**
     * 1. instantiate a dateFormat object with the pattern of "yyyy-MM-dd HH:mm"
     * 2. return the formated date string
     */
    public String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(new Date());
    }

    public String getRelativeTime(String timeString) {

        String date = timeString.split(" ")[0];
        String time = timeString.split(" ")[1];
        String currentDate = getCurrentTime().split(" ")[0];
        String[] dateList = date.split("-");
        String[] currentDateList = currentDate.split("-");
        int dateDiff = Integer.parseInt(currentDateList[2]) - Integer.parseInt(dateList[2]);

        if(Integer.parseInt(dateList[0]) == Integer.parseInt(currentDateList[0])
                && Integer.parseInt(dateList[1]) == Integer.parseInt(currentDateList[1])
                && dateDiff < 3)
            return DATEPREFIX[dateDiff] + " " + time;
        else
            return timeString;
    }

}
