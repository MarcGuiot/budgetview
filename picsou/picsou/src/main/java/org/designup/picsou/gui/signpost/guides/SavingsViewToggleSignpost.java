package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SavingsViewToggleSignpost extends Signpost implements ChangeSetListener {

  public SavingsViewToggleSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.SAVINGS_VIEW_TOGGLE_SHOWN, repository, directory);
    setLocation(BalloonTip.Orientation.RIGHT_ABOVE, BalloonTip.AttachLocation.NORTHEAST);
  }

  protected void init() {
    repository.addChangeListener(this);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (isCompleted()) {
      return;
    }
    if (changeSet.containsChanges(SignpostStatus.TYPE) || changeSet.containsChanges(Series.TYPE)) {
      update();
    }
  }

  private boolean containsExternalSavingsSeries(GlobRepository repository) {
    for (Glob series : repository.getAll(Series.TYPE)) {
      if (BudgetArea.get(series.get(Series.BUDGET_AREA)) == BudgetArea.SAVINGS) {
        if (!isMainAccount(series, Series.FROM_ACCOUNT, repository) &&
            !isMainAccount(series, Series.TO_ACCOUNT, repository)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isMainAccount(Glob series, LinkField accountLink, GlobRepository repository) {
    Glob account = repository.findLinkTarget(series, accountLink);
    if (Account.isMain(account)) {
      return true;
    }
    return false;
  }

  protected void update() {
    if (canShow() && containsExternalSavingsSeries(repository)) {
      show(Lang.get("signpost.savingsViewToggle"));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }


  public void dispose() {
    super.dispose();
  }
}