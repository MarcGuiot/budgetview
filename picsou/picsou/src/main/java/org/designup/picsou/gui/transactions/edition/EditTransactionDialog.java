package org.designup.picsou.gui.transactions.edition;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.transactions.edition.utils.DateAndAmountPanel;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobHtmlView;
import org.globsframework.model.Glob;
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
  private SelectionService selectionService;

  private PicsouDialog dialog;
  private GlobTextEditor labelEditor;
  private JTextField labelField;
  private JEditorPane notice;
  private DateAndAmountPanel dateAndAmountPanel;

  public EditTransactionDialog(GlobRepository parentRepository, Directory directory) {
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository).get();
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

    final GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactions/editTransactionDialog.splits",
                            localRepository, localDirectory);

    labelEditor = GlobTextEditor.init(Transaction.LABEL, localRepository, localDirectory)
      .setMultiSelectionText("")
      .setValidationAction(okAction);
    labelField = labelEditor.getComponent();
    builder.add("labelEditor", labelField);

    builder.add("originalLabel",
                GlobHtmlView.init(Transaction.TYPE, localRepository, localDirectory,
                                  GlobListStringifiers.fieldValue(Transaction.ORIGINAL_LABEL, "",
                                                                  Lang.get("transaction.edition.originalLabel.multi"))));

    dateAndAmountPanel = new DateAndAmountPanel(dialog, localRepository, localDirectory);
    builder.add("dateAndAmount", dateAndAmountPanel.getPanel());

    notice = builder.add("notice", GuiUtils.createReadOnlyHtmlComponent()).getComponent();

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
        if ((transactions.size() == 1) && Transaction.isManuallyCreated(transactions.getFirst())) {
          notice.setVisible(false);
          dateAndAmountPanel.show(transactions);
          return;
        }

        dateAndAmountPanel.hide();
        notice.setVisible(true);
        for (Glob transaction : transactions) {
          if (!Transaction.isManuallyCreated(transaction)) {
            notice.setText(Lang.get("transaction.edition.notice.imported"));
            return;
          }
        }
        notice.setText(Lang.get("transaction.edition.notice.multi"));
      }
    }, Transaction.TYPE);

    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
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

      dateAndAmountPanel.apply();
      if (!checkValues()) {
        return;
      }

      labelField.setText(labelField.getText().toUpperCase());
      labelEditor.apply();
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }

    private boolean checkValues() {
      if (Strings.isNullOrEmpty(labelField.getText())) {
        ErrorTip.showLeft(labelField,
                          Lang.get("transaction.edition.emptyLabelMessage"),
                          localDirectory);
        return false;
      }

      return dateAndAmountPanel.validateFields();
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
