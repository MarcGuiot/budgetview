package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.sqlstreams.exceptions.SqlException;

public interface SqlRequest {
  void run() throws SqlException;
}
