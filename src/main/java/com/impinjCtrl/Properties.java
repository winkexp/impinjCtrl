package com.impinjCtrl;

public class Properties {
    // R420 阅读器IP
    //public static String readerHost = "192.168.3.7";
    public static String readerHost = "readerHost";

    // debug mode, default false
    public static String debugMode = "debugMode";

    // default 500ms
    public static String validIntervalMs = "validIntervalMs";

    // 天线发射功率, default 23.0
    // double. The amount of transmit power to use on the antenna. Only use power levels the Reader supports,
    // typically in the range of 10.00 to 30.00 dBm in 0.25 dBm steps.
    //public static double antennaTxPowerinDbm = 23.0;
    public static String txPowerinDbm = "txPowerinDbm";

    // default log dir, default .
    public static String logDir = "logDir";

    //信号接收灵敏度
    // bool. Whether the Reader’s maximum sensitivity is used. When true the Reader reports all tags that it can
    // successfully communicate with. False indicates that the Reader should only report tags that have a return
    // signal strength specified by RxSenstivityInDbm (see next field).
    public static int rxSensitivityinDbm = -70;
    //public static String rxSensitivityinDbm = "rxSensitivityinDbm";
}
