package org.designup.picsou.gui.about;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AboutDialog {

  private static final String[] SYSTEM_PROPERTIES = {
    "java.vm.vendor",
    "java.version",
    "java.vm.version",
    "java.runtime.version",
    "java.specification.version",
    "os.name",
    "os.version",
    "os.arch",
    "sun.arch.data.model",
    "file.encoding",
    "user.language"
  };

  private PicsouDialog dialog;
  private SplitsBuilder builder;

  public AboutDialog(Directory directory) {

    builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/aboutDialog.splits");

    String version = Lang.get("about.version", PicsouApplication.APPLICATION_VERSION);
    builder.add("versionLabel", new JLabel(version));

    builder.add("configurationArea", Gui.createHtmlEditor(getConfiguration()));

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.pack();
  }

  private String getConfiguration() {
    StringBuilder builder = new StringBuilder();
    builder.append("<html><body>");

    builder
      .append("<h2>").append(Lang.get("about.config.version")).append("</h2>")
      .append("<p>").append(PicsouApplication.APPLICATION_VERSION + " - " + PicsouApplication.JAR_VERSION).append("</p>");

    builder
      .append("<h2>").append(Lang.get("about.datapath")).append("</h2>")
      .append("<p>").append(PicsouApplication.getDataPath()).append("</p>");

    builder
      .append("<h2>").append(Lang.get("about.system.properties")).append("</h2>");
    for (String property : SYSTEM_PROPERTIES) {
      builder
        .append("<p>- <b>").append(property).append("</b>: ")
        .append(System.getProperty(property))
        .append("</p>");
    }

    builder.append("</body></html>");
    return builder.toString();
  }

  public void show() {
    GuiUtils.showCentered(dialog);
    builder.dispose();
  }
}
