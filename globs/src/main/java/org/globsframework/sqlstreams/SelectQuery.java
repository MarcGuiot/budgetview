package org.globsframework.sqlstreams;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.streams.GlobStream;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

public interface SelectQuery {
  GlobStream execute();

  GlobList executeAsGlobs();

  Glob executeUnique() throws ItemNotFound, TooManyItems;

  void close();
}
