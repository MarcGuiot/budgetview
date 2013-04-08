package com.budgetview.android.datasync;

public interface DownloadCallback extends DataSyncCallback {
  void onDownloadFailed(String errorMessage);

  void onDownloadFailed(Integer errorId);
}
