package org.globsframework.sqlstreams;

import org.globsframework.sqlstreams.exceptions.GlobsSqlException;

public interface SqlRequest {
  int execute() throws GlobsSqlException;

  void close();
}
