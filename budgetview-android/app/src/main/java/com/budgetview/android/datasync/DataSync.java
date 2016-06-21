package com.budgetview.android.datasync;

import java.io.IOException;

public interface DataSync {

    void load(String email, String password, DownloadCallback callback);

    void loadDemoFile() throws IOException;

    boolean loadTempFile();

    void deleteTempFile();

    void sendDownloadEmail(String email, DataSyncCallback callback);

    boolean canConnect();
}
