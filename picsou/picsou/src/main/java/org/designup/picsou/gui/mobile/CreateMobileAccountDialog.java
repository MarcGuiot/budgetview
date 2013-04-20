package org.designup.picsou.gui.mobile;

import org.apache.wicket.markup.html.pages.PageExpiredErrorPage;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.components.utils.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.mobile.utils.AbstractMobileAccountDialog;
import org.designup.picsou.gui.mobile.utils.ConfirmMobileAccountPanel;
import org.designup.picsou.gui.mobile.utils.PasswordEditionPanel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CreateMobileAccountDialog extends AbstractMobileAccountDialog {

  private PicsouDialog dialog;
  private JEditorPane message;
  private ProgressPanel progressBar;
  private JTextField emailField;
  private CardHandler cards;
  private PasswordEditionPanel passwordEditionPanel;

  public CreateMobileAccountDialog(Directory directory, GlobRepository parentRepository) {
    super(parentRepository, directory);
    createDialog();
  }

  private void createDialog() {
    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);
    progressBar = new ProgressPanel();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/createMobileAccountDialog.splits",
                                                      localRepository, localDirectory);

    ActivateMobileAccountAction validateAction = new ActivateMobileAccountAction(dialog, progressBar);
    JButton createButton = new JButton(validateAction);
    builder.add("activateMobileAccount", createButton);

    cards = builder.addCardHandler("cards");

    emailField = builder.addEditor("emailField", UserPreferences.MAIL_FOR_MOBILE).getComponent();
    emailField.addActionListener(validateAction);

    passwordEditionPanel = new PasswordEditionPanel(localRepository, localDirectory);
    builder.add("passwordEdition", passwordEditionPanel.getPanel());

    message = Gui.createHtmlDisplay();
    builder.add("message", message);

    builder.add("progress", progressBar);

    builder.add("gotoGooglePlay", new BrowsingAction(localDirectory) {
      protected String getUrl() {
        return Lang.get("mobile.dialog.app.url");
      }
    });

    ConfirmMobileAccountPanel confirmPanel = new ConfirmMobileAccountPanel(localRepository, localDirectory);
    builder.add("confirmMobileAccount", confirmPanel.getPanel());

    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(emailField, createButton));
  }

  public void show() {
    Glob prefs = UserPreferences.get(localRepository);
    if (Strings.isNullOrEmpty(prefs.get(UserPreferences.PASSWORD_FOR_MOBILE))) {
      changePassword();
    }
    selectionService.select(prefs);
    dialog.pack();
    emailField.requestFocus();
    dialog.showCentered();
  }

  protected void showActivationConfirmation() {
    cards.show("confirmation");
  }

  protected void showErrorMessage(String errorMessage) {
    message.setText(errorMessage);
  }

  protected boolean checkFieldsAreValid() {
    String email = this.emailField.getText();
    if (Strings.isNullOrEmpty(email) || !email.contains("@")) {
      ErrorTip.show(this.emailField, Lang.get("mobile.mail.empty"), localDirectory, TipPosition.TOP_LEFT);
      return false;
    }
    return passwordEditionPanel.check();
  }
}
