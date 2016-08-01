package com.budgetview.shared.utils;

import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;

public interface PicsouGlobSerializer {

  boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues);

  byte[] serializeData(FieldValues fieldValues);

  void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id);

  int getWriteVersion();
}
