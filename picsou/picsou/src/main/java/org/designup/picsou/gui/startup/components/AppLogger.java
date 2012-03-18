package org.designup.picsou.gui.startup.components;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.AppPaths;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.utils.Log;

import java.io.*;
import java.util.logging.LogManager;

public class AppLogger {

  public static void init() {
    try {
      System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
      String sout = System.getProperty(PicsouApplication.LOG_TO_SOUT);
      InputStream propertiesStream = PicsouApplication.class.getResourceAsStream("/logging.properties");
      LogManager.getLogManager().readConfiguration(propertiesStream);

      if ("true".equalsIgnoreCase(sout)) {
        return;
      }

      File logFilePath = new File(AppPaths.getDataPath() + "/" + "logs");
      logFilePath.mkdirs();
      File logFile = new File(logFilePath, "log.txt");
      if (logFile.exists() && logFile.length() > 2 * 1024 * 1024) {
        File oldFile = new File(logFilePath, "oldLog.txt");
        if (oldFile.exists()) {
          oldFile.delete();
        }
        logFile.renameTo(oldFile);
      }

      FileOutputStream stream = new FileOutputStream(logFile, true);
      PrintStream output = new PrintStream(stream);
      output.println("---------------------------");
      output.println(TimeService.getToday() + " - version: " + PicsouApplication.JAR_VERSION);
      output.println("---------------------------");
      Log.init(output);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static File getLogFile() {
    File logFilePath = new File(AppPaths.getDataPath() + "/" + "logs");
    logFilePath.mkdirs();
    return new File(logFilePath, "log.txt");
  }
}
