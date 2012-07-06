package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum ReconciliationStatus implements GlobConstantContainer {
  TO_RECONCILE(0),
  RECONCILED(1);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  int id;

  static {
    GlobTypeLoader.init(ReconciliationStatus.class, "reconciliationStatus");
  }

  ReconciliationStatus(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ReconciliationStatus.TYPE,
                            value(ReconciliationStatus.ID, id));
  }

  public static boolean canBeSet(Glob transaction) {
    Integer status = transaction.get(Transaction.RECONCILIATION_STATUS);
    return (status == null) || (status == TO_RECONCILE.id);
  }

  public static boolean isToReconcile(Glob transaction) {
    Integer status = transaction.get(Transaction.RECONCILIATION_STATUS);
    return Utils.equal(status, TO_RECONCILE.id);
  }
}
