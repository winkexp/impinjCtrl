package com.impinjCtrl;

public class Properties {
//    public static String readerHost = "readerHost";
//    public static String apiHost = "apiHost";
//    public static String debugMode = "debugMode";
//    public static String validIntervalMs = "validIntervalMs";

    // R420 阅读器IP
    public static String readerHost = "192.168.3.7";

    // API host:port
    public static String apiHost = "https://azai.synology.me:8080";

    // API URI
    public static String apiUri = "/api/socket/impinj";

    // debug mode
    public static boolean isDebugMode = true;

    // default log dir
    public static String logDir = "/tmp/";

    // report interval in ms
    public static String validIntervalMs = "500";

    // 天线发射功率
    // double. The amount of transmit power to use on the antenna. Only use power levels the Reader supports,
    // typically in the range of 10.00 to 30.00 dBm in 0.25 dBm steps.
    public static double antennaTxPowerinDbm = 23.0;

    //信号接收灵敏度
    // bool. Whether the Reader’s maximum sensitivity is used. When true the Reader reports all tags that it can
    // successfully communicate with. False indicates that the Reader should only report tags that have a return
    // signal strength specified by RxSenstivityInDbm (see next field).
    public static int rxSensitivityinDbm = -70;


}
