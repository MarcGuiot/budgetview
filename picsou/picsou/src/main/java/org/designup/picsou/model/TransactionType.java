package org.designup.picsou.model;

import static org.designup.picsou.model.TransactionTypeMatcher.TRANSACTION_TYPE;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

public enum TransactionType implements GlobConstantContainer {
  VIREMENT(1),
  CHECK(2),
  WITHDRAWAL(3),
  DEPOSIT(4),
  PRELEVEMENT(5),
  CREDIT(6),
  CREDIT_CARD(7),
  BANK_FEES(9);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  public static boolean isOfType(Glob transaction, TransactionType type) {
    return Utils.equal(transaction.get(Transaction.TRANSACTION_TYPE), type.getGlob().get(ID));
  }

  public static TransactionType getType(Integer id) {
    for (TransactionType type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    return null;
  }

  public static Integer getId(TransactionType transactionType) {
    return transactionType.id;
  }

  private ReadOnlyGlob glob;
  private final int id;

  TransactionType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name().toLowerCase();
  }

  public ReadOnlyGlob getGlob() {
    if (glob == null) {
      glob = new ReadOnlyGlob(TYPE,
                              FieldValuesBuilder
                                .init(ID, id)
                                .set(NAME, name().toLowerCase())
                                .get());
    }
    return glob;
  }

  public static TransactionType get(Glob transactionTypeMatcher) throws ItemNotFound {
    Integer transactionTypeId = transactionTypeMatcher.get(TRANSACTION_TYPE);
    for (TransactionType transactionType : values()) {
      if (Utils.equal(transactionType.id, transactionTypeId)) {
        return transactionType;
      }
    }
    throw new ItemNotFound("Unknown transactionType: " + transactionTypeId);
  }

  static {
    GlobTypeLoader.init(TransactionType.class);
  }
}
