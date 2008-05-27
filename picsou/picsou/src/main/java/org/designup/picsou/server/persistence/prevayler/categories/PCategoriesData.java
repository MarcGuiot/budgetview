package org.designup.picsou.server.persistence.prevayler.categories;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.designup.picsou.server.persistence.prevayler.accounts.CategoryData;
import org.designup.picsou.server.session.Persistence;

import java.util.List;

public class PCategoriesData implements CustomSerializable {
  private static final String CATEGORIES = "CATEGORIES";
  private CategoryData categoryData = new CategoryData();

  public List<Persistence.CategoryInfo> getAssociatedCategory(List<String> infos) {
    return categoryData.getAssociatedCategory(infos);
  }

  public void addCategory(String info, Integer categoryId) {
    categoryData.addCategory(info, categoryId);
  }

  public String getSerializationName() {
    return CATEGORIES;
  }

  public void read(SerializedInput input, Directory directory) {
    categoryData = CategoryData.read(input);
  }

  public void write(SerializedOutput output, Directory directory) {
    CategoryData.write(output, categoryData);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return CATEGORIES;
    }

    public CustomSerializable create() {
      return new PCategoriesData();
    }
  }

}
