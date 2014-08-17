package org.designup.picsou.gui.license.promotion;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.gui.license.PremiumEvolution;
import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PremiumPromotionPanel extends View {

  private JPanel panel = new JPanel();

  public PremiumPromotionPanel(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/license/promotion/premiumPromotionPanel.splits",
                                                      repository, directory);

    builder.add("premiumPromotionPanel", panel);
    builder.add("activateTrial", new ActivateTrialAction());
    PremiumEvolution.addListener(repository, new PremiumEvolution.Listener() {
      public void processState(PremiumEvolutionState state) {
        panel.setVisible((state != PremiumEvolutionState.TRIAL_IN_PROGRESS) && !state.isRegistered());
      }
    });

    parentBuilder.add("premiumPromotionPanel", builder);
  }

  private class ActivateTrialAction extends AbstractAction {
    public ActivateTrialAction() {
      super(Lang.get("license.premium.promotion.activateTrial"));
    }

    public void actionPerformed(ActionEvent e) {
      LicenseService.activateTrial(repository);
    }
  }
}
