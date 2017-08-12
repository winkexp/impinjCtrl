package com.impinjCtrl;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.sun.istack.internal.NotNull;
import io.socket.client.IO;
import io.socket.client.Socket;

import io.socket.emitter.Emitter;
import lib.HttpClient;
import lib.PropertyUtils;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class ReaderController {

    public static final String EVENT_START_READER = "startreader";
    public static final String EVENT_TERMINATE_READER = "terminatereader";
    public static final String EVENT_TRANSFER_DATA = "rxdata";
    public static final String EVENT_GET_READER_STATUS = "getreaderstatus";
    public static final String EVENT_READER_STATUS = "readerstatus";
    public static String mEventId;
    public static String mSocketId;

    private boolean mIsDebugMode;

    private String mReaderHost;
    private String mApiHost;
    private JSONObject mMsg;
    private ImpinjReader mReader;
    private Socket mSocket;
    private HttpClient mHttpClient;
    private HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    private X509TrustManager mMyX509TrustManager = new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    };
    private final TrustManager[] trustAllCerts= new TrustManager[] {mMyX509TrustManager};
    private SSLContext mySSLContext;

    JSONParser parser = new JSONParser();


    ReaderController(@NotNull String readerHost) {
        this.mReaderHost = readerHost;
        this.mApiHost = PropertyUtils.getAPiHost();
        mIsDebugMode = PropertyUtils.isDebugMode();
    }

    public void initialize() {
        mMsg = new JSONObject();
        mReader = new ImpinjReader();

        if (mIsDebugMode) {
            initialReader();
        } else {

            mHttpClient = HttpClient.getInstance();
            System.out.println("Try to connect to socket: " + mApiHost);
            try {
                if (mApiHost.matches("^(https)://.*$")) {
                    mySSLContext = SSLContext.getInstance("TLS");
                    mySSLContext.init(null, trustAllCerts, null);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .hostnameVerifier(myHostnameVerifier)
                            .sslSocketFactory(mySSLContext.getSocketFactory(), mMyX509TrustManager)
                            .build();

// default settings for all sockets

                    IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
                    IO.setDefaultOkHttpCallFactory(okHttpClient);

// set as an option
                    IO.Options opts = new IO.Options();
                    opts.callFactory = okHttpClient;
                    opts.webSocketFactory = okHttpClient;
                    mSocket = IO.socket(mApiHost, opts);
                } else {
                    mSocket = IO.socket(mApiHost);
                }

                mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    public void call(Object... args) {
                        mSocketId = mSocket.id();
                        System.out.println("Connected to socket: " + mApiHost + ". socket ID: " + mSocketId);

                        // join / register id to socket io
                        Request req = new Request.Builder()
                                .url(mApiHost + "/api/socket/impinj?sid=" + mSocketId)
                                .build();
                        mHttpClient.request(req, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    initialReader();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).on(EVENT_START_READER, new Emitter.Listener() {
                    public void call(Object... args) {
                        System.out.println("start reader");
                        if (null == mReader) {
                            return;
                        }
                        try {
                            if (!mReader.isConnected()) {
                                initialReader();
                            }
                            String input = args[0].toString();
                            JSONObject inputJson = (JSONObject) parser.parse(input);
                            mEventId = inputJson.get("eventId").toString();
                            //System.out.println("mEventId: " + mEventId);
                            if (null == mEventId) {
                                System.out.println("Please specify the eventId in parameter");
                                return;
                            }
                            mReader.start();
                            mMsg.put("payload", ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader)));
                            mMsg.put("event", mEventId);
                            mMsg.put("type", EVENT_READER_STATUS);
                            System.out.println(mMsg.toJSONString());

                        } catch (OctaneSdkException ex) {
                            System.out.println(ex.getMessage());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            ex.printStackTrace(System.out);
                        }
                        Request sendMsg = new Request.Builder()
                                .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                                .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                                .build();

                        mHttpClient.request(sendMsg, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    ResponseBody body = response.body();
                                    body.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }).on(EVENT_GET_READER_STATUS, new Emitter.Listener() {
                    public void call(Object... args) {
                        System.out.println("get reader status");
                        if (null == mReader) {
                            return;
                        }
                        try {
                            // TODO: send back to api (control panel)
                            mMsg.put("payload", ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader)));
                            mMsg.put("event", mEventId);
                            mMsg.put("type", EVENT_READER_STATUS);
                            System.out.println(mMsg.toJSONString());
                        } catch (OctaneSdkException ex) {
                            System.out.println(ex.getMessage());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            ex.printStackTrace(System.out);
                        }


                        Request sendMsg = new Request.Builder()
                                .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                                .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                                .build();

                        mHttpClient.request(sendMsg, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    ResponseBody body = response.body();
                                    body.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }).on(EVENT_TERMINATE_READER, new Emitter.Listener() {
                    public void call(Object... args) {
                        System.out.println("stop reader");
                        if (null == mReader) {
                            return;
                        }

                        try {
                            if (mReader.isConnected()) {
                                mReader.stop();

                            }
                            mMsg.put("payload", ReaderSettings.getReaderInfo(mReader, ReaderSettings.getSettings(mReader)));
                            mMsg.put("event", mEventId);
                            mMsg.put("type", EVENT_READER_STATUS);
                            System.out.println(mMsg.toJSONString());

                        } catch (OctaneSdkException ex) {
                            System.out.println(ex.getMessage());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            ex.printStackTrace(System.out);
                        }
                        Request sendMsg = new Request.Builder()
                                .url(PropertyUtils.getAPiHost() + "/api/socket/impinj?sid=" + mSocketId)
                                .post(RequestBody.create(HttpClient.MEDIA_TYPE_JSON, mMsg.toJSONString()))
                                .build();

                        mHttpClient.request(sendMsg, new Callback() {
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    ResponseBody body = response.body();
                                    body.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    public void call(Object... args) {
                        System.out.println("socket disconnect");
                    }

                });

                mSocket.connect();

            } catch (URISyntaxException e) {
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
            } catch (NoSuchAlgorithmException E)  {

            } catch (KeyManagementException E) {

            }
        }
        mMsg.put("message", "Connecting");

    }

    private void initialReader() {
        if (null == mReader) {
            System.out.println("reader obj is null: initialReader");
            return;
        }

        try {
            checkReaderConnection();
            mReader.connect(mReaderHost);
            Settings settings = ReaderSettings.getSettings(mReader);
            mReader.setTagReportListener(new ReportFormat());
            mReader.applySettings(settings);

            // 後門
            Scanner s = new Scanner(System.in);
            while (s.hasNextLine() && mReader.isConnected()) {
                String line = s.nextLine();
                System.out.println(line);
                if (line.equals("START")) {
                    mReader.start();
                } else if (line.equals("STOP")) {
                    mReader.stop();
                    //break;
                } else if (line.equals("STATUS")) {
                    ReaderSettings.getReaderInfo(mReader, settings);
                }
            }

        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private void checkReaderConnection() {
        if (mReader.isConnected()) {
            try {
                mReader.removeTagReportListener();
                mReader.stop();
                mReader.disconnect();

            } catch (OctaneSdkException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    private void destory() {
        mSocket.disconnect();
        checkReaderConnection();
        mHttpClient.distory();

        mReader.removeTagReportListener();
        mSocket = null;
        mReader = null;
    }
}
