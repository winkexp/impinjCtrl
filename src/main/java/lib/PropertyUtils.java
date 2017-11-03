package lib;

import com.impinjCtrl.Properties;
import com.impinjCtrl.ReaderController;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PropertyUtils {

    public static boolean isDebugMode() {
        String debugMode = System.getProperty(Properties.debugMode, "0");
        return debugMode.equals("1");
    }
    public static String getLogFileName() {
        String logDir = System.getProperty(Properties.logDir,"./");
        String logFileName = "R420_";

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间

        String jsonLogFileName = logDir + logFileName + date + ".json";
        System.out.println("LogFileName: " + jsonLogFileName);
        return jsonLogFileName;
    }

    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static Long getDefaultValidIntervalMs () {
        // Default interval: 500ms
        return Long.parseLong(System.getProperty(Properties.validIntervalMs, "500"));
    }
}
