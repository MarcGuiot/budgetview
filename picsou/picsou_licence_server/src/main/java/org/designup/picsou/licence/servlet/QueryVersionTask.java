package org.designup.picsou.licence.servlet;

import org.designup.picsou.licence.model.SoftwareInfo;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.LongAccessor;
import org.globsframework.utils.Log;
import org.globsframework.utils.Ref;

import java.util.TimerTask;

public class QueryVersionTask extends TimerTask {
  private VersionService versionService;
  private SelectQuery query;
  private Ref<LongAccessor> jarVersionRef;
  private Ref<LongAccessor> configVersionRef;

  public QueryVersionTask(SqlService sqlService, VersionService versionService) {
    this.versionService = versionService;
    jarVersionRef = new Ref<LongAccessor>();
    configVersionRef = new Ref<LongAccessor>();
    query = sqlService.getDb().getQueryBuilder(SoftwareInfo.TYPE)
      .select(SoftwareInfo.LATEST_JAR_VERSION, jarVersionRef)
      .select(SoftwareInfo.LATEST_CONFIG_VERSION, configVersionRef)
      .getQuery();
  }


  public void run() {
    try {
      GlobStream stream = query.execute();
      if (stream.next()) {
        versionService.setVersion(jarVersionRef.get().getLong(), configVersionRef.get().getLong());
      }
      stream.close();
    }
    catch (Exception e) {
      Log.write("query version", e);
    }
  }
}
