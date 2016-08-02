package com.budgetview.desktop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryDesktopApp {
  public static void main(String[] args) throws Exception {
    System.setProperty(Application.IS_DATA_IN_MEMORY, "true");
    System.setProperty(Application.DISABLE_BACKUP, "true");
    List<String> strings = new ArrayList<String>(Arrays.asList(args));
    if (!strings.contains("-l")) {
      strings.addAll(Arrays.asList("-l", "fr"));
    }
    Application.main(strings.toArray(new String[strings.size()]));
  }
}
