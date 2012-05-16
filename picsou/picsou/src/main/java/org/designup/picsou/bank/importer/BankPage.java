package org.designup.picsou.bank.importer;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Date;

public abstract class BankPage {
  private Window parent;
  protected Directory directory;
  protected Integer bankId;
  protected LocalGlobRepository repository;
  protected PicsouDialog dialog;
  protected GlobList accounts = new GlobList();
//  protected InfiniteProgressPanel occupedPanel = new InfiniteProgressPanel();

  public BankPage(Window parent, Directory directory, GlobRepository repository, Integer bankId) {
    this.parent = parent;
    this.directory = directory;
    this.bankId = bankId;
    this.repository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, RealAccount.TYPE)
      .get();
  }

  public void init() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/synchroDialog.splits");
    builder.add("bankSpecific", getPanel());
    dialog = PicsouDialog.create(parent, directory);
    JPanel panel = builder.load();
    dialog.setPanelAndButton(panel, new CancelAction());
  }

  public abstract JPanel getPanel();

  protected void createOrUpdateRealAccount(String name, String number, String position, Date date, final Integer bankId) {
    if (Strings.isNotEmpty(name) || Strings.isNotEmpty(number)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       and(fieldEquals(RealAccount.NAME, name.trim()),
                                           fieldEquals(RealAccount.NUMBER, number.trim()),
                                           fieldEquals(RealAccount.BANK, bankId)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    value(RealAccount.NAME, name.trim()),
                                    value(RealAccount.NUMBER, number.trim()),
                                    value(RealAccount.BANK, bankId),
                                    value(RealAccount.POSITION_DATE, date),
                                    value(RealAccount.POSITION, position.trim()),
                                    value(RealAccount.FROM_SYNCHRO, true));
      }
      else {
        repository.update(account.getKey(),
                          value(RealAccount.POSITION_DATE, date),
                          value(RealAccount.POSITION, position.trim()));
      }
      accounts.add(account);
    }
  }

  protected void createOrUpdateRealAccount(String type, String number,
                                           final String url, String org, String fid) {
    if (Strings.isNotEmpty(number)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       and(fieldEquals(RealAccount.NUMBER, number),
                                           fieldEquals(RealAccount.ACC_TYPE, type),
                                           fieldEquals(RealAccount.URL, url),
                                           fieldEquals(RealAccount.ORG, org),
                                           fieldEquals(RealAccount.FID, fid)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    value(RealAccount.ACC_TYPE, type.trim()),
                                    value(RealAccount.NUMBER, number.trim()),
                                    value(RealAccount.URL, url),
                                    value(RealAccount.ORG, org),
                                    value(RealAccount.BANK, bankId),
                                    value(RealAccount.FID, fid),
                                    value(RealAccount.FROM_SYNCHRO, true));
      }
      else {
        repository.update(account.getKey(), RealAccount.FROM_SYNCHRO, Boolean.TRUE);
      }

      accounts.add(account);
    }
  }

  public static File createQifLocalFile(Glob realAccount, InputStream contentAsStream, String charset) {
    File file;
    try {
      file = File.createTempFile("download", ".qif");
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(("! accountId=" + realAccount.get(RealAccount.ID) + "\n").getBytes());
      Files.copyInUtf8(contentAsStream, charset, fileOutputStream);
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

  public void startOccuped() {
//    occupedPanel.start();
  }

  public void endOccuped() {
//    occupedPanel.stop();
  }

  protected class CancelAction extends AbstractAction {
    protected CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      accounts.clear();
      dialog.setVisible(false);
    }
  }

  public GlobList show() {
    dialog.pack();
    dialog.showCentered();
    return accounts;
  }
}
