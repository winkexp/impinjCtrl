package com.impinjCtrl;

import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.AntennaConfig;
import com.impinj.octane.FeatureSet;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Status;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import lib.PropertyUtils;


public class ReaderSettings {
    public static Settings getSettings (ImpinjReader reader) throws OctaneSdkException {
        Settings settings = reader.queryDefaultSettings();
        Boolean debugMode = PropertyUtils.isDebugMode();

        ReportConfig report = settings.getReport();
        report.setIncludeFirstSeenTime(true);
        report.setMode(ReportMode.Individual);
        // The reader can be set into various modes in which reader
        // dynamics are optimized for specific regions and environments.
        // The following mode, AutoSetDenseReader, monitors RF noise and interference and then automatically
        // and continuously optimizes the reader’s configuration
        settings.setReaderMode(ReaderMode.AutoSetDenseReader);

        //Search mode determines how reader change tags' state, or how frequent a tag is reported when in sensor field
        // https://support.impinj.com/hc/en-us/articles/202756158-Understanding-EPC-Gen2-Search-Modes-and-Sessions
        // https://support.impinj.com/hc/en-us/articles/202756368-Optimizing-Tag-Throughput-Using-ReaderMode
        // TagFocus uses Singletarget session 1 with fewer reports when in sensor field
        // Race timing recommendation: session 1
        // http://racetiming.wimsey.co/2015/05/rfid-inventory-search-modes.html
        settings.setSearchMode(SearchMode.DualTarget);
        //settings.setSearchMode(SearchMode.SingleTarget);
        //settings.setSearchMode(SearchMode.TagFocus);
        settings.setSession(2);

        if (debugMode) {
            report.setIncludeAntennaPortNumber(true);
            report.setIncludeChannel(true);
            report.setIncludeCrc(true);
            report.setIncludePeakRssi(true);
            report.setIncludePhaseAngle(true);
        }

        // set some special settings for antennas
        AntennaConfigGroup antennas = settings.getAntennas();
        antennas.disableAll();

        for (short i = 1; i <= 4; i++) {
            //antennas.enableById(new short[]{i});
            // Define reader range

            // winkexp: 取消天线功率最大
            //antennas.getAntenna(i).setIsMaxRxSensitivity(true);
            //antennas.getAntenna(i).setIsMaxTxPower(true);

            antennas.getAntenna(i).setIsMaxRxSensitivity(false);
            antennas.getAntenna(i).setIsMaxTxPower(false);
            antennas.getAntenna(i).setTxPowerinDbm(Properties.antennaTxPowerinDbm);
            antennas.getAntenna(i).setRxSensitivityinDbm(Properties.rxSensitivityinDbm);
        }
        antennas.enableAll();
        return settings;
    }
    public static JSONObject getReaderInfo (ImpinjReader reader, Settings settings) throws OctaneSdkException {

        JSONObject result = new JSONObject();

        FeatureSet features = reader.queryFeatureSet();
        Status status = reader.queryStatus();

        result.put("modelName", features.getModelName());
        result.put("modelNumber", features.getModelNumber());
        result.put("antennaCount", features.getAntennaCount());
        result.put("isSingulating", status.getIsSingulating());
        result.put("temperature", status.getTemperatureCelsius());
        result.put("readerMode", settings.getReaderMode().toString());
        result.put("searchMode", settings.getSearchMode().toString());
        result.put("session", settings.getSession());


        ArrayList<AntennaConfig> ac = settings.getAntennas().getAntennaConfigs();

        Boolean isMaxRxSensitivity = ac.get(0).getIsMaxRxSensitivity();
        Boolean isMaxTxPower = ac.get(0).getIsMaxTxPower();
        String rxSensitivity = "max";
        String txPower = "max";
        if (!isMaxRxSensitivity) {
            rxSensitivity = String.valueOf(ac.get(0).getRxSensitivityinDbm()) + "Dbm";
        }
        if (!isMaxTxPower) {
            txPower = String.valueOf(ac.get(0).getTxPowerinDbm()) + "Dbm";
        }
        result.put("getRxSensitivityinDbm", rxSensitivity);
        result.put("getTxPowerinDbm", txPower);

        System.out.print(">>> Reader STATUS in JSON Format: ");
        System.out.println(result.toJSONString());
        return result;
    }
}
