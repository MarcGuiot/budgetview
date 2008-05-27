package org.crossbowlabs.globs.remote;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.sqlstreams.SelectQuery;
import org.crossbowlabs.globs.streams.GlobStream;

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
