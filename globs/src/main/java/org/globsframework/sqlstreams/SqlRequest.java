package org.globsframework.sqlstreams;

import org.globsframework.sqlstreams.exceptions.SqlException;

public interface SqlRequest {
  void run() throws SqlException;

  void close();
}
