package org.designup.picsou.gui.license;

import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;

public class PremiumEvolution {

  public interface Listener {
    void processState(PremiumEvolutionState state);
  }

  public static void addListener(final GlobRepository repository, final Listener listener) {
    KeyChangeListener keyListener = new KeyChangeListener(User.KEY) {
      public void update() {
        Glob user = repository.find(User.KEY);
        if (user != null) {
          listener.processState(PremiumEvolutionState.get(user.get(User.PREMIUM_EVOLUTION_STATE)));
        }
      }
    };
    repository.addChangeListener(keyListener);
    keyListener.update();
  }
}
