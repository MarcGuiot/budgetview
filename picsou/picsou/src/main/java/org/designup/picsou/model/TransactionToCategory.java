package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Set;

public class TransactionToCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Transaction.class)
  public static LinkField TRANSACTION;

  @Target(Category.class)
  public static LinkField CATEGORY;

  public static MultiFieldUniqueIndex TRANSACTION_INDEX;

  static {
    GlobTypeLoader.init(TransactionToCategory.class)
      .defineMultiFieldUniqueIndex(TRANSACTION_INDEX, TRANSACTION, CATEGORY);
  }

  public static void link(GlobRepository repository, Glob transaction, MasterCategory... categories) {
    link(repository, transaction.get(Transaction.ID), categories);
  }

  public static void link(GlobRepository repository, int transactionId, MasterCategory... categories) {
    for (MasterCategory category : categories) {
      if (category != MasterCategory.NONE) {
        link(repository, transactionId, category.getId());
      }
    }
  }

  public static void link(GlobRepository repository, Integer transactionId, Integer[] categoryIds) {
    for (Integer categoryId : categoryIds) {
      if (!categoryId.equals(Category.NONE) && !categoryId.equals(Category.ALL)) {
        GlobList transactionToCategory =
          repository
            .findByIndex(TransactionToCategory.TRANSACTION_INDEX, TRANSACTION, transactionId)
            .findByIndex(categoryId);
        if (transactionToCategory.isEmpty()) {
          repository.create(TYPE,
                            value(ID, repository.getIdGenerator().getNextId(ID, 1)),
                            value(TRANSACTION, transactionId),
                            value(CATEGORY, categoryId));
        }
      }
    }
  }

  public static void link(GlobRepository repository, int transactionId, int categoryId) {
    if (categoryId == Category.NONE) {
      GlobList categories = getCategories(transactionId, repository);
      for (Glob category : categories) {
        unlink(repository, transactionId, category.get(Category.ID));
      }
    }
    else {
      GlobList association = repository.findByIndex(TransactionToCategory.TRANSACTION_INDEX, TransactionToCategory.TRANSACTION, transactionId)
        .findByIndex(categoryId);
      if (association.isEmpty()) {
        repository.create(TYPE,
                          value(ID, repository.getIdGenerator().getNextId(ID, 1)),
                          value(TRANSACTION, transactionId),
                          value(CATEGORY, categoryId));
      }
    }
  }

  public static void link(GlobRepository repository, Glob transaction, Glob category) {
    link(repository, transaction.get(Transaction.ID), category.get(Category.ID));
  }

  public static void unlink(GlobRepository repository, Glob transaction, Glob category) {
    unlink(repository, transaction.get(Transaction.ID), category.get(Category.ID));
  }

  private static void unlink(GlobRepository repository, int transactionId, int categoryId) {
    GlobList linkToDelete = repository.findByIndex(TransactionToCategory.TRANSACTION_INDEX, TransactionToCategory.TRANSACTION, transactionId)
      .findByIndex(categoryId);
    repository.delete(linkToDelete);
  }

  public static boolean hasCategories(Glob transaction, GlobRepository repository) {
    GlobList categories = getCategories(transaction, repository);
    if (categories.isEmpty() ||
        ((categories.size() == 1) && (categories.get(0).get(Category.ID).equals(Category.NONE)))) {
      return false;
    }
    return true;
  }

  public static GlobList getCategories(int transactionId, GlobRepository repository) {
    Set<Integer> categoryIds = repository.findByIndex(TRANSACTION_INDEX, TRANSACTION, transactionId)
      .getGlobs()
      .getValueSet(CATEGORY);

    return repository.getAll(Category.TYPE, GlobMatchers.contained(Category.ID, categoryIds));
  }

  public static GlobList getCategories(Glob transaction, GlobRepository repository) {
    return getCategories(transaction.get(Transaction.ID), repository);
  }

  public static boolean isInternal(Glob transaction, GlobRepository repository) {
    GlobList categories = getCategories(transaction, repository);
    return ((categories.size() == 1) && (categories.get(0).get(Category.ID) == MasterCategory.INTERNAL.getId()));
  }

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.write(values.get(CATEGORY));
      outputStream.write(values.get(TRANSACTION));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(CATEGORY, input.readNotNullInt());
      fieldSetter.set(TRANSACTION, input.readNotNullInt());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
