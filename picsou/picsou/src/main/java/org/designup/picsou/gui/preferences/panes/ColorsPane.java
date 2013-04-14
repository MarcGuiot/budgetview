package org.designup.picsou.gui.preferences.panes;

import org.designup.picsou.gui.preferences.PreferencesPane;
import org.designup.picsou.gui.preferences.PreferencesResult;
import org.designup.picsou.gui.preferences.components.ColorThemeItemFactory;
import org.designup.picsou.model.ColorTheme;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ColorsPane implements PreferencesPane {
  private final GlobRepository parentRepository;

  private JPanel panel;
  private ColorThemeItemFactory colorThemeFactory;

  public ColorsPane(GlobRepository parentRepository, GlobRepository localRepository, Directory directory) {
    this.parentRepository = parentRepository;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(ColorsPane.class,
                                                      "/layout/general/preferences/colorsPane.splits",
                                                      localRepository, directory);
    colorThemeFactory = new ColorThemeItemFactory(localRepository, directory);

    builder.addRepeat("colorThemes", ColorTheme.TYPE, GlobMatchers.ALL, GlobComparators.ascending(ColorTheme.ID),
                      colorThemeFactory);

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void prepareForDisplay() {
    colorThemeFactory.init();
  }

  public void validate(PreferencesResult result) {
  }

  public void postValidate() {
    colorThemeFactory.complete(parentRepository);
  }

  public void processCancel() {
    colorThemeFactory.complete(parentRepository);
  }
}
