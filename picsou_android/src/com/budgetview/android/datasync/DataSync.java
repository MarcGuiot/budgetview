package com.budgetview.android.datasync;

import java.io.*;

public interface DataSync {

  void load(String email, String password, DataSyncCallback callback);

  void loadDemoFile() throws IOException;

  boolean loadTempFile();

  void deleteTempFile();

  void sendDownloadEmail(String email, DataSyncCallback callback);

  boolean canConnect();
}
