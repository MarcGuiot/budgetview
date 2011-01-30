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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PreferencesDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private JComboBox futureMonth;
  private GlobsPanelBuilder builder;
  private JCheckBox multiplePlanned;
  private JComboBox periodComboBox;
  private JComboBox monthBackComboBox;

  public PreferencesDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;

    builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                    "/layout/general/preferencesDialog.splits",
                                                      repository, directory);
    Integer[] items= new Integer[]{12, 18, 24, 36};
    Utils.beginRemove();
    items = new Integer[]{0, 1, 2, 3, 4, 6, 12, 18, 24, 36};
    Utils.endRemove();
    futureMonth = new JComboBox(items);
    builder.add("futureMonth", futureMonth);
    multiplePlanned = new JCheckBox();
    builder.add("multiplePlanned", multiplePlanned);
    periodComboBox = new JComboBox(new Integer[]{4, 5, 6, 7});
    builder.add("period", periodComboBox);
    monthBackComboBox = new JComboBox(new Integer[]{1, 2, 3});
    builder.add("monthBack", monthBackComboBox);
    multiplePlanned.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        monthBackComboBox.setEnabled(multiplePlanned.isSelected());
        periodComboBox.setEnabled(multiplePlanned.isSelected());
      }
    });
    dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons((JPanel)builder.load(),
                               new AbstractAction(Lang.get("ok")) {
                                 public void actionPerformed(ActionEvent e) {
                                   repository.startChangeSet();
                                   try {
                                     Integer futureMonth =
                                       (Integer)PreferencesDialog.this.futureMonth.getSelectedItem();
                                     if (futureMonth != null) {
                                       repository.update(UserPreferences.KEY,
                                                         UserPreferences.FUTURE_MONTH_COUNT,
                                                         futureMonth);
                                     }
                                     repository.update(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED, multiplePlanned.isSelected());
                                     if (multiplePlanned.isSelected()){
                                       repository.update(UserPreferences.KEY,
                                                         UserPreferences.MONTH_FOR_PLANNED, monthBackComboBox.getSelectedItem());
                                       repository.update(UserPreferences.KEY,
                                                         UserPreferences.PERIOD_COUNT_FOR_PLANNED, periodComboBox.getSelectedItem());
                                     }
                                   }
                                   finally {
                                     repository.completeChangeSet();
                                   }
                                   dialog.setVisible(false);
                                 }
                               },
                               new CancelAction(dialog));
  }

  public void show() {
    Glob preference = repository.get(UserPreferences.KEY);
    futureMonth.setSelectedItem(preference.get(UserPreferences.FUTURE_MONTH_COUNT));
    Boolean isMultiPlanned = preference.get(UserPreferences.MULTIPLE_PLANNED);
    multiplePlanned.setSelected(isMultiPlanned);
    monthBackComboBox.setEnabled(isMultiPlanned);
    periodComboBox.setEnabled(isMultiPlanned);
    if (isMultiPlanned) {
      monthBackComboBox.setSelectedItem(preference.get(UserPreferences.MONTH_FOR_PLANNED));
      periodComboBox.setSelectedItem(preference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED));
    }
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }
}
