package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ImportSignpost extends Signpost implements ChangeSetListener {

  public ImportSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.IMPORT_DONE, repository, directory);
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

  private void update() {
    if (!SignpostStatus.isCompleted(SignpostStatus.GOTO_DATA_DONE, repository)) {
      return;
    }

    if (SignpostStatus.isCompleted(SignpostStatus.IMPORT_DONE, repository)) {
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
