package com.budgetview.android.datasync;

import java.io.*;

public interface DataSync {

  void load(String email, String password, DataSyncCallback callback);

  void loadDemoFile() throws IOException;

  boolean loadTempFile();

  boolean sendDownloadEmail(String email, DataSyncCallback callback);

}
