package org.designup.picsou.license.servlet;

import org.designup.picsou.license.model.SoftwareInfo;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.LongAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.utils.Log;
import org.globsframework.utils.Ref;

import java.util.TimerTask;

public class QueryVersionTask extends TimerTask {
  private VersionService versionService;
  private SelectQuery query;
  private Ref<LongAccessor> jarVersionRef;
  private Ref<LongAccessor> configVersionRef;
  private Ref<StringAccessor> mailRef;
  private Ref<IntegerAccessor> groupRef;

  public QueryVersionTask(SqlService sqlService, VersionService versionService) {
    this.versionService = versionService;
    jarVersionRef = new Ref<LongAccessor>();
    configVersionRef = new Ref<LongAccessor>();
    groupRef = new Ref<IntegerAccessor>();
    mailRef = new Ref<StringAccessor>();
    query = sqlService.getDb().getQueryBuilder(SoftwareInfo.TYPE)
      .select(SoftwareInfo.LATEST_JAR_VERSION, jarVersionRef)
      .select(SoftwareInfo.LATEST_CONFIG_VERSION, configVersionRef)
      .select(SoftwareInfo.GROUP_ID, groupRef)
      .select(SoftwareInfo.MAIL, mailRef)
      .getNotAutoCloseQuery();
  }


  public void run() {
    try {
      GlobStream stream = query.execute();
      versionService.start();
      while (stream.next()) {
        versionService.setVersion(mailRef.get().getString(), groupRef.get().getInteger(),
                                  jarVersionRef.get().getLong(), configVersionRef.get().getLong());
      }
      versionService.complete();
      stream.close();
    }
    catch (Exception e) {
      Log.write("query version", e);
    }
  }
}
