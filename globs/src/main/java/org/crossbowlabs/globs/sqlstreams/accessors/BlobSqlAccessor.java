package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.BlobAccessor;

public class BlobSqlAccessor extends SqlAccessor implements BlobAccessor {
  public Object getObjectValue() {
    return getValue();
  }

  public byte[] getValue() {
    return getSqlMoStream().getBytes(getIndex());
  }
}
