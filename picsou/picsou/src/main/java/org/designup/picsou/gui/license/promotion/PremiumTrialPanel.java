package org.designup.picsou.gui.license.promotion;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.license.PremiumEvolution;
import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class PremiumTrialPanel extends View {
  private JPanel panel = new JPanel();

  public PremiumTrialPanel(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/license/promotion/premiumTrialPanel.splits",
                                                      repository, directory);

    builder.add("premiumTrialPanel", panel);
    builder.add("purchaseLicense", new PurchaseLicenseAction(directory));
    panel = builder.load();
    PremiumEvolution.addListener(repository, new PremiumEvolution.Listener() {
      public void processState(PremiumEvolutionState state) {
        panel.setVisible(state == PremiumEvolutionState.TRIAL_IN_PROGRESS);
      }
    });

    parentBuilder.add("premiumTrialPanel", builder);
  }

  private String getDaysLeftMessage(Glob user, long days, Integer state) {
    if (days > 1) {
      return Lang.get("license.info.day.count", days);
    }
    else if (days == 1) {
      return Lang.get("license.info.one.day");
    }
    else if (days == 0) {
      return Lang.get("license.info.last.day");
    }
    else if (licenseExpired(user, state)) {
      return Lang.get("license.expiration.message");
    }
    return "";
  }

  private boolean licenseExpired(Glob user, Integer state) {
    return user.get(User.EMAIL) == null || state == null;
  }

}
