package org.designup.picsou.bank.connectors.ofx;

import org.designup.picsou.bank.connectors.AbstractBankConnector;
import org.designup.picsou.gui.importer.components.OfxSecurityInfoButton;
import org.designup.picsou.importer.ofx.OfxConnection;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.utils.ShowHideButton;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

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

  public OfxDownloadPage(GlobRepository repository, Directory directory, Integer bankId, String url, String org, String fid) {
    super(bankId, repository, directory);
    this.url = url;
    this.org = org;
    this.fid = fid;
  }

  public String getCurrentLocation() {
    return url;
  }

  protected JPanel createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/ofxDownloadPage.splits");

    codeField = new JTextField();
    codeField.setName("code");
    builder.add(codeField);

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
        File file = File.createTempFile("download", ".ofx");
        OfxConnection.getInstance().loadOperation(account, OfxConnection.previousDate(120), codeField.getText(), new String(passwordField.getPassword()),
                                                  urlField.getText(), orgField.getText(), fidField.getText(), file);
        file.deleteOnExit();
        repository.update(account.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
      }
      catch (IOException e) {
        notifyErrorFound(e);
      }
    }
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
      Thread thread = new Thread(new Runnable() {
        public void run() {
          try {
            List<OfxConnection.AccountInfo> list = OfxConnection.getInstance()
              .getAccounts(codeField.getText(), new String(passwordField.getPassword()), OfxConnection.previousDate(1),
                           urlField.getText(), orgField.getText(), fidField.getText());

            for (OfxConnection.AccountInfo info : list) {
              createOrUpdateRealAccount(info.accType, info.number, urlField.getText(), orgField.getText(), fidField.getText());
            }
            doImport();
          }
          catch (RuntimeException exception) {
            notifyErrorFound(exception);
          }
        }
      });
      thread.start();
    }
  }
}
