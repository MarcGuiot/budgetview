package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.components.MandatoryFieldFlag;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.BankFormat;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class BankEditionDialog {
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private SelectionService selectionService;

  private PicsouDialog dialog;
  private JLabel title;
  private GlobTextEditor nameEditor;
  private MandatoryFieldFlag nameFlag;

  private Glob currentBank;
  private Key createdBankKey;

  public BankEditionDialog(Window owner, GlobRepository parentRepository, Directory directory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(Bank.TYPE, BankFormat.TYPE, BankEntity.TYPE)
        .get();
    this.selectionService = new SelectionService();
    this.localDirectory = createDirectory(directory);
    createDialog(owner);
  }

  private Directory createDirectory(Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    localDirectory.add(selectionService);
    return localDirectory;
  }

  private void createDialog(Window owner) {
    dialog = PicsouDialog.create(owner, true, localDirectory);
    OkAction okAction = new OkAction();

    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/bank/bankEditionDialog.splits",
                            localRepository, localDirectory);

    title = new JLabel();
    builder.add("title", title);

    nameEditor =
      GlobTextEditor.init(Bank.NAME, localRepository, localDirectory)
        .setValidationAction(okAction);
    builder.add("nameField", nameEditor.getComponent());

    nameFlag = new MandatoryFieldFlag("nameFlag", builder);
    localRepository.addChangeListener(new TypeChangeSetListener(Bank.TYPE) {
      protected void update(GlobRepository repository) {
        updateMandatoryFlag();
      }
    });

    builder.addEditor("urlField", Bank.URL);

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.pack();
  }

  public Key showNewBank() {
    localRepository.rollback();

    title.setText(Lang.get("bank.edition.title.create"));

    currentBank = localRepository.create(Bank.TYPE,
                                         value(Bank.USER_CREATED, true));
    createdBankKey = currentBank.getKey();
    localRepository.create(BankEntity.TYPE,
                           value(BankEntity.ID, getEntityId()),
                           value(BankEntity.BANK, currentBank.get(Bank.ID)));

    selectionService.select(currentBank);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GuiUtils.selectAndRequestFocus(nameEditor.getComponent());
      }
    });

    GuiUtils.showCentered(dialog);

    return createdBankKey;
  }

  private void updateMandatoryFlag() {
    if (currentBank != null && localRepository.contains(createdBankKey)) {
      nameFlag.update(Strings.isNullOrEmpty(currentBank.get(Bank.NAME)));
    }
    else {
      nameFlag.clear();
    }
  }

  private Integer getEntityId() {
    Set<Integer> ids = localRepository.getAll(BankEntity.TYPE).getValueSet(BankEntity.ID);
    int id = -1;
    while (ids.contains(id)) {
      id--;
    }
    return id;
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      JTextField nameField = nameEditor.getComponent();
      if (Strings.isNullOrEmpty(nameField.getText())) {
        ErrorTip.showLeft(nameField,
                          Lang.get("bank.edition.emptyNameMessage"),
                          localDirectory);
        return;
      }

      nameEditor.apply();
      for (Glob entity : localRepository.findLinkedTo(currentBank, BankEntity.BANK)) {
        localRepository.update(entity.getKey(),
                               value(BankEntity.LABEL, currentBank.get(Bank.NAME)),
                               value(BankEntity.URL, currentBank.get(Bank.URL)));
      }

      localRepository.commitChanges(false);

      currentBank = null;
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {

    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      localRepository.rollback();
      currentBank = null;
      createdBankKey = null;
      dialog.setVisible(false);
    }
  }
}
