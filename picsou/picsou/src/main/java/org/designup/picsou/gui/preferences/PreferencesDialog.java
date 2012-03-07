package org.designup.picsou.gui.preferences;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.preferences.components.ColorThemeItemFactory;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.ColorTheme;
import org.designup.picsou.model.NumericDateType;
import org.designup.picsou.model.TextDateType;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PreferencesDialog {
  private PicsouDialog dialog;

  private LocalGlobRepository repository;

  private GlobsPanelBuilder builder;
  private ColorThemeItemFactory colorThemeFactory;
  private GlobRepository parentRepository;

  public PreferencesDialog(Window parent, final GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;
    this.repository = createLocalRepository(parentRepository);

    builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                    "/layout/general/preferencesDialog.splits",
                                    repository, directory);
    int[] items = new int[]{12, 18, 24, 36};
    Utils.beginRemove();
    items = new int[]{0, 1, 2, 3, 4, 6, 12, 18, 24, 36};
    Utils.endRemove();

    builder.addComboEditor("futureMonth", UserPreferences.KEY,
                           UserPreferences.FUTURE_MONTH_COUNT, items);

    colorThemeFactory = new ColorThemeItemFactory(repository, directory);

    builder.addRepeat("colorThemes", ColorTheme.TYPE, GlobMatchers.ALL, GlobComparators.ascending(ColorTheme.ID),
                      colorThemeFactory);

    builder.addComboEditor("textDate", UserPreferences.TEXT_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    builder.addComboEditor("numericDate", UserPreferences.NUMERIC_DATE_TYPE)
      .forceSelection(UserPreferences.KEY);

    dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons((JPanel)builder.load(), new OkAction(), new CancelAction());
  }

  private static LocalGlobRepository createLocalRepository(GlobRepository parentRepository) {
    return LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(UserPreferences.TYPE, ColorTheme.TYPE, NumericDateType.TYPE, TextDateType.TYPE)
      .get();
  }

  public void show() {
    colorThemeFactory.init();
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
      colorThemeFactory.complete(parentRepository);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      colorThemeFactory.complete(parentRepository);
      dialog.setVisible(false);
    }
  }
}
