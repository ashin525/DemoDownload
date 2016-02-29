package com.ashinqian.demodownload.services;

import android.content.Context;
import android.content.Intent;

import com.ashinqian.demodownload.db.ThreadDAO;
import com.ashinqian.demodownload.db.ThreadDAOImpl;
import com.ashinqian.demodownload.entity.FileInfo;
import com.ashinqian.demodownload.entity.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 * Created by qiany on 2016/2/23.
 */
public class DownloadTask {

    private Context mContext = null;

    private FileInfo mFileInfo = null;

    private ThreadDAO mDao = null;

    private int mFinished = 0;

    public boolean isPause = false;


    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程
        new DownloadThread(threadInfo).start();
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        public  void run() {
            //向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())){
                mDao.insertThread(mThreadInfo);
            }
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream is = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == 206) {
                    is = conn.getInputStream();
                    byte[] buffer = new byte[8 * 1024];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //下载暂停，保存进度
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    //删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    raf.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
