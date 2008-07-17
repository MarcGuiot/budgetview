package org.designup.picsou.server.session;

import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.globsframework.model.Glob;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Arrays;

public interface Persistence {
  UserInfo createUser(String name, boolean isRegisteredUser,
                      byte[] encryptedPassword, byte[] linkInfo, byte[] encryptedLinkInfo);

  void getData(SerializedOutput output, Integer userId);

  void updateData(SerializedInput input, SerializedOutput output, Integer userId);

  Glob identify(String name, byte[] encryptedPassword);

  Integer confirmUser(String b64LinkInfo) throws IdentificationFailed;

  Integer getNextId(String tableName, Integer count, Integer userId);

  void delete(String name, byte[] encryptedPassword, byte[] linkInfo, byte[] encryptedLinkInfo, Integer userId);

  Glob getUser(String name);

  Glob getHiddenUser(byte[] encryptedLinkInfo);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);

  static class CategoryInfo {
    int categories[] = new int[0];
    int count[] = new int[0];
    long timestamp[] = new long[0];

    static CategoryInfo NULL = new CategoryInfo();
    static final byte V1 = 1;

    public int[] getCategories() {
      return categories;
    }

    public void add(Integer categoryId) {
      if (categoryId == null) {
        return;
      }
      for (int i = 0; i < categories.length; i++) {
        if (categories[i] == categoryId) {
          count[i]++;
          timestamp[i] = System.currentTimeMillis();
          return;
        }
      }
      int tmp[] = categories;
      categories = new int[tmp.length + 1];
      System.arraycopy(tmp, 0, categories, 0, tmp.length);
      categories[categories.length - 1] = categoryId;

      tmp = count;
      count = new int[tmp.length + 1];
      System.arraycopy(tmp, 0, count, 0, tmp.length);
      count[count.length - 1] = 1;

      long tmpl[] = timestamp;
      timestamp = new long[tmpl.length + 1];
      System.arraycopy(tmpl, 0, timestamp, 0, tmpl.length);
      timestamp[timestamp.length - 1] = System.currentTimeMillis();
    }

    public static CategoryInfo read(SerializedInput input) {
      byte version = input.readByte();
      switch (version) {
        case -1:
        case V1:
          return readV1(input);
        default:
          throw new InvalidData("Reading version '" + version + "' not managed");
      }
    }

    public static void write(SerializedOutput output, CategoryInfo categoryInfo) {
      write(output, categoryInfo, (byte)-1);
    }

    static void write(SerializedOutput output, CategoryInfo categoryInfo, byte version) {
      if (categoryInfo == null) {
        categoryInfo = CategoryInfo.NULL;
      }
      switch (version) {
        case -1:
        case V1:
          writeV1(output, categoryInfo);
          return;
        default:
          throw new InvalidData("Writing version '" + version + "' not managed");
      }
    }

    private static CategoryInfo readV1(SerializedInput input) {
      CategoryInfo info = new CategoryInfo();
      info.categories = input.readIntArray();
      info.count = input.readIntArray();
      info.timestamp = input.readLongArray();
      return info;
    }

    private static void writeV1(SerializedOutput output, CategoryInfo categoryInfo) {
      output.writeByte(V1);
      output.write(categoryInfo.categories);
      output.write(categoryInfo.count);
      output.write(categoryInfo.timestamp);
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < categories.length; i++) {
        builder.append(categories[i]).append(':').append(count[i]);
        if (i < categories.length - 1) {
          builder.append(",");
        }
      }
      return builder.toString();
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      CategoryInfo info = (CategoryInfo)o;

      if (!Arrays.equals(categories, info.categories)) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      return (categories != null ? Arrays.hashCode(categories) : 0);
    }
  }

  class UserInfo {
    final public Integer userId;
    final public boolean isRegistered;

    public UserInfo(Integer userId, boolean registered) {
      this.userId = userId;
      isRegistered = registered;
    }
  }
}
