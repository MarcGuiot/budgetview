package org.globsframework.sqlstreams;

import org.globsframework.sqlstreams.exceptions.GlobsSQLException;

public interface SqlRequest {
  void execute() throws GlobsSQLException;

  void close();
}
