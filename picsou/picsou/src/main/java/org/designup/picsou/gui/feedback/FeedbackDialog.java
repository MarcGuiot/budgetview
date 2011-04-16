package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class FeedbackDialog {
  private PicsouDialog dialog;
  private JTextField userMail;
  private JTextField mailSubject;
  private ErrorTip errorTips = null;
  private ChangeSetListener tipsListener;
  private GlobRepository repository;
  private Directory directory;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.repository = repository;
    this.directory = directory;

    dialog = PicsouDialog.create(parent, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/feedbackDialog.splits",
                                                      repository, directory);
    JEditorPane jEditorPane = new JEditorPane();
    builder.add("mailContent", jEditorPane);

    String userMail = repository.get(User.KEY).get(User.MAIL);

    this.userMail = new JTextField(userMail);
    builder.add("fromMail", this.userMail);

    this.mailSubject = new JTextField();
    builder.add("mailSubject", this.mailSubject);

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(directory, jEditorPane),
                               new AbstractAction(Lang.get("cancel")) {
                                 public void actionPerformed(ActionEvent e) {
                                   dialog.setVisible(false);
                                 }
                               });
    tipsListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(User.CONNECTED)) {
          showConnection();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    };
    repository.addChangeListener(tipsListener);

    showConnection();
  }

  private void showConnection() {
    Glob user = repository.get(User.KEY);
    if (!user.isTrue(User.CONNECTED) && errorTips == null) {
      errorTips = ErrorTip.showLeft(dialog.getOkButton(), Lang.get("feedback.notConnected"), directory);
    }

    if (user.isTrue(User.CONNECTED) && errorTips != null) {
      errorTips.dispose();
    }
    
    dialog.getOkButton().setEnabled(user.isTrue(User.CONNECTED));
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
    repository.removeChangeListener(tipsListener);
  }

  private class ValidateAction extends AbstractAction {
    private final Directory directory;
    private final JEditorPane jEditorPane;

    public ValidateAction(Directory directory, JEditorPane jEditorPane) {
      super(Lang.get("feedback.send"));
      this.directory = directory;
      this.jEditorPane = jEditorPane;
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(ConfigService.class).sendMail(ConfigService.MAIL_CONTACT,
                                                  userMail.getText(),
                                                  mailSubject.getText(), jEditorPane.getText(),
                                                  new ConfigService.Listener() {
                                                    public void sent() {
                                                    }

                                                    public void sendFail() {
                                                    }
                                                  });
      dialog.setVisible(false);
    }
  }
}
