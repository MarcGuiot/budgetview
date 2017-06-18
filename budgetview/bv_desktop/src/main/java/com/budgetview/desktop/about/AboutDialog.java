package com.budgetview.desktop.about;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.browsing.BrowsingService;
import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.help.actions.GotoWebsiteAction;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.InputStream;

public class AboutDialog {

  public static final String[] SYSTEM_PROPERTIES = {
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
  private Directory directory;

  public AboutDialog(Directory directory) {

    this.directory = directory;

    builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/general/aboutDialog.splits");

    String version = Lang.get("about.version", Application.APPLICATION_VERSION);
    builder.add("versionLabel", new JLabel(version));
    builder.add("website", new GotoWebsiteAction(Lang.get("site.url"), directory));

    builder.add("configurationArea", Gui.createHtmlEditor(getConfiguration()));

    builder.add("licensesArea", createLicensesArea());

    builder.add("slaArea", createSlaArea());

    dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }

  private JEditorPane createLicensesArea() {

    JEditorPane editor = Gui.createHtmlEditor(loadLicensesContent());
    editor.addHyperlinkListener(new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        directory.get(BrowsingService.class).launchBrowser(href);
      }
    });
    return editor;
  }

  private String loadLicensesContent() {
    InputStream stream = Lang.class.getResourceAsStream("/docs/licenses.html");
    if (stream == null) {
      return "Unable to load licenses file";
    }
    return Files.loadStreamToString(stream, "UTF-8");
  }

  private JEditorPane createSlaArea() {
    return Gui.createHtmlEditor(Lang.getDocFile("sla.html"));
  }

  private String getConfiguration() {
    StringBuilder builder = new StringBuilder();
    builder.append("<html><body>");

    builder
      .append("<h2>").append(Lang.get("about.config.version")).append("</h2>")
      .append("<p>").append(Application.APPLICATION_VERSION + " - " + Application.JAR_VERSION).append("</p>");

    builder
      .append("<h2>").append(Lang.get("about.datapath")).append("</h2>")
      .append("<p>").append(AppPaths.getCurrentDataPath()).append("</p>");

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
