package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.components.JWavePanel;

import javax.swing.*;

public class NewUserPanel {
  private JPanel panel;

  public static void show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    NewUserPanel panel = new NewUserPanel(repository, directory, mainWindow);
    mainWindow.setPanel(panel.panel);
  }

  private NewUserPanel(final GlobRepository repository, final Directory directory, final MainWindow mainWindow) {
    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
    builder.add("wave", new JWavePanel(directory.get(ColorService.class)));
    ImportPanel importPanel = new ImportPanel(mainWindow.getFrame(), repository, directory) {
      protected void complete() {
        MainPanel.show(repository, directory, mainWindow);
      }
    };
    builder.add("content", importPanel.getPanel());
    panel = (JPanel)builder.parse(getClass(), "/layout/newUserPanel.splits");
  }
}
