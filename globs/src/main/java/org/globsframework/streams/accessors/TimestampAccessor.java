package org.globsframework.streams.accessors;

import java.util.Date;

public interface TimestampAccessor extends Accessor {

  long getTimestamp();

  Date getDate();
}
