package fri.servers.hiking.emergencyalert.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import fri.servers.hiking.emergencyalert.statemachine.Event;
import fri.servers.hiking.emergencyalert.util.DateUtil;

class HikeTimerTest
{
    @Test
    @Disabled("Lasts 2 minutes!")
    void timerShouldWorkSuccessfully() throws Exception {
        // set up test data
        
        final int EVENT_COUNT = 3;
        final int SET_OFF_MINUTES = 0; // send SETTING_OFF in ... minute
        // scheduler will start a task dated to past immediately
        final int OVERDUE_ALERT_MINUTES = 1; // send OVERDUE_ALERT in ... minute intervals
        
        final Date now = DateUtil.now();
        final Date plannedBegin = DateUtil.addMinutes(now, SET_OFF_MINUTES); // start now
        final Date plannedHome = DateUtil.addMinutes(now, SET_OFF_MINUTES + OVERDUE_ALERT_MINUTES); // end in 1 minute
        
        final int TEST_MINUTES = SET_OFF_MINUTES + (EVENT_COUNT - 1) * OVERDUE_ALERT_MINUTES;
        
        // run test
        
        final List<Integer> dispatchMinutes = new ArrayList<>();
        final HikeTimer timer = new HikeTimer();
        
        final HikeTimer.EventDispatcher dispatcher = new HikeTimer.EventDispatcher() {
            private int count = 0;
            
            @Override
            public void dispatchEvent(Event event) {
                final Date now = DateUtil.now();
                System.err.println("Event "+count+" "+event+" dispatched at "+now);
                
                dispatchMinutes.add(DateUtil.getMinute(now)); // collect result minutes
                
                count++;
                if (count == EVENT_COUNT)
                    timer.stop(); // stops test
            }
        };
        
        System.err.println("This is a test lasting "+TEST_MINUTES+" minutes! Starting timer at "+DateUtil.now());
        timer.start(plannedBegin, plannedHome, OVERDUE_ALERT_MINUTES, dispatcher);
        
        while (timer.isRunning()) // wait for timer background thread to be stopped
            Thread.sleep(1000);
        
        // assert result
        
        assertEquals(EVENT_COUNT, dispatchMinutes.size());
        
        // check event dispatch result minutes
        Integer previousMinute = null;
        for (Integer minute : dispatchMinutes) {
            if (previousMinute != null)
                assertEquals(OVERDUE_ALERT_MINUTES, minute - previousMinute);
            
            previousMinute = minute;
        }
        
        System.err.println("Finished long lasting test positively, dispatch minutes were: "+dispatchMinutes);
    }
}