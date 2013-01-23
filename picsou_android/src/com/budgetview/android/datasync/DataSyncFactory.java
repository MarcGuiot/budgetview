package com.budgetview.android.datasync;

import android.app.Activity;
import com.budgetview.android.datasync.https.HttpsDataSync;

public class DataSyncFactory {

  private static DataSync forcedSync;

  // For tests only
  public static void setForcedSync(DataSync forcedSync) {
    DataSyncFactory.forcedSync = forcedSync;
  }

  public static DataSync create(Activity activity) {
    if (forcedSync != null) {
      return forcedSync;
    }
    return new HttpsDataSync(activity);
  }
}
