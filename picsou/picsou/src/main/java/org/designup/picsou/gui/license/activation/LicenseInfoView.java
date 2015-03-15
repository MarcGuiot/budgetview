package org.designup.picsou.gui.license.activation;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.LicenseActivationState;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LicenseInfoView extends View {
  private JPanel panel = new JPanel();
  private JEditorPane licenseMessage;
  private boolean forceHidden;

  public LicenseInfoView(final GlobRepository repository, final Directory directory) {
    super(repository, directory);
    this.repository = repository;
    this.directory = directory;

    licenseMessage = GuiUtils.createReadOnlyHtmlComponent();
    licenseMessage.setVisible(false);

    final JFrame parent = directory.get(JFrame.class);
    licenseMessage.addHyperlinkListener(new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        if (href.equals("newLicense")) {
          LicenseExpirationDialog dialog =
            new LicenseExpirationDialog(parent, repository, directory);
          dialog.show();
        }
        else if (href.equals("activateKey")) {
          LicenseActivationDialog dialog = new LicenseActivationDialog(parent, repository, directory);
          dialog.show();
        }
        else if (href.equals("logout")) {
          directory.get(LogoutService.class).logout();
        }
      }
    });
    ApplicationColors.installLinkColor(licenseMessage, "licenseMessage", "notesView.introBlock.link", directory);

    repository.addChangeListener(new KeyChangeListener(User.KEY) {
      public void update() {
        updateMessage(repository);
      }
    });
    updateMessage(repository);
  }

  private void updateMessage(GlobRepository repository) {
    if (forceHidden) {
      licenseMessage.setVisible(false);
      return;
    }

    Glob user = repository.find(User.KEY);
    if ((user == null) || User.isDemoUser(user)) {
      licenseMessage.setVisible(false);
      return;
    }

    showMessage(getRegistrationMessage(user));
  }

  private void showMessage(String message) {
    if (Strings.isNullOrEmpty(message)) {
      licenseMessage.setText("");
      licenseMessage.setVisible(false);
    }
    else {
      licenseMessage.setText("<html>" + message + "</message>");
      licenseMessage.setVisible(true);
    }
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/license/activation/licenseInfoView.splits",
                                                      repository, directory);

    builder.add("licenseInfoView", panel);
    builder.add("licenseInfoMessage", licenseMessage);
    builder.add("hide", new HideAction());

    parentBuilder.add("licenseInfoView", builder);
  }

  private String getRegistrationMessage(Glob user) {

    if (user.isTrue(User.IS_REGISTERED_USER)) {
      return Lang.get("license.activation.ok", user.get(User.EMAIL));
    }

    LicenseActivationState activationState = LicenseActivationState.get(user.get(User.LICENSE_ACTIVATION_STATE));
    if (activationState == null) {
      return "";
    }

    switch (activationState) {

      case ACTIVATION_IN_PROGRESS:
        return Lang.get("license.activation.inProgress", user.get(User.EMAIL));

      case ACTIVATION_FAILED_MAIL_SENT:
        return Lang.get("license.activation.failed.mailSent", user.get(User.EMAIL));

      case ACTIVATION_FAILED_CAN_NOT_CONNECT:
      case ACTIVATION_FAILED_HTTP_REQUEST:
        return Lang.get("license.remote.connect");

      case ACTIVATION_FAILED_MAIL_UNKNOWN:
        return Lang.get("license.mail.unknown");

      case ACTIVATION_FAILED_BAD_SIGNATURE:
        return Lang.get("license.activation.failed");

      case ACTIVATION_FAILED_MAIL_NOT_SENT:
        return Lang.get("license.code.invalid");

      case STARTUP_CHECK_KILL_USER:
        return Lang.get("license.registered.user.killed");

      case STARTUP_CHECK_MAIL_SENT:
        return Lang.get("license.registered.user.killed.mail.sent");

      case STARTUP_CHECK_JAR_VERSION:
        return Lang.get("license.registered.user.killed.jar.updated.manually");

      default:
        return "";
    }
  }

  private class HideAction extends AbstractAction {

    private HideAction() {
      super(Lang.get("footerBanner.hide"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      forceHidden = true;
      updateMessage(repository);
    }
  }

}