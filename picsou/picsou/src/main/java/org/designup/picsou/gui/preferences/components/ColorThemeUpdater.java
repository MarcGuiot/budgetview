package org.designup.picsou.gui.preferences.components;

import org.designup.picsou.model.ColorTheme;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.parameters.ConfiguredPropertiesService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class ColorThemeUpdater {

  public static void register(final GlobRepository repository, final Directory directory) {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.COLOR_THEME)) {
          apply(repository, directory);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(UserPreferences.TYPE)) {
          apply(repository, directory);
        }
      }
    });
    apply(repository, directory);
  }

  public static void apply(GlobRepository repository, Directory directory) {
    ColorTheme theme = getCurrentTheme(repository);
    directory.get(ColorService.class).setCurrentSet(theme.getColorFileName());
    directory.get(ConfiguredPropertiesService.class)
      .apply(Files.loadProperties(ColorThemeUpdater.class, "/" + theme.getThemeFilePath()));
    JFrame frame = directory.find(JFrame.class);
    if (frame != null) {
      frame.repaint();
    }
  }

  private static ColorTheme getCurrentTheme(GlobRepository repository) {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return ColorTheme.STANDARD;
    }
    Integer themeId = preferences.get(UserPreferences.COLOR_THEME);
    return ColorTheme.get(themeId);
  }
}
