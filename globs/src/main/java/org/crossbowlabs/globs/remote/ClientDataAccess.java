package org.crossbowlabs.globs.remote;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SelectBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlRequest;
import org.crossbowlabs.globs.sqlstreams.UpdateBuilder;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;

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
