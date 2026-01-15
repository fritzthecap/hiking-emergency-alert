package fri.servers.hiking.emergencyalert.time;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import fri.servers.hiking.emergencyalert.util.DateUtil;

/** Ignores given date/time and schedules every task to 1 second in future. */
public class ImpatientTimer extends HikeTimer
{
    @Override
    public Timer newTimer() {
        return new Timer() {
            @Override
            public void schedule(TimerTask task, Date time) {
                final Date testTime = DateUtil.addSeconds(DateUtil.now(), 1);
                super.schedule(task, testTime);
            }
        };
    }
}