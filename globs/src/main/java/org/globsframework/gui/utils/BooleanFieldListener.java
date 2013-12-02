package org.globsframework.gui.utils;

import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import javax.swing.*;
import java.util.Set;

public class BooleanFieldListener implements Disposable {

  private final Key key;
  private final BooleanField field;
  private final GlobRepository repository;
  private BooleanListener functor;
  private ChangeSetListener listener;

  public static BooleanFieldListener installShowHide(final JComponent component, Key key, BooleanField field, GlobRepository repository) {
    return install(key, field, repository, new BooleanListener() {
      public void apply(boolean value) {
        component.setVisible(value);
      }
    });
  }

  public static BooleanFieldListener installNodeStyle(Key key, BooleanField field,
                                                      final SplitsNode node, final String styleForTrue, final String styleForFalse,
                                                      GlobRepository repository) {
    return install(key, field, repository, new BooleanListener() {
      public void apply(boolean isTrue) {
        node.applyStyle(isTrue ? styleForTrue : styleForFalse);
      }
    });
  }

  public static BooleanFieldListener install(Key key, BooleanField field, GlobRepository repository, BooleanListener listener) {
    return new BooleanFieldListener(key, field, repository, listener);
  }

  public BooleanFieldListener(final Key key, final BooleanField field, GlobRepository repository, BooleanListener listener) {
    this.key = key;
    this.field = field;
    this.repository = repository;
    this.functor = listener;
    this.listener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!changeSet.containsChanges(key, field)) {
          return;
        }
        doUpdate();
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (!changedTypes.contains(key.getGlobType())) {
          return;
        }
        doUpdate();
      }

    };
    repository.addChangeListener(this.listener);
    doUpdate();
  }

  private void doUpdate() {
    Glob glob = repository.find(key);
    functor.apply(glob != null && glob.isTrue(field));
  }

  public void dispose() {
    repository.removeChangeListener(listener);
  }
}
