package com.budgetview.shared.utils;

import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;

public interface GlobSerializer {

  boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues);

  byte[] serializeData(FieldValues fieldValues);

  void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter);

  int getWriteVersion();
}
