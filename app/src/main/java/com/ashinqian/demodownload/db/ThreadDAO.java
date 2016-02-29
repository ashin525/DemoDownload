package com.ashinqian.demodownload.db;

import com.ashinqian.demodownload.entity.ThreadInfo;

import java.util.List;

/**
 * Created by qiany on 2016/2/23.
 */
public interface ThreadDAO {

    /**
     * 插入线程信息
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    public void deleteThread(String url, int thread_id);

    public void updateThread(String url, int thread_id, int finished);

    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param thread_id
     * @return
     */
    public boolean isExists(String url, int thread_id);

}
