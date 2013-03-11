package com.budgetview.android.datasync;

import java.io.*;

public interface DataSync {

  void setUser(String email, String password);

  void load(DataSyncCallback callback);

  void loadDemoFile() throws IOException;

  boolean loadTempFile();

  boolean sendDownloadEmail(String email, DataSyncCallback callback);

}
