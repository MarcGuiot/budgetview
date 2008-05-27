package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.impl.ReadOnlyGlob;
import org.crossbowlabs.globs.model.utils.GlobConstantContainer;
import org.crossbowlabs.globs.utils.Utils;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public enum TagType implements GlobConstantContainer {
  GRATABILITI(1),
  EXCLUSIVE(1),
  BANK_FEES(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  public static TagType getType(Integer id) {
    for (TagType type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    return null;
  }

  public static Integer getId(TagType tagType) {
    return tagType.id;
  }

  private ReadOnlyGlob glob;
  private final int id;

  TagType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
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

  public static TagType get(Glob transactionTypeMatcher) throws ItemNotFound {
    Integer transactionTypeId = transactionTypeMatcher.get(TransactionTypeMatcher.TRANSACTION_TYPE);
    for (TagType transactionType : values()) {
      if (Utils.equal(transactionType.id, transactionTypeId)) {
        return transactionType;
      }
    }
    throw new ItemNotFound("Unknown transactionType: " + transactionTypeId);
  }

  static {
    GlobTypeLoader.init(TagType.class);
  }
}
