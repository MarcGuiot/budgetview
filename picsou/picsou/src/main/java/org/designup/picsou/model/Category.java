package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Category {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NoObfuscation
  @NamingField
  public static StringField NAME;

  @NoObfuscation
  @Target(Category.class)
  public static LinkField MASTER;

  @NoObfuscation
  public static BooleanField SYSTEM;

  public static final Integer ALL = MasterCategory.ALL.getId();
  public static final Integer NONE = MasterCategory.NONE.getId();

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(Category.class, "category");
    loader.addConstants(MasterCategory.createGlobs());
  }

  public static boolean isAll(Glob category) {
    return (category != null) && ALL.equals(category.get(Category.ID));
  }

  public static boolean isNone(Glob category) {
    return (category != null) && NONE.equals(category.get(Category.ID));
  }

  public static Integer findId(String categoryName, ReadOnlyGlobRepository repository) {
    Glob category = find(categoryName, repository);
    if (category == null) {
      return null;
    }
    return category.get(Category.ID);
  }

  public static Glob find(String categoryName, ReadOnlyGlobRepository repository) {
    return repository.findUnique(Category.TYPE, GlobMatchers.fieldEqualsIgnoreCase(Category.NAME, categoryName));
  }

  public static boolean isMaster(Glob category) {
    return category.get(MASTER) == null;
  }

  public static Glob findByName(String name, GlobRepository repository) {
    return repository.findUnique(TYPE, GlobMatchers.fieldEqualsIgnoreCase(NAME, name));
  }

  public static String getName(Integer categoryId, GlobRepository repository) {
    Glob category = repository.get(org.globsframework.model.Key.create(TYPE, categoryId));
    return category.get(NAME);
  }

  public static boolean hasChildren(Integer categoryId, GlobRepository repository) {
    for (Glob category : repository.getAll(Category.TYPE)) {
      if (categoryId.equals(category.get(Category.MASTER))) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSystem(Glob category) {
    return Boolean.TRUE.equals(category.get(Category.SYSTEM));
  }

  public static boolean isReserved(Glob category) {
    return MasterCategory.isReserved(category);
  }

  public static Integer getMasterCategoryId(Integer categoryId, GlobRepository repository) {
    if (categoryId == null) {
      categoryId = MasterCategory.NONE.getId();
    }
    Glob category = repository.find(org.globsframework.model.Key.create(TYPE, categoryId));
    if (category == null) {
      return null;
    }
    if (!isMaster(category)) {
      categoryId = category.get(MASTER);
    }
    return categoryId;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeString(values.get(NAME));
      outputStream.writeInteger(values.get(MASTER));
      outputStream.writeBoolean(values.get(SYSTEM));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readString());
      fieldSetter.set(MASTER, input.readInteger());
      fieldSetter.set(SYSTEM, input.readBoolean());

    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
