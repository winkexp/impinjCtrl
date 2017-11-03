package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
//import com.sun.istack.internal.NotNull;

import lib.PropertyUtils;
import lib.TextUtils;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.util.Scanner;
//import com.sun.istack.internal.Nullable;


public class ReaderController {
    // Race session-based data, init at startreader, destroy at terminatereader
    public static Long mValidIntervalMs;
    public static String mLogFileName;
    public static JSONObject mRecordsHashTable;

    private boolean mIsDebugMode;
    private String mReaderHost;
    private ImpinjReader mReader;

    ReaderController(String readerHost) {
        this.mReaderHost = readerHost;
        mIsDebugMode = PropertyUtils.isDebugMode();
    }

    public void initialize() {
        mReader = new ImpinjReader();
        if (mIsDebugMode) {
            initReader();
        } else {
            initReader();
        }
    }

    private void initReader() {
        if (null == mReader) {
            System.out.println("reader obj is null: initialReader");
            return;
        }
        System.out.println("Initializing reader ...");
        try {
            //dischargeReader();
            mReader.connect(mReaderHost);

            System.out.println("Applying reader settings ...");
            Settings settings = ReaderSettings.getSettings(mReader);
            mReader.applySettings(settings);

            mReader.setTagReportListener(new ReportFormat());

            TextUtils.printUsage();
            // 後門
            Scanner s = new Scanner(System.in);
            while (s.hasNextLine() && mReader.isConnected()) {
                String line = s.nextLine();
                System.out.println("Received Command: " + line);
                if (line.equals("START")) {
                    mValidIntervalMs = PropertyUtils.getDefaultValidIntervalMs();
                    mRecordsHashTable = new JSONObject();
                    mReader.start();

                    mLogFileName = PropertyUtils.getLogFileName();
                    FileWriter file = new FileWriter(ReaderController.mLogFileName);
                    file.write("");
                    file.flush();

                } else if (line.equals("STOP")) {
                    mValidIntervalMs = null;
                    mRecordsHashTable = null;
                    mReader.stop();
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getReaderInfo(mReader, settings);
                } else {
                    System.out.println("!!! Wrong Command: " + line);
                    TextUtils.printUsage();
                }
            }
        } catch (OctaneSdkException e) {
            System.out.println("InitReader error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("InitReader error: " + e.getMessage());
        }
    }
//    private void dischargeReader() {
//        if (mReader.isConnected()) {
//            System.out.println("Discharging reader");
//            try {
//                mReader.removeTagReportListener();
//                mReader.stop();
//                mReader.disconnect();
//            } catch (OctaneSdkException e) {
//                System.out.println("dischargeReader error: " + e.getMessage());
//            } catch (Exception e) {
//                System.out.println("dischargeReader error: " + e.getMessage());
//            }
//        }
//    }
}
