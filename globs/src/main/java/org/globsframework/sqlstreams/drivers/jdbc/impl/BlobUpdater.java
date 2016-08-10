package org.globsframework.sqlstreams.drivers.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BlobUpdater {

  void setBlob(PreparedStatement preparedStatement, int index, byte[] bytes) throws SQLException;
}
