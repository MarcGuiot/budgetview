package org.designup.picsou.gui.license.registered;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.license.PremiumEvolution;
import org.designup.picsou.model.PremiumEvolutionState;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class PremiumRegisteredPanel extends View {
  private JPanel panel = new JPanel();

  public PremiumRegisteredPanel(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/license/registered/premiumRegisteredPanel.splits",
                                                      repository, directory);

    builder.add("premiumRegisteredPanel", panel);
    PremiumEvolution.addListener(repository, new PremiumEvolution.Listener() {
      public void processState(PremiumEvolutionState state) {
        panel.setVisible(state.isRegistered());
      }
    });

    parentBuilder.add("premiumRegisteredPanel", panel);
  }
}
