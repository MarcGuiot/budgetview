package org.crossbowlabs.globs.utils.serialization;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.delta.DeltaGlob;

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

  /** @deprecated - A SUPPRIMER*/
  DeltaGlob readDeltaGlob(GlobModel model);

  int[] readIntArray();

  long[] readLongArray();
}
