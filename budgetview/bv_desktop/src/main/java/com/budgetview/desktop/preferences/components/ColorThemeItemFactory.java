package com.budgetview.desktop.preferences.components;

import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.ColorTheme;
import com.budgetview.model.UserPreferences;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ColorThemeItemFactory implements RepeatComponentFactory<Glob> {

  private GlobRepository repository;
  private Directory directory;

  private ImageLocator imageLocator;
  private ButtonGroup masterGroup = new ButtonGroup();
  private Map<Integer, JToggleButton> toggleMap = new HashMap<Integer, JToggleButton>();
  private boolean selectionInProgress = false;

  public ColorThemeItemFactory(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;

    imageLocator = this.directory.get(ImageLocator.class);
    this.repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.COLOR_THEME)) {
          doUpdate();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(UserPreferences.TYPE)) {
          doUpdate();
        }
      }
    });
  }

  public void registerComponents(PanelBuilder cellBuilder, Glob colorTheme) {
    ColorTheme theme = ColorTheme.get(colorTheme);

    JToggleButton toggle = new JToggleButton(new ToggleAction(theme));

    ImageIcon icon = imageLocator.get(theme.getImagePath());
    toggle.setIcon(icon);
    toggle.setRolloverEnabled(true);
    Gui.configureIconButton(toggle, theme.name(), new Dimension(icon.getIconWidth(), icon.getIconHeight()));

    cellBuilder.add("themeToggle", toggle);
    masterGroup.add(toggle);
    toggleMap.put(theme.getId(), toggle);
  }

  private void doUpdate() {
    if (toggleMap.isEmpty() || selectionInProgress) {
      return;
    }
    Integer id = repository.get(UserPreferences.KEY).get(UserPreferences.COLOR_THEME);
    if (id == null) {
      return;
    }
    JToggleButton toggle = toggleMap.get(id);
    if (toggle == null) {
      throw new InvalidState("Could not find theme for id: " + id);
    }
    toggle.doClick(0);
  }

  public void init() {
    getCurrentToggle().doClick();
  }

  public void complete(GlobRepository parentRepository) {
    ColorThemeUpdater.apply(parentRepository, directory);
  }

  private JToggleButton getCurrentToggle() {
    Integer themeId = repository.get(UserPreferences.KEY).get(UserPreferences.COLOR_THEME);
    if (themeId == null) {
      return toggleMap.get(ColorTheme.STANDARD.getId());
    }
    if (!toggleMap.containsKey(themeId)) {
      throw new ItemNotFound("No toggle found for theme: " + themeId);
    }
    return toggleMap.get(themeId);
  }

  private class ToggleAction extends AbstractAction {
    private ColorTheme theme;

    public ToggleAction(ColorTheme theme) {
      this.theme = theme;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      try {
        selectionInProgress = true;
        repository.update(UserPreferences.KEY, UserPreferences.COLOR_THEME, theme.getId());
        complete(repository);
      }
      finally {
        selectionInProgress = false;
      }
    }
  }
}
