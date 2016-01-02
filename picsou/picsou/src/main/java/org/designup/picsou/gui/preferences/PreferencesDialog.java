package org.designup.picsou.gui.preferences;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.preferences.panes.ColorsPane;
import org.designup.picsou.gui.preferences.panes.ParametersPane;
import org.designup.picsou.gui.preferences.panes.StorageDirPane;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class PreferencesDialog {
  private PicsouDialog dialog;

  private LocalGlobRepository repository;

  private GlobsPanelBuilder builder;
  private Directory directory;
  private java.util.List<PreferencesPane> panes;

  public PreferencesDialog(Window parent, final GlobRepository parentRepository, final Directory directory) {
    this.directory = directory;
    this.repository = createLocalRepository(parentRepository);

    builder = new GlobsPanelBuilder(PreferencesDialog.class,
                                    "/layout/general/preferences/preferencesDialog.splits",
                                    repository, directory);

    dialog = PicsouDialog.create(this, parent, directory);

    PreferencesPane colorsPane = new ColorsPane(parentRepository, repository, directory);
    builder.add("colorsPane", colorsPane.getPanel());

    PreferencesPane parametersPane = new ParametersPane(repository, directory);
    builder.add("parametersPane", parametersPane.getPanel());

    PreferencesPane storageDirPane = new StorageDirPane(dialog, repository, directory);
    builder.add("dataPathPane", storageDirPane.getPanel());

    panes = Arrays.asList(colorsPane, parametersPane, storageDirPane);

    dialog.addPanelWithButtons((JPanel)builder.load(), new OkAction(), new CancelAction());
  }

  private static LocalGlobRepository createLocalRepository(GlobRepository parentRepository) {
    return LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(User.TYPE, UserPreferences.TYPE, ColorTheme.TYPE, NumericDateType.TYPE, TextDateType.TYPE)
      .get();
  }

  public void show() {
    for (PreferencesPane pane : panes) {
      pane.prepareForDisplay();
    }
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      repository.commitChanges(true);

      PreferencesResult result = new PreferencesResult();
      for (PreferencesPane pane : panes) {
        pane.validate(result);
      }
      if (!result.shouldClose()) {
        return;
      }
      for (PreferencesPane pane : panes) {
        pane.postValidate();
      }

      dialog.setVisible(false);
      if (result.shouldExit()) {
        JFrame frame = directory.get(JFrame.class);
        frame.setVisible(false);
        frame.dispose();
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      for (PreferencesPane pane : panes) {
        pane.processCancel();
      }
      dialog.setVisible(false);
    }
  }
}
