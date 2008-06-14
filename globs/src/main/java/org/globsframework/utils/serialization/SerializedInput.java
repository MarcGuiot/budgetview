package org.globsframework.utils.serialization;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.delta.DeltaGlob;

import java.util.Date;

public interface SerializedInput {
  Date readDate();

  Integer readInteger();

  int readNotNullInt();

  Double readDouble();

  double readNotNullDouble();

  String readString();

  Boolean readBoolean();

  Long readLong();

  long readNotNullLong();

  byte readByte();

  byte[] readBytes();

  Glob readGlob(GlobModel model);

  ChangeSet readChangeSet(GlobModel model);

  /**
   * @deprecated - A SUPPRIMER
   */
  DeltaGlob readDeltaGlob(GlobModel model);

  int[] readIntArray();

  long[] readLongArray();
}
