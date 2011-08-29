package org.designup.picsou.bank.importer.ofx;

import org.designup.picsou.bank.importer.BankPage;
import org.designup.picsou.importer.ofx.OfxConnection;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OfxDownloadPage extends BankPage {
  private JTextField code;
  private JButton validerCode;
  private JPasswordField passwordTextField;
  private JTextField orgTextField;
  private JTextField fidTextField;
  private JTextField urlTextField;
  private String url;
  private String org;
  private String fid;

  public OfxDownloadPage(GlobRepository repository, Directory directory, Integer bankId, String url, String org, String fid) {
    super(directory, repository, bankId);
    this.url = url;
    this.org = org;
    this.fid = fid;
  }

  public JPanel getPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/connection/ofxPanel.splits");
    code = new JTextField();
    code.setName("code");
    builder.add(code);
    validerCode = new JButton("valider");
    validerCode.setName("validerCode");
    builder.add(validerCode);
    validerCode.addActionListener(new ValiderActionListener());
    passwordTextField = new JPasswordField();
    passwordTextField.setName("password");
    builder.add(passwordTextField);
    urlTextField = new JTextField(url);
    builder.add("url", urlTextField);
    orgTextField = new JTextField(org);
    builder.add("org", orgTextField);
    fidTextField = new JTextField(fid);
    builder.add("fid", fidTextField);
    return builder.load();
  }

  public void loadFile() {
    for (Glob glob : this.accounts) {
      if (glob.get(RealAccount.IMPORTED)) {
        try {
          File file = File.createTempFile("download", ".ofx");
          OfxConnection.loadOperation(glob, OfxConnection.previousDate(120), code.getText(), new String(passwordTextField.getPassword()),
                                      urlTextField.getText(), orgTextField.getText(), fidTextField.getText(), file);
          file.deleteOnExit();
          repository.update(glob.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }

      }
    }
  }

  private class ValiderActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      List<OfxConnection.AccountInfo> list = OfxConnection.getAccounts(code.getText(), new String(passwordTextField.getPassword()),
                                                                       urlTextField.getText(), orgTextField.getText(), fidTextField.getText(), OfxDownloadPage.this.repository);

      for (OfxConnection.AccountInfo info : list) {
        createOrUpdateRealAccount(info.accType, info.number, urlTextField.getText(), orgTextField.getText(), fidTextField.getText());
      }
      doImport();
    }
  }
}
