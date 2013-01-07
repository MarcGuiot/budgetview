package org.globsframework.utils.serialization;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;

import java.util.Date;

public interface SerializedInput {
  Date readDate();

  Integer readInteger();

  int readNotNullInt();

  Double readDouble();

  double readNotNullDouble();

  String readJavaString();

  String readUtf8String();

  Boolean readBoolean();

  Long readLong();

  long readNotNullLong();

  byte readByte();

  byte[] readBytes();

  Glob readGlob(GlobModel model);

  ChangeSet readChangeSet(GlobModel model);

  int[] readIntArray();

  long[] readLongArray();

  void close();
}
