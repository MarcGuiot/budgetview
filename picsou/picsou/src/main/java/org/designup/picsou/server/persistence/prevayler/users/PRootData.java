package org.designup.picsou.server.persistence.prevayler.users;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PRootData implements CustomSerializable {
  private static final byte V1 = 1;
  private Map<String, Glob> hidenUsers = new HashMap<String, Glob>();
  private Map<String, Glob> users = new HashMap<String, Glob>();
  private Map<String, Integer> labelToCategory = new HashMap<String, Integer>();
  private static final String USERS_ROOT_DATA = "UsersRootData";

  public Glob getUser(String name) {
    return users.get(name);
  }

  public Glob getHiddenUser(String linkInfo) {
    return hidenUsers.get(linkInfo);
  }

  public void addHiddenUser(String cryptedLinkInfo, Glob hiddenUser) {
    hidenUsers.put(cryptedLinkInfo, hiddenUser);
  }

  public void addUser(String name, Glob user) {
    users.put(name, user);
  }

  public void removeUser(String name) {
    users.remove(name);
  }

  public void removeHiddenUser(String cryptedLinkInfo) {
    hidenUsers.remove(cryptedLinkInfo);
  }

  public String getSerializationName() {
    return USERS_ROOT_DATA;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    switch (version) {
      case V1:
        readV1(input);
        break;
      default:
        throw new InvalidData("Unable to read version " + version);
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    {
      output.write(hidenUsers.size());
      Set<Map.Entry<String, Glob>> entries = hidenUsers.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeString(entry.getKey());
        HiddenUser.write(output, entry.getValue());
      }
    }
    {
      output.write(users.size());
      Set<Map.Entry<String, Glob>> entries = users.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeString(entry.getKey());
        User.write(output, entry.getValue());
      }
    }
    {
      output.write(labelToCategory.size());
      Set<Map.Entry<String, Integer>> entries = labelToCategory.entrySet();
      for (Map.Entry<String, Integer> entry : entries) {
        output.writeString(entry.getKey());
        output.write(entry.getValue());
      }
    }
  }

  private void readV1(SerializedInput input) {
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addHiddenUser(input.readString(), HiddenUser.read(input));
        size--;
      }
    }
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addUser(input.readString(), User.read(input));
        size--;
      }
    }
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        labelToCategory.put(input.readString(), input.readNotNullInt());
        size--;
      }
    }
  }

  public int getNewUserId(Date executionTime, String name) {
    int userId = getHash(executionTime, name);
    while (true) {
      if (checkDoNotExist(userId)) {
        return userId;
      }
      userId++;
    }
  }

  private boolean checkDoNotExist(int userId) {
    for (Glob hiddenUser : hidenUsers.values()) {
      if (hiddenUser.get(HiddenUser.USER_ID) == userId) {
        return false;
      }
    }
    return true;
  }

  public int getHash(Date date, String name) {
    int result;
    long ht = date.getTime();
    result = (int) ht ^ (int) (ht >> 32);
    int h = 0;
    for (byte val : name.getBytes()) {
      h = 31 * h + val;
    }
    result = 31 * result + h;
    return result;
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return USERS_ROOT_DATA;
    }

    public CustomSerializable create() {
      return new PRootData();
    }
  }
}
