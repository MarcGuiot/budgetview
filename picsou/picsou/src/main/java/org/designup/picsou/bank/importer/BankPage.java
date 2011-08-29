package org.designup.picsou.bank.importer;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class BankPage {
  protected Directory directory;
  protected Integer bankId;
  protected LocalGlobRepository repository;
  protected PicsouDialog dialog;
  protected GlobList accounts = new GlobList();
  private CancelAction cancelAction;

  public BankPage(Directory directory, GlobRepository repository, Integer bankId) {
    this.directory = directory;
    this.bankId = bankId;
    this.repository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, RealAccount.TYPE)
      .get();
  }

  public void init() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/connection/synchroDialog.splits");
    builder.add("bankSpecific", getPanel());
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    JPanel panel = builder.load();
    cancelAction = new CancelAction();

    dialog.setPanelAndButton(panel, cancelAction);
  }

  abstract public JPanel getPanel();

  protected void createOrUpdateRealAccount(String name, String number, String position, final Integer bankId) {
    if (Strings.isNotEmpty(name) || Strings.isNotEmpty(number)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       GlobMatchers.and(GlobMatchers.fieldEquals(RealAccount.NAME, name.trim()),
                                                        GlobMatchers.fieldEquals(RealAccount.NUMBER, number.trim()),
                                                        GlobMatchers.fieldEquals(RealAccount.BANK, bankId)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    FieldValue.value(RealAccount.NAME, name.trim()),
                                    FieldValue.value(RealAccount.NUMBER, number.trim()),
                                    FieldValue.value(RealAccount.BANK, bankId),
                                    FieldValue.value(RealAccount.POSITION, position.trim()));
      }
      else {
        repository.update(account.getKey(), RealAccount.POSITION, position.trim());
      }
      accounts.add(account);
    }
  }

  protected void createOrUpdateRealAccount(String type, String number,
                                           final String url, String org, String fid) {
    if (Strings.isNotEmpty(number)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       GlobMatchers.and(GlobMatchers.fieldEquals(RealAccount.NUMBER, number),
                                                        GlobMatchers.fieldEquals(RealAccount.ACC_TYPE, type),
                                                        GlobMatchers.fieldEquals(RealAccount.URL, url),
                                                        GlobMatchers.fieldEquals(RealAccount.ORG, org),
                                                        GlobMatchers.fieldEquals(RealAccount.FID, fid)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    FieldValue.value(RealAccount.ACC_TYPE, type.trim()),
                                    FieldValue.value(RealAccount.NUMBER, number.trim()),
                                    FieldValue.value(RealAccount.URL, url),
                                    FieldValue.value(RealAccount.ORG, org),
                                    FieldValue.value(RealAccount.FID, fid));
      }
      accounts.add(account);
    }
  }

  public static File createQifLocalFile(Glob realAccount, InputStream contentAsStream) {
    File file;
    try {
      file = File.createTempFile("download", ".qif");
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(("! accountId=" + realAccount.get(RealAccount.ID) + "\n").getBytes());
      Files.copyStream(contentAsStream, fileOutputStream);
      file.deleteOnExit();
    }
    catch (IOException e) {
      Log.write("Can not create temporary file.");
      return null;
    }
    return file;
  }

  public abstract void loadFile();

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position);
  }

  public void doImport() {
    for (Glob account : accounts) {
      repository.update(account.getKey(), RealAccount.FILE_NAME, null);
    }
    loadFile();
    dialog.setVisible(false);
    repository.commitChanges(true);
  }

  protected class CancelAction extends AbstractAction {
    protected CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      accounts.clone();
      dialog.setVisible(false);
    }
  }

  public GlobList show() {
    dialog.pack();
    dialog.showCentered();
    return accounts;
  }
}
