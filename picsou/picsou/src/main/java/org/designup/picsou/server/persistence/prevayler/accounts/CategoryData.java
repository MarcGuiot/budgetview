package org.designup.picsou.server.persistence.prevayler.accounts;

import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.server.session.Persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryData {
  private static byte V1 = 1;
  private Map<String, Persistence.CategoryInfo> categories = new HashMap<String, Persistence.CategoryInfo>();

  public List<Persistence.CategoryInfo> getAssociatedCategory(List<String> infos) {
    List<Persistence.CategoryInfo> result = new ArrayList<Persistence.CategoryInfo>(infos.size());
    for (String info : infos) {
      result.add(categories.get(info));
    }
    return result;
  }

  public void addCategory(String info, Integer categoryId) {
    Persistence.CategoryInfo categoryInfo = categories.get(info);
    if (categoryInfo == null) {
      categoryInfo = new Persistence.CategoryInfo();
      categories.put(info, categoryInfo);
    }
    categoryInfo.add(categoryId);
  }

  public static CategoryData read(SerializedInput input) {
    byte version = input.readByte();
    if (version == V1) {
      return readV1(input);
    }
    throw new UnexpectedApplicationState("version " + version + " not managed");
  }

  private static CategoryData readV1(SerializedInput input) {
    CategoryData categoryData = new CategoryData();

    int categoryLength = input.readNotNullInt();
    categoryData.categories = new HashMap<String, Persistence.CategoryInfo>(categoryLength);
    while (categoryLength != 0) {
      categoryData.categories.put(input.readString(), Persistence.CategoryInfo.read(input));
      categoryLength--;
    }
    return categoryData;
  }

  public static void write(SerializedOutput output, CategoryData categoryData) {
    output.writeByte(V1);
    output.write(categoryData.categories.size());
    for (Map.Entry<String, Persistence.CategoryInfo> entry : categoryData.categories.entrySet()) {
      output.writeString(entry.getKey());
      Persistence.CategoryInfo.write(output, entry.getValue());
    }
  }
}