package org.designup.picsou.gui.license;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
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
import org.globsframework.utils.Millis;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class LicenseInfoView extends View {
  private JEditorPane licenseMessage;
  private JButton askForKey;

  public LicenseInfoView(final GlobRepository repository, final Directory directory) {
    super(repository, directory);
    this.repository = repository;
    this.directory = directory;

    licenseMessage = new JEditorPane();
    GuiUtils.initReadOnlyHtmlComponent(licenseMessage);
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
  }

  private void update(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    Glob userPreferences = repository.find(UserPreferences.KEY);
    if (user == null || userPreferences == null) {
      return;
    }
    update(user, userPreferences);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("licenseMessage", licenseMessage);
  }

  private void update(Glob user, Glob userPreferences) {
    if (User.isDemoUser(user)) {
      licenseMessage.setText(Lang.get("demo.license.info.message"));
      licenseMessage.setVisible(true);
      return;
    }
    if (user.isTrue(User.IS_REGISTERED_USER)) {
      licenseMessage.setVisible(false);
      licenseMessage.setText("");
    }
    else {
      licenseMessage.setVisible(true);
      String message = null;
      long days =
        (userPreferences.get(UserPreferences.LAST_VALID_DAY).getTime() - TimeService.getToday().getTime()) / Millis.ONE_DAY;
      Integer state = user.get(User.ACTIVATION_STATE);
      if (days > 1) {
        message = Lang.get("license.info.day.count", days);
      }
      else if (days == 1) {
        message = Lang.get("license.info.one.day");
      }
      else if (days == 0) {
        message = Lang.get("license.info.last.day");
      }
      else if (user.get(User.MAIL) == null || state == null) {
        message = Lang.get("license.expiration.message");
      }

      String registerMessage = null;
      if (state != null) {
        if (state == User.ACTIVATION_FAILED_MAIL_SENT) {
          registerMessage = Lang.get("license.activation.failed.mailSent", user.get(User.MAIL));
        }
        else if (state == User.ACTIVATION_FAILED_CAN_NOT_CONNECT || state == User.ACTIVATION_FAILED_HTTP_REQUEST) {
          registerMessage = Lang.get("license.remote.connect");
        }
        else if (state == User.ACTIVATION_FAILED_MAIL_UNKNOWN){
          registerMessage = Lang.get("license.mail.unknown");
        }
        else if (state == User.ACTIVATION_FAILED_BAD_SIGNATURE){
          registerMessage = Lang.get("license.activation.failed");
        }
        else if (state == User.ACTIVATION_FAILED_MAIL_NOT_SENT){
          registerMessage = Lang.get("license.code.invalide");
        }
        else if (state == User.STARTUP_CHECK_KILL_USER){
          if (days < 0) {
            registerMessage = Lang.get("license.registered.user.killed");
          }
          else {
            registerMessage = Lang.get("license.registered.user.killed.trial");
          }
        }
        else if (state == User.STARTUP_CHECK_MAIL_SENT){
          if (days < 0) {
            registerMessage = Lang.get("license.registered.user.killed.mail.sent");
          }
          else {
            registerMessage = Lang.get("license.registered.user.killed.trial.mail.sent");
          }
        }
      }
      StringBuilder msg = new StringBuilder();
      msg.append("<html>");
      if (message != null) {
        msg.append(message);
      }
      if (registerMessage != null) {
        msg.append(registerMessage);
      }
      msg.append("</html>");
      licenseMessage.setText(msg.toString());
    }
  }
}