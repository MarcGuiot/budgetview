package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PreferencesDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private JComboBox futureMonth;

  public PreferencesDialog(Window parent, final GlobRepository repository, Directory directory) {
    this.repository = repository;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/preferencesDialog.splits",
                                                      repository, directory);
    futureMonth = new JComboBox(new Integer[]{12, 18, 24, 36});
    builder.add("futureMonth", futureMonth);
    dialog = PicsouDialog.createWithButtons(parent, (JPanel)builder.load(),
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
                                            new AbstractAction(Lang.get("cancel")) {
                                              public void actionPerformed(ActionEvent e) {
                                                dialog.setVisible(false);
                                              }
                                            }, directory);
  }

  public void show() {
    Glob preference = repository.get(UserPreferences.KEY);
    futureMonth.setSelectedItem(preference.get(UserPreferences.FUTURE_MONTH_COUNT));
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }
}