package com.budgetview.desktop;

import com.budgetview.desktop.components.PicsouFrame;
import com.budgetview.desktop.components.layoutconfig.LayoutConfigService;
import com.budgetview.desktop.plaf.PicsouMacLookAndFeel;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class DevDesktopApp {

  static {
    PicsouMacLookAndFeel.initApplicationName();
    Gui.init();
  }

  public static void main(String... args) throws Exception {
    if (args.length > 1) {
      args = Application.parseLanguage(args);
    }
    List<String> arguments = new ArrayList<String>();
    arguments.addAll(Arrays.asList(args));
    String user = parseArguments(arguments, "-u", "user");
    String password = parseArguments(arguments, "-p", "pwd");
    String snapshot = parseArguments(arguments, "-s", null);
    run(user, password, snapshot);
  }

  public static Directory run(String user, String password, String snapshot) throws Exception {

    AppCore core = AppCore.init(user, password, snapshot).complete();
    Directory directory = core.getDirectory();

    final PicsouFrame frame = new PicsouFrame(Lang.get("application"), directory);
    directory.add(JFrame.class, frame);
    MainPanel.init(core.getRepository(), directory, new WindowManager() {
      public PicsouFrame getFrame() {
        return frame;
      }

      public void setPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.validate();
      }

      public void logout() {
        System.exit(1);
      }

      public void logOutAndDeleteUser(String name, char[] passwd) {
        System.exit(1);
      }

      public void logOutAndOpenDemo() {
        System.exit(1);
      }

      public void logOutAndAutoLogin() {
        System.exit(1);
      }

      public void shutdown() {
        System.exit(1);
      }
    })
      .prepareForDisplay();

    directory.get(LayoutConfigService.class).show(frame);

    return directory;
  }

  private static String parseArguments(List<String> args, String key, String defaultValue) {
    for (Iterator<String> it = args.iterator(); it.hasNext(); ) {
      String arg = it.next();
      if (key.equals(arg)) {
        it.remove();
        if (it.hasNext()) {
          String value = it.next();
          it.remove();
          return value;
        }
      }
    }
    return defaultValue;
  }
}
