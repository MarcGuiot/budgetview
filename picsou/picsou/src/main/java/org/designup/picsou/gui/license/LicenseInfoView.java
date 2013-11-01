package org.designup.picsou.gui.license;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class LicenseInfoView extends View {
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
          dialog.show(false);
        }
        else if (href.equals("logout")) {
          directory.get(LogoutService.class).logout();
        }
      }
    });
    ApplicationColors.installLinkColor(licenseMessage, "licenseMessage", "notesView.introBlock.link", directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY) || changeSet.containsChanges(UserPreferences.KEY)) {
          update(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(repository);
      }
    });
    update(repository);
  }

  private void update(GlobRepository repository) {
    if (forceHidden) {
      licenseMessage.setVisible(false);
      return;
    }

    Glob user = repository.find(User.KEY);
    Glob userPreferences = repository.find(UserPreferences.KEY);
    if (user == null || userPreferences == null) {
      return;
    }
    if (User.isDemoUser(user)) {
      licenseMessage.setText(Lang.get("demo.license.info.message"));
      licenseMessage.setVisible(true);
      return;
    }

    if (user.isTrue(User.IS_REGISTERED_USER)) {
      licenseMessage.setVisible(false);
      licenseMessage.setText("");
      return;
    }

    Integer state = user.get(User.ACTIVATION_STATE);
    if (user.get(User.EMAIL) == null && state == null) {
      licenseMessage.setVisible(false);
      licenseMessage.setText("");
      return;
    }

    licenseMessage.setText(getMessage(user, state));
    licenseMessage.setVisible(true);
  }
  
  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/general/licenseInfoView.splits",
                                                      repository, directory);

    builder.add("licenseInfoMessage", licenseMessage);
    builder.add("hide", new HideAction());

    parentBuilder.add("licenseInfo", builder);
  }

  private String getMessage(Glob user, Integer state) {
    StringBuilder htmlText = new StringBuilder();
    htmlText.append("<html>");
    htmlText.append(getRegisterMessage(user, state));
    htmlText.append("</html>");
    return htmlText.toString();
  }

  private String getRegisterMessage(Glob user, Integer state) {
    if (state == null) {
      return "";
    }

    switch (state) {
      case User.ACTIVATION_FAILED_MAIL_SENT:
        return Lang.get("license.activation.failed.mailSent", user.get(User.EMAIL));

      case User.ACTIVATION_FAILED_CAN_NOT_CONNECT:
      case User.ACTIVATION_FAILED_HTTP_REQUEST:
        return Lang.get("license.remote.connect");

      case User.ACTIVATION_FAILED_MAIL_UNKNOWN: {
        return Lang.get("license.mail.unknown");
      }

      case User.ACTIVATION_FAILED_BAD_SIGNATURE: {
        return Lang.get("license.activation.failed");
      }

      case User.ACTIVATION_FAILED_MAIL_NOT_SENT: {
        return Lang.get("license.code.invalid");
      }

      case User.STARTUP_CHECK_KILL_USER: {
        return Lang.get("license.registered.user.killed");
      }

      case User.STARTUP_CHECK_MAIL_SENT: {
        return Lang.get("license.registered.user.killed.mail.sent");
      }

      case User.STARTUP_CHECK_JAR_VERSION: {
        return Lang.get("license.registered.user.killed.jar.updated.manually");
      }

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
      update(repository);
    }
  }

}