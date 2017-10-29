package lib;

import com.impinjCtrl.Properties;
import com.impinjCtrl.ReaderController;

public class PropertyUtils {

    public static boolean isDebugMode() {
        return Properties.isDebugMode;
    }

    public static String getLogFileName() {
        String logDir = Properties.logDir;
        String fileName = "R420_log";

//        if (ReaderController.mRaceId != null) {
//            fileName = "race-" + ReaderController.mRaceId;
//        } else {
//            fileName = "event-" + ReaderController.mEventId;
//        }

        System.out.println("LogFileName: " + fileName);
        return logDir + fileName + ".json";
    }

    public static String getAPiHost() {
        return System.getProperty(Properties.apiHost);
    }

    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static Long getDefaultValidIntervalMs () {
        // Default interval: 1000ms
        return Long.parseLong(Properties.validIntervalMs);
    }
}
