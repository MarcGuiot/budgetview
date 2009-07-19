package org.designup.picsou.server.serialization;

import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;

public interface PicsouGlobSerializer {

  byte[] serializeData(FieldValues fieldValues);

  void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id);

  int getWriteVersion();
}
