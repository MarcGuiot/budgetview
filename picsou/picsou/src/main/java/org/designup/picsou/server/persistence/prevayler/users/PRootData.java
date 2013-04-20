package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class PRootData implements CustomSerializable {
  private static final byte V1 = 1;
  private static final byte V2 = 2;
  private static final byte V3 = 3;
  private static final byte V4 = 4;
  private static final byte V5 = 5;
  private byte[] id = null;
  private byte[] mail = null;
  private byte[] signature = null;
  private String activationCode;
  private long count = 0;
  private Map<String, Glob> hidenUsers = new HashMap<String, Glob>();
  private Map<String, Glob> users = new HashMap<String, Glob>();
  private static final String USERS_ROOT_DATA = "UsersRootData";
  private long downloadedVersion = -1;
  private String lang;
  private long jarVersion;

  public PRootData() {
  }

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

  public void register(byte[] mail, byte[] signature, String activationCode) {
    this.mail = mail;
    this.signature = signature;
    this.activationCode = activationCode;
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
      case V2:
        readV2(input);
        break;
      case V3:
        readV3(input);
        break;
      case V4:
        readV4(input);
        break;
      case V5:
        readV5(input);
        break;
      default:
        throw new InvalidData("Unable to read version " + version);
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V5);
    writeV5Info(output);
    output.write(downloadedVersion);
    output.writeUtf8String(lang);
    output.write(jarVersion);
  }

  private void writeV1Info(SerializedOutput output) {
    {
      output.write(hidenUsers.size());
      Set<Map.Entry<String, Glob>> entries = hidenUsers.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeJavaString(entry.getKey());
        HiddenUser.write(output, entry.getValue());
      }
    }
    output.writeBytes(id);
    output.writeBytes(mail);
    output.write(count);
    output.writeJavaString(activationCode);
    output.writeBytes(signature);
    {
      output.write(users.size());
      Set<Map.Entry<String, Glob>> entries = users.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeJavaString(entry.getKey());
        User.write(output, entry.getValue());
      }
    }
  }

  private void writeV5Info(SerializedOutput output) {
    {
      output.write(hidenUsers.size());
      Set<Map.Entry<String, Glob>> entries = hidenUsers.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeUtf8String(entry.getKey());
        HiddenUser.write(output, entry.getValue());
      }
    }
    output.writeBytes(id);
    output.writeBytes(mail);
    output.write(count);
    output.writeUtf8String(activationCode);
    output.writeBytes(signature);
    {
      output.write(users.size());
      Set<Map.Entry<String, Glob>> entries = users.entrySet();
      for (Map.Entry<String, Glob> entry : entries) {
        output.writeUtf8String(entry.getKey());
        User.write(output, entry.getValue());
      }
    }
  }

  private void readV5(SerializedInput input) {
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addHiddenUser(input.readUtf8String(), HiddenUser.read(input));
        size--;
      }
    }
    id = input.readBytes();
    mail = input.readBytes();
    count = input.readNotNullLong();
    activationCode = input.readUtf8String();
    signature = input.readBytes();
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addUser(input.readUtf8String(), User.read(input));
        size--;
      }
    }
    downloadedVersion = input.readNotNullLong();
    lang = input.readUtf8String();
    jarVersion = input.readNotNullLong();
  }


  private void readV4(SerializedInput input) {
    readV3(input);
    jarVersion = input.readNotNullLong();
  }

  private void readV3(SerializedInput input) {
    readV2(input);
    lang = input.readUtf8String();
  }

  private void readV2(SerializedInput input) {
    readV1Info(input);
    downloadedVersion = input.readNotNullLong();
  }


  private void readV1(SerializedInput input) {
    readV1Info(input);
  }

  private void readV1Info(SerializedInput input) {
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addHiddenUser(Strings.removeNewLine(input.readJavaString()), HiddenUser.read(input));
        size--;
      }
    }
    id = input.readBytes();
    mail = input.readBytes();
    count = input.readNotNullLong();
    activationCode = Strings.removeNewLine(input.readJavaString());
    signature = input.readBytes();
    {
      int size = input.readNotNullInt();
      while (size != 0) {
        addUser(Strings.removeNewLine(input.readJavaString()), User.read(input));
        size--;
      }
    }
  }

  public int getNewUserId(Date executionTime, String name) {
    int userId = Math.abs(getHash(executionTime, name));
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

  // attention a ne pas changer cette fonction sinon on pourrait avoir des comportements d'excecution de transaction
  // different (createUserAndHiddenUser
  public int getHash(Date date, String name) {
    int result;
    long ht = date.getTime();
    result = (int)ht ^ (int)(ht >> 32);
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

  public RootDataManager.RepoInfo getRepoInfo() {
    count++;
    return new RootDataManager.RepoInfo(id, mail, signature, activationCode, count, downloadedVersion, lang, jarVersion);
  }

  public void setRepoId(byte[] repoId) {
    if (id == null) {
      id = repoId;
    }
  }

  public GlobList getLocalUsers() {
    return new GlobList(users.values());
  }

  public void setDownloadedVersion(long version) {
    this.downloadedVersion = version;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public void setJarVersion(long jarVersion) {
    this.jarVersion = jarVersion;
  }

  public byte[] getId() {
    return id;
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return USERS_ROOT_DATA;
    }

    public CustomSerializable create() {
      return new PRootData();
    }
  }

  public void dump() throws IOException {
    Writer writer = new OutputStreamWriter(System.out);
    writer.append("id='").append(Arrays.toString(id)).append("'\n");
    writer.append("mail='").append(Arrays.toString(mail)).append("'\n");
    writer.append("signature='").append(Arrays.toString(signature)).append("'\n");
    for (Map.Entry<String, Glob> userEntry : users.entrySet()) {
      writer.append("user='").append(userEntry.getKey()).append("'");
      writer.append(GlobPrinter.toString(userEntry.getValue())).append("\n");
    }
    for (Map.Entry<String, Glob> hiddenUserEntry : hidenUsers.entrySet()) {
      writer.append("hiddenUser='").append(hiddenUserEntry.getKey()).append("'\n");
      writer.append(GlobPrinter.toString(hiddenUserEntry.getValue())).append("\n");
    }
    writer.flush();
  }
}
