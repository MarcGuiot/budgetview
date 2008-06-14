package org.globsframework.remote;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.streams.GlobStream;

public class SerializedQuery implements SelectQuery {
  private ClientDataAccess.Proxy proxy;
  private SerializedSelectBuilder selectBuilder;

  public SerializedQuery(ClientDataAccess.Proxy proxy, SerializedSelectBuilder selectBuilder) {
    this.proxy = proxy;
    this.selectBuilder = selectBuilder;
  }

  public GlobStream execute() {
    return null;
  }

  public GlobList executeAsGlobs() {
    return null;
  }

  public Glob executeUnique() {
    return null;
  }
}
