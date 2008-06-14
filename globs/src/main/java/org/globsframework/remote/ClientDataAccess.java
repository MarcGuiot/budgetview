package org.globsframework.remote;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SelectBuilder;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;

import java.io.InputStream;
import java.io.OutputStream;

public class ClientDataAccess implements DataAccess {

  private Proxy proxy;

  public interface Proxy {
    InputStream apply(OutputStream outputStream);
  }

  public SelectBuilder getQueryBuilder(GlobType globType) {
    return new SerializedSelectBuilder(proxy);
  }

  public SelectBuilder getQueryBuilder(GlobType globType, Constraint constraint) {
    return null;
  }

  public UpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint) {
    return null;
  }

  public SqlRequest getDeleteRequest(GlobType globType) {
    return null;
  }

  public SqlRequest getDeleteRequest(GlobType globType, Constraint constraint) {
    return null;
  }

  public CreateBuilder getCreateRequest(GlobType globType) {
    return null;
  }

}
