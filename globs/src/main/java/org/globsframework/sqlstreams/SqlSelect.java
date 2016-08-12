package org.globsframework.sqlstreams;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.streams.GlobStream;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

public interface SqlSelect {
  GlobStream getStream();

  GlobList getList();

  Glob getUnique() throws ItemNotFound, TooManyItems;

  void close();
}
