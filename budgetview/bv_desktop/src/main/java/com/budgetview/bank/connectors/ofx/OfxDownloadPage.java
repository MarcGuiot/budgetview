package com.budgetview.bank.connectors.ofx;

import com.budgetview.io.importer.ofx.OfxConnection;
import com.budgetview.model.RealAccount;
import com.budgetview.bank.connectors.AbstractBankConnector;
import com.budgetview.gui.importer.components.OfxSecurityInfoButton;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.components.ShowHideButton;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class OfxDownloadPage extends AbstractBankConnector {
  private JTextField codeField;
  private JPasswordField passwordField;
  private JTextField orgField;
  private JTextField fidField;
  private JTextField urlField;
  private String url;
  private String org;
  private String fid;
  private OfxDownloadPage.ValidateAction validateAction;
  private String uuid = UUID.randomUUID().toString();
  private boolean v2 = false;


  public OfxDownloadPage(GlobRepository repository, Directory directory, Integer bankId, String url, String org, String fid, Glob synchro) {
    super(bankId, repository, directory, synchro);
    this.url = url;
    this.org = org;
    this.fid = fid;
  }

  public String getCurrentLocation() {
    return url;
  }

  protected JPanel createPanel() {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/ofxDownloadPage.splits");

    codeField = new JTextField();
    codeField.setName("code");
    builder.add(codeField);

    codeField.setText(getSyncCode());

    passwordField = new JPasswordField();
    passwordField.setName("password");
    builder.add(passwordField);

    urlField = new JTextField(url);
    builder.add("url", urlField);

    orgField = new JTextField(org);
    builder.add("org", orgField);

    fidField = new JTextField(fid);
    builder.add("fid", fidField);

    validateAction = new ValidateAction();
    builder.add("validate", validateAction);

    builder.add("securityInfo", OfxSecurityInfoButton.create(directory));

    JPanel detailsPanel = new JPanel();
    detailsPanel.setVisible(false);
    builder.add("detailsPanel", detailsPanel);

    builder.add("showDetails", new ShowHideButton(detailsPanel,
                                                  Lang.get("synchro.ofx.showDetails"),
                                                  Lang.get("synchro.ofx.hideDetails")));

    addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });

    return builder.load();
  }

  public void panelShown() {
    codeField.requestFocus();
  }

  public void reset() {
    // TODO: remettre en etat apres exception
  }

  public void downloadFile() throws Exception {
    for (Glob account : this.accounts) {
      try {
        String s = OfxConnection.getInstance().loadOperation(account, OfxConnection.previousDate(120), codeField.getText(), new String(passwordField.getPassword()),
                                                             urlField.getText(), orgField.getText(), fidField.getText(), uuid, v2);
        localRepository.update(account.getKey(), RealAccount.FILE_CONTENT, s);
      }
      catch (IOException e) {
        notifyErrorFound(e);
      }
    }
  }

  public String getCode() {
    return codeField.getText();
  }

  public void stop() {
  }

  protected void notifyDownloadInProgress() {
    super.notifyDownloadInProgress();
    validateAction.setEnabled(false);
  }

  protected void notifyWaitingForUser() {
    super.notifyWaitingForUser();
    validateAction.setEnabled(true);
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("synchro.ofx.validate"));
    }

    public void actionPerformed(ActionEvent e) {
      notifyDownloadInProgress();
      directory.get(ExecutorService.class)
        .submit(new Runnable() {
          public void run() {
            try {
              preload();
            }
            catch (final RuntimeException exception) {
              v2 = true;
              try {
                preload();
                return;
              }
              catch (Exception e1) {
              }
              notifyErrorFound(exception);
            }
          }
        });
    }
  }

  private void preload() {
    List<OfxConnection.AccountInfo> list = OfxConnection.getInstance()
      .getAccounts(codeField.getText(), new String(passwordField.getPassword()), OfxConnection.current(),
                   urlField.getText(), orgField.getText(), fidField.getText(), uuid, v2);

    for (OfxConnection.AccountInfo info : list) {
      createOrUpdateRealAccount(info.accType, info.number, urlField.getText(), orgField.getText(), fidField.getText());
    }
    doImport();
  }
}
