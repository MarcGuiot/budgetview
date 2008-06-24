package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.MainPanel;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.components.DialogOwner;
import org.designup.picsou.gui.components.JWavePanel;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;

public class NewUserPanel {
  private JPanel panel;

  public static void show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    NewUserPanel panel = new NewUserPanel(repository, directory, mainWindow);
    mainWindow.setPanel(panel.panel);
  }

  private NewUserPanel(final GlobRepository repository, final Directory directory, final MainWindow mainWindow) {
    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
    builder.add("wave", new JWavePanel(directory.get(ColorService.class)));
    ImportPanel importPanel = new ImportPanel(Lang.get("login.skip"), Collections.<File>emptyList(), new DialogOwner() {
      public Window getOwner() {
        return mainWindow.getFrame();
      }
    }, repository, directory) {
      protected void complete() {
        MainPanel.show(repository, directory, mainWindow);
      }
    };
    builder.add("content", importPanel.getPanel());
    panel = (JPanel)builder.parse(getClass(), "/layout/newUserPanel.splits");
  }
}
