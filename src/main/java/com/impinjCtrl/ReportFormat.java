package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import java.io.FileWriter;
import lib.PropertyUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;

public class ReportFormat implements TagReportListener {
    private boolean mIsDebugMode = PropertyUtils.isDebugMode();

    private void writeJSONToFile() {
        try {
            FileWriter file = new FileWriter(ReaderController.mLogFileName);
            JSONObject wrapper = new JSONObject();
            //wrapper.put("raw", ReaderController.mReadResultRaw);
            wrapper.put("recordsHashTable", ReaderController.mRecordsHashTable);

            file.write(wrapper.toString());
            file.flush();
        } catch (IOException e) {
            System.out.println("writeJSONToFile IO error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("writeJSONToFile assembling JSON error: " + e.getMessage());
        }
    }
    private void sendResult() {


    }
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();
        for (Tag t : tags) {
            String readingEpc = t.getEpc().toString().replace(" ", "").toLowerCase();
            String epc = readingEpc;
            Boolean isSlaveEpc = false;
            Long ts = PropertyUtils.getTimestamp();

            //
            if (mIsDebugMode) {
                JSONObject result = new JSONObject();
                try {
                    result.put("epc", epc);
                    result.put("timestamp", ts);
                    result.put("isSlave", isSlaveEpc);
                    result.put("antenna", t.getAntennaPortNumber());
                    //result.put("doppler", t.getRfDopplerFrequency());
                    //result.put("peakRssi", t.getPeakRssiInDbm());
                    //result.put("phase angel", t.getPhaseAngleInRadians());
                }  catch (Exception e) {
                    System.out.println("Assembling debug result error: " + e.getMessage());
                }
                System.out.println(result.toString());
            } else {
                JSONArray hashtableArray = new JSONArray();
                Integer lastValueIndex = -1;
                // Check if entries exist
                if (ReaderController.mRecordsHashTable.get(epc) != null) {
                    hashtableArray = (JSONArray) ReaderController.mRecordsHashTable.get(epc);
                    lastValueIndex = hashtableArray.size() - 1;
                }
                // Check if interval valid
                if (lastValueIndex < 0 || (ts - (Long) hashtableArray.get(lastValueIndex) >= ReaderController.mValidIntervalMs)) {
                    String print = "Valid epc: " + readingEpc + ". timestamp: " + ts;
                    try {
                        hashtableArray.add(ts);
                        ReaderController.mRecordsHashTable.put(epc, hashtableArray);

                    } catch (Exception e) {
                        System.out.println("Assembling result error: " + e.getMessage());
                    }
                    System.out.println(print);
                    writeJSONToFile(); // Write result to log file
                    sendResult(); // Post result to Database
                }
            }
        }
    }
}
