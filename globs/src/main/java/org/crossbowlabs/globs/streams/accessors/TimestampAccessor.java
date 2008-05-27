package org.crossbowlabs.globs.streams.accessors;

import java.util.Date;

public interface TimestampAccessor extends Accessor {

  long getTimestamp();

  Date getDate();
}
