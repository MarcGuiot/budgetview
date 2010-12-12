package org.designup.picsou.gui.feedback;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FeedbackDialog {
  private PicsouDialog dialog;
  private JTextField userMail;
  private JTextField mailSubject;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {

    dialog = PicsouDialog.create(parent, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/general/feedbackDialog.splits",
                                                      repository, directory);
    JEditorPane jEditorPane = new JEditorPane();
    builder.add("mailContent", jEditorPane);

    String userMail = repository.get(User.KEY).get(User.MAIL);

    this.userMail = new JTextField(userMail);
    builder.add("fromMail", this.userMail);

    this.mailSubject = new JTextField(Lang.get("feedback.default.subject"));
    builder.add("mailSubject", this.mailSubject);


    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(directory, jEditorPane),
                               new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
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
