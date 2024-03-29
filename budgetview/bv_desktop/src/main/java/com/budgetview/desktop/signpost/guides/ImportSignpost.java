package com.budgetview.desktop.signpost.guides;

import com.budgetview.desktop.signpost.SimpleSignpost;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import net.java.balloontip.BalloonTip;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ImportSignpost extends SimpleSignpost implements ChangeSetListener {

  public ImportSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.import"), SignpostStatus.IMPORT_STARTED, SignpostStatus.WELCOME_SHOWN, repository, directory);
    setLocation(BalloonTip.Orientation.RIGHT_BELOW, BalloonTip.AttachLocation.SOUTHWEST);
  }

  protected void init() {
    repository.addChangeListener(this);
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(SignpostStatus.KEY)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(SignpostStatus.TYPE)) {
      update();
    }
  }

  protected boolean canShow() {
    return SignpostStatus.isCompleted(SignpostStatus.WELCOME_SHOWN, repository) && super.canShow();
  }

  protected void update() {
    if (SignpostStatus.isCompleted(SignpostStatus.IMPORT_STARTED, repository)) {
      dispose();
      return;
    }
    if (!repository.contains(Transaction.TYPE) && canShow()) {
      show(Lang.get("signpost.import"));
    }
    else if (repository.contains(Transaction.TYPE) && isShowing()) {
      dispose();
    }
  }
}
