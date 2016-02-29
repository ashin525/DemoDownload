package com.ashinqian.demodownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ashinqian.demodownload.entity.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by qiany on 2016/2/22.
 */
public class DownloadService extends Service {

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";

    public static final String ACTION_START = "ACTION_START";

    public static final String ACTION_STOP = "ACTION_STOP";

    public static final String ACTION_UPDATE = "ACTION_UPDATE";

    public static final int MSG_INIT = 0;

    private DownloadTask mTask = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d("test", "Init:" + fileInfo.toString());
                    //启动下载任务
                    mTask = new DownloadTask(DownloadService.this, fileInfo);
                    mTask.download();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得Activity传来的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d("test", "==Start" + fileInfo.toString());
            new InitThread(fileInfo).start();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d("test", "==Stop" + fileInfo.toString());
            if (mTask != null) {
                mTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class InitThread extends Thread{
        private FileInfo fileInfo;

        public InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(fileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3 * 1000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == 200) {
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                fileInfo.setLength(length);
                handler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
