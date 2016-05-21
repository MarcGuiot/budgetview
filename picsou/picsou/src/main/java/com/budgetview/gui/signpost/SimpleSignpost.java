package com.budgetview.gui.signpost;

import com.budgetview.model.SignpostStatus;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SimpleSignpost extends PersistentSignpost implements ChangeSetListener {
  private String text;
  protected BooleanField prerequisiteField;

  protected SimpleSignpost(String text,
                           BooleanField completionField,
                           BooleanField prerequisiteField,
                           GlobRepository repository,
                           Directory directory) {
    super(completionField, repository, directory);
    this.text = text;
    this.prerequisiteField = prerequisiteField;
  }

  protected void init() {
    repository.addChangeListener(this);
    update();
  }

  public void delete() {
    hide();
    repository.removeChangeListener(this);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(SignpostStatus.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SignpostStatus.TYPE)) {
      update();
    }
  }

  protected void update() {
    if (SignpostStatus.isCompleted(prerequisiteField, repository)) {
      if (canShow()) {
        show(text);
      }
    }
    else {
      if (isShowing()) {
        hide();
      }
    }
  }
}