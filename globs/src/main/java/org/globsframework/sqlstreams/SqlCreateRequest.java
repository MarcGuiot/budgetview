package org.globsframework.sqlstreams;

import org.globsframework.model.FieldValues;

public interface SqlCreateRequest extends SqlRequest {
  FieldValues getLastGeneratedIds();
}
