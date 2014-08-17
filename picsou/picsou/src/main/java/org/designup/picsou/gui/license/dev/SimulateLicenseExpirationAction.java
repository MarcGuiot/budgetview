package org.designup.picsou.gui.license.dev;

import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class SimulateLicenseExpirationAction extends AbstractAction {
  private GlobRepository repository;

  public SimulateLicenseExpirationAction(GlobRepository repository) {
    super("[simulate license expiration]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.update(User.KEY,
                      value(User.IS_REGISTERED_USER, false),
                      value(User.PREMIUM_EVOLUTION_STATE, PremiumEvolutionState.TRIAL_OVER.getId()));
  }
}
