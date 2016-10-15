package org.globsframework.sqlstreams;

import org.globsframework.sqlstreams.exceptions.GlobsSQLException;

public interface SqlRequest {
  int execute() throws GlobsSQLException;

  void close();
}
