package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.BlobAccessor;

public class BlobSqlAccessor extends SqlAccessor implements BlobAccessor {
  public Object getObjectValue() {
    return getValue();
  }

  public byte[] getValue() {
    return getSqlStream().getBytes(getIndex());
  }
}
