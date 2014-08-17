package org.designup.picsou.gui.license.promotion;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.license.PremiumEvolution;
import org.designup.picsou.model.PremiumEvolutionState;
import org.globsframework.gui.GlobsPanelBuilder;
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

}
