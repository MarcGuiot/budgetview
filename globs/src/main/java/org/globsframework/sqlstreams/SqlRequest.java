package org.globsframework.sqlstreams;

import org.globsframework.sqlstreams.exceptions.SqlException;

public interface SqlRequest {
  void execute() throws SqlException;

  void close();
}
