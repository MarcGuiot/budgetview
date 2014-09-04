package org.designup.picsou.gui.signpost;

import org.designup.picsou.model.SignpostStatus;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class PersistentSignpost extends Signpost {

  private BooleanField completionField;

  protected PersistentSignpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.completionField = completionField;
  }

  protected abstract void init();

  public boolean isCompleted() {
    return SignpostStatus.isCompleted(completionField, repository);
  }

  public void dispose() {
    complete();
    super.dispose();
  }

  protected void complete() {
    SignpostStatus.setCompleted(completionField, repository);
  }
}
