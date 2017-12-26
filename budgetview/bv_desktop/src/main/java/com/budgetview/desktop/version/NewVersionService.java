package com.budgetview.desktop.version;

import com.budgetview.desktop.Application;
import com.budgetview.shared.license.LicenseAPI;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Log;
import org.json.JSONObject;

import java.io.IOException;

public class NewVersionService {

  public interface Listener {
    void update(boolean newVersionAvailable, String currentVersion, String newVersion);
  }

  public void update(final Listener listener) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        LicenseAPI api = new LicenseAPI();
        JSONObject desktopVersion = null;
        try {
          desktopVersion = api.getDesktopVersion();
        }
        catch (IOException e) {
          Log.write("Could not retrieve current Desktop version: " + e.getMessage());
        }

        final int jar = desktopVersion != null ? desktopVersion.optInt("jar", -1) : -1;
        final String newVersion = desktopVersion != null ? desktopVersion.optString("version", "") : "";
        final boolean newVersionAvailable = jar > Application.JAR_VERSION;
        GuiUtils.runInSwingThread(new Runnable() {
          public void run() {
            listener.update(newVersionAvailable, Application.APPLICATION_VERSION, newVersion);
          }
        });
      }
    });
    thread.start();
  }
}
