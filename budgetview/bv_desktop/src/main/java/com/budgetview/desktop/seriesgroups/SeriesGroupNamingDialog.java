package com.budgetview.desktop.seriesgroups;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.components.tips.ErrorTip;
import com.budgetview.desktop.components.tips.TipPosition;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import com.budgetview.utils.Lang;
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

public abstract class SeriesGroupNamingDialog {

  private String title;
  private String message;
  private PicsouDialog dialog;
  private JTextField nameField;

  private final LocalGlobRepository localRepository;
  private final Directory directory;
  private Key groupKey;

  protected SeriesGroupNamingDialog(String title, String message, GlobRepository parentRepository, Directory directory) {
    this.title = title;
    this.message = message;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(SeriesGroup.TYPE)
      .copy(Series.TYPE)
      .get();
    this.directory = directory;
  }

  protected void doShow() {

    groupKey = getGroupKey(localRepository);

    dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/seriesgroups/seriesGroupNamingDialog.splits",
                                                      localRepository, directory);

    builder.add("title", new JLabel(title));
    builder.add("message", GuiUtils.createReadOnlyHtmlComponent(message));

    OkAction okAction = new OkAction();

    nameField = builder.addEditor("nameField", SeriesGroup.NAME)
      .forceSelection(groupKey)
      .setNotifyOnKeyPressed(true)
      .setValidationAction(okAction)
      .getComponent();

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.pack();
    GuiUtils.selectAndRequestFocus(nameField);
    GuiUtils.showCentered(dialog);
  }

  protected abstract Key getGroupKey(LocalGlobRepository localRepository);

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
      processOk(groupKey, localRepository);
      localRepository.commitChanges(true);
      closeAndDispose();
    }
  }

  protected abstract void processOk(Key groupKey, LocalGlobRepository localRepository);

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
