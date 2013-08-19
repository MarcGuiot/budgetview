package org.globsframework.gui.utils;

import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class GlobBooleanNodeStyleUpdater implements ChangeSetListener, Disposable {
  private BooleanField field;
  private SplitsNode splitsNode;
  private String styleForTrue;
  private String styleForFalse;
  private GlobRepository repository;

  private Key currentKey;

  public static GlobBooleanNodeStyleUpdater init(BooleanField field, SplitsNode splitsNode,
                                                 String styleForTrue, String styleForFalse,
                                                 GlobRepository repository, Directory directory) {
    return new GlobBooleanNodeStyleUpdater(field, splitsNode, styleForTrue, styleForFalse, repository);
  }

  public GlobBooleanNodeStyleUpdater(BooleanField field, SplitsNode splitsNode,
                                     String styleForTrue, String styleForFalse,
                                     GlobRepository repository) {
    this.field = field;
    this.splitsNode = splitsNode;
    this.styleForTrue = styleForTrue;
    this.styleForFalse = styleForFalse;
    this.repository = repository;
    repository.addChangeListener(this);
  }

  public void setKey(Key key) {
    this.currentKey = key;
    updateStyle();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if ((currentKey != null) && changeSet.containsChanges(currentKey, field)) {
      updateStyle();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(field.getGlobType())) {
      updateStyle();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  private void updateStyle() {
    if (currentKey == null) {
      return;
    }
    Glob project = repository.find(currentKey);
    if (project != null) {
      splitsNode.applyStyle(project.isTrue(field) ? styleForTrue : styleForFalse);
    }
  }
}
