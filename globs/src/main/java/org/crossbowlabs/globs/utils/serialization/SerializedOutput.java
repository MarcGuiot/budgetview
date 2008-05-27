package org.crossbowlabs.globs.utils.serialization;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.delta.DeltaGlob;

import java.util.Date;

public interface SerializedOutput {
  void writeDate(Date date);

  void write(int value);

  void writeInteger(Integer value);

  void write(double value);

  void writeDouble(Double value);

  void write(boolean value);

  void writeBoolean(Boolean value);

  void write(long value);

  void writeLong(Long value);

  void writeByte(int value);

  void writeByte(byte value);

  void writeBytes(byte[] value);

  void writeString(String value);

  void writeGlob(Glob glob);

  void writeChangeSet(ChangeSet changeSet);

  /** @deprecated - A SUPPRIMER*/
  void writeDeltaGlob(DeltaGlob deltaGlob);

  void write(int[] array);

  void write(long[] array);
}
