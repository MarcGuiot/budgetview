package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class CreateSeriesGroupDialog {

  private static GlobRepository parentRepository;
  private final Key seriesKey;
  private final BudgetArea budgetArea;
  private PicsouDialog dialog;
  private JTextField nameField;

  public static void show(Key seriesKey, GlobRepository repository, Directory directory) {
    parentRepository = repository;
    CreateSeriesGroupDialog dialog = new CreateSeriesGroupDialog(seriesKey, repository, directory);
    dialog.doShow();
  }

  private final LocalGlobRepository localRepository;
  private final Directory directory;
  private Key groupKey;

  private CreateSeriesGroupDialog(Key seriesKey, GlobRepository repository, Directory directory) {
    this.seriesKey = seriesKey;
    this.budgetArea = Series.getBudgetArea(repository.get(seriesKey));
    this.localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(SeriesGroup.TYPE)
      .get();
    this.directory = directory;
  }

  private void doShow() {

    groupKey = localRepository.create(SeriesGroup.TYPE,
                                      value(SeriesGroup.BUDGET_AREA, budgetArea.getId())).getKey();

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/seriesgroups/createSeriesGroupDialog.splits",
                                                      localRepository, directory);

    OkAction okAction = new OkAction();

    nameField = builder.addEditor("nameField", SeriesGroup.NAME)
      .forceSelection(groupKey)
      .setNotifyOnKeyPressed(true)
      .setValidationAction(okAction)
      .getComponent();

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.pack();
    nameField.requestFocus();
    GuiUtils.showCentered(dialog);
  }

  private class OkAction extends AbstractAction {
    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      Glob group = localRepository.get(groupKey);
      if (Strings.isNullOrEmpty(group.get(SeriesGroup.NAME))) {
        ErrorTip.show(nameField, Lang.get("seriesGroup.creation.name.error"), directory, TipPosition.BOTTOM_LEFT);
        return;
      }
      try {
        parentRepository.startChangeSet();
        localRepository.commitChanges(true);
        parentRepository.update(seriesKey, Series.GROUP, groupKey.get(SeriesGroup.ID));
      }
      finally {
        parentRepository.completeChangeSet();
      }
      closeAndDispose();
    }
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.dispose();
      closeAndDispose();
    }
  }

  private void closeAndDispose() {
    dialog.setVisible(false);
    dialog.dispose();
  }
}
