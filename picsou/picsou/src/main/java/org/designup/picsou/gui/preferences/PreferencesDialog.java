package org.designup.picsou.gui.preferences;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PreferencesDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private JComboBox futureMonth;
  private GlobsPanelBuilder builder;

  public PreferencesDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;

    builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/preferencesDialog.splits",
                                                      repository, directory);
    Integer[] items= new Integer[]{12, 18, 24, 36};
    Utils.beginRemove();
    items = new Integer[]{0, 1, 2, 3, 4, 6, 12, 18, 24, 36};
    Utils.endRemove();
    futureMonth = new JComboBox(items);
    builder.add("futureMonth", futureMonth);
    dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons((JPanel)builder.load(),
                               new AbstractAction(Lang.get("ok")) {
                                 public void actionPerformed(ActionEvent e) {
                                   Integer futureMonth =
                                     (Integer)PreferencesDialog.this.futureMonth.getSelectedItem();
                                   if (futureMonth != null) {
                                     repository.update(UserPreferences.KEY,
                                                       UserPreferences.FUTURE_MONTH_COUNT,
                                                       futureMonth);
                                   }
                                   dialog.setVisible(false);
                                 }
                               },
                               new CancelAction(dialog));
  }

  public void show() {
    Glob preference = repository.get(UserPreferences.KEY);
    futureMonth.setSelectedItem(preference.get(UserPreferences.FUTURE_MONTH_COUNT));
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }
}
