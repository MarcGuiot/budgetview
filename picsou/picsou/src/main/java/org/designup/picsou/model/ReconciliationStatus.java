package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;

import static org.globsframework.model.FieldValue.value;

public enum ReconciliationStatus implements GlobConstantContainer {
  TO_RECONCILE(true),
  RECONCILED(false);
  public static GlobType TYPE;

  @Key
  public static BooleanField ID;
  boolean id;

  static {
    GlobTypeLoader.init(ReconciliationStatus.class, "reconciliationStatus");
  }

  ReconciliationStatus(boolean id) {
    this.id = id;
  }

  public boolean getId() {
    return id;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ReconciliationStatus.TYPE,
                            value(ReconciliationStatus.ID, id));
  }

  public static boolean canBeSet(Glob transaction) {
    Boolean status = transaction.get(Transaction.RECONCILIATION_STATUS);
    return (status == null) || (status == TO_RECONCILE.id);
  }
}
