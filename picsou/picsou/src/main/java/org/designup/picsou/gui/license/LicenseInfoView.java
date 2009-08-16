package org.designup.picsou.gui.license;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.help.HyperlinkHandler;
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
import org.globsframework.utils.Millis;

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
    PicsouColors.installLinkColor(licenseMessage, "licenseMessage", "notesView.introBlock.link", directory);

    Glob user = repository.get(User.KEY);
    Glob userPreferences = repository.get(UserPreferences.KEY);
    update(user, userPreferences);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          Glob user = repository.get(User.KEY);
          Glob userPreferences = repository.get(UserPreferences.KEY);
          update(user, userPreferences);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        Glob user = repository.get(User.KEY);
        Glob userPreferences = repository.get(UserPreferences.KEY);
        update(user, userPreferences);
      }
    });
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("licenseMessage", licenseMessage);
  }

  private void update(Glob user, Glob userPreferences) {
    if (user.get(User.IS_REGISTERED_USER)) {
      licenseMessage.setVisible(false);
    }
    else {
      if (User.isDemoUser(user)){
        licenseMessage.setText(Lang.get("demo.license.info.message"));
        licenseMessage.setVisible(true);
        return;
      }
      licenseMessage.setVisible(true);
      long days =
        (userPreferences.get(UserPreferences.LAST_VALID_DAY).getTime() - TimeService.getToday().getTime()) / Millis.ONE_DAY;
      if (days > 1) {
        licenseMessage.setText(Lang.get("license.info.day.count", days));
      }
      else if (days == 1) {
        licenseMessage.setText(Lang.get("license.info.one.day"));
      }
      else if (days == 0) {
        licenseMessage.setText(Lang.get("license.info.last.day"));
      }
      else {
        Integer state = user.get(User.ACTIVATION_STATE);
        if (state != null && state == User.ACTIVATION_FAILED_MAIL_SENT) {
          licenseMessage.setText(Lang.get("license.activation.faileded.mailSent", user.get(User.MAIL)));
        }
        else if (user.get(User.MAIL) == null) {
          licenseMessage.setText(Lang.get("license.expiration.message"));
        }
        else {
          licenseMessage.setText(Lang.get("license.registered.user.killed"));
        }
      }
    }
  }
}
