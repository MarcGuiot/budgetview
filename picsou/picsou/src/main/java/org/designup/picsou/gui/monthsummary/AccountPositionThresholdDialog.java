package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountPositionThresholdDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory directory;
  private GlobNumericEditor editor;

  public AccountPositionThresholdDialog(GlobRepository repository, Directory directory) {
    this.directory = directory;
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(AccountPositionThreshold.TYPE)
      .get();

    createDialog();
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountPositionThresholdDialog.splits",
                                                      localRepository, directory);


    OkAction okAction = new OkAction();

    editor = builder.addEditor("editor", AccountPositionThreshold.THRESHOLD)
      .setValidationAction(okAction)
      .setValueForNull(0.)
      .forceSelection(AccountPositionThreshold.KEY);

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CloseAction(dialog));
    dialog.setAutoFocusOnOpen(editor.getComponent());
    dialog.pack();
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      editor.apply();
      editor.dispose();
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }

  public void show() {
    dialog.showCentered();
  }
}
