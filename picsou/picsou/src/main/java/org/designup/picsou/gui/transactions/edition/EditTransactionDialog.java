package org.designup.picsou.gui.transactions.edition;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobHtmlView;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class EditTransactionDialog {
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private PicsouDialog dialog;
  private GlobTextEditor labelEditor;
  private SelectionService selectionService;

  public EditTransactionDialog(GlobRepository localRepository, Directory directory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(localRepository).get();
    this.selectionService = new SelectionService();
    this.localDirectory = createDirectory(directory);
    createDialog(directory.get(JFrame.class));
  }

  private Directory createDirectory(Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    localDirectory.add(selectionService);
    return localDirectory;
  }

  private void createDialog(JFrame owner) {
    dialog = PicsouDialog.create(owner, true, localDirectory);
    OkAction okAction = new OkAction();

    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactions/editTransactionDialog.splits",
                            localRepository, localDirectory);

    labelEditor = GlobTextEditor.init(Transaction.LABEL, localRepository, localDirectory)
      .setMultiSelectionText("")
      .setValidationAction(okAction);
    builder.add("labelEditor", labelEditor.getComponent());

    builder.add("originalLabel",
                GlobHtmlView.init(Transaction.TYPE, localRepository, localDirectory,
                                  GlobListStringifiers.fieldValue(Transaction.ORIGINAL_LABEL, "",
                                                                  Lang.get("transaction.edition.originalLabel.multi"))));

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.pack();
  }

  public void show(GlobList transactions) {
    localRepository.rollback();
    localRepository.reset(transactions, Transaction.TYPE);
    selectionService.select(localRepository.getAll(Transaction.TYPE), Transaction.TYPE);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GuiUtils.selectAndRequestFocus(labelEditor.getComponent());
      }
    });
    GuiUtils.showCentered(dialog);
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      JTextField labelField = labelEditor.getComponent();
      if (Strings.isNullOrEmpty(labelField.getText())) {
        ErrorTip.showLeft(labelField,
                          Lang.get("transaction.edition.emptyLabelMessage"),
                          localDirectory);
        return;
      }

      labelField.setText(labelField.getText().toUpperCase());
      labelEditor.apply();
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {

    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      localRepository.rollback();
      dialog.setVisible(false);
    }
  }
}
