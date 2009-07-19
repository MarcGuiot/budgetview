package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.ItemAmbiguity;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

/**
 * @deprecated
 */
public class Category {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  @NamingField
  public static StringField INNER_NAME;

  @Target(Category.class)
  public static LinkField MASTER;

  public static BooleanField SYSTEM;

  public static final Integer ALL = MasterCategory.ALL.getId();
  public static final Integer NONE = MasterCategory.NONE.getId();

  static {
    GlobTypeLoader.init(Category.class, "category");
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
      Glob existingCategory =
        repository.findUnique(Category.TYPE,
                              GlobMatchers.fieldEqualsIgnoreCase(Category.INNER_NAME, categoryName));
      if (existingCategory == null) {
        return null;
      }
      return existingCategory.get(Category.ID);
    }
    return category.get(Category.ID);
  }

  public static Glob find(String categoryName, ReadOnlyGlobRepository repository) {
    try {
      return repository.findUnique(Category.TYPE, GlobMatchers.fieldEqualsIgnoreCase(Category.NAME, categoryName));
    }
    catch (ItemAmbiguity ambiguity) {
      throw new ItemAmbiguity("For category '" + categoryName + "' ", ambiguity);
    }
  }

  public static boolean isMaster(Glob category) {
    return category.get(MASTER) == null;
  }

  public static Glob findByName(String name, GlobRepository repository) {
    return repository.findUnique(TYPE, GlobMatchers.fieldEqualsIgnoreCase(NAME, name));
  }

  public static String getName(Integer categoryId, GlobRepository repository) {
    Glob category = repository.get(org.globsframework.model.Key.create(TYPE, categoryId));
    String name = category.get(NAME);
    if (name != null) {
      return name;
    }

    String innerName = category.get(Category.INNER_NAME);
    String translation = Lang.find("category." + innerName);
    if (translation != null) {
      return translation;
    }

    return innerName;
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

  public static String getInnerName(MasterCategory master, String subcat) {
    return master.getName() + "." + subcat;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(INNER_NAME));
      outputStream.writeInteger(values.get(MASTER));
      outputStream.writeBoolean(values.get(SYSTEM));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readJavaString());
      fieldSetter.set(INNER_NAME, input.readJavaString());
      fieldSetter.set(MASTER, input.readInteger());
      fieldSetter.set(SYSTEM, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(INNER_NAME, input.readUtf8String());
      fieldSetter.set(MASTER, input.readInteger());
      fieldSetter.set(SYSTEM, input.readBoolean());
    }

    public int getWriteVersion() {
      return 2;
    }
  }

}
