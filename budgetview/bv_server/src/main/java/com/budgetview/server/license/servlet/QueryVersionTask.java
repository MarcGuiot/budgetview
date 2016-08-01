package com.budgetview.server.license.servlet;

import com.budgetview.server.license.model.SoftwareInfo;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.LongAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.utils.Ref;

import java.util.TimerTask;

public class QueryVersionTask extends TimerTask {
  static Logger logger = Logger.getLogger("QueryVersionTask");
  private SqlService sqlService;
  private VersionService versionService;
  private SelectQuery query;
  private Ref<LongAccessor> jarVersionRef;
  private Ref<LongAccessor> configVersionRef;
  private Ref<StringAccessor> mailRef;
  private Ref<IntegerAccessor> groupRef;
  private SqlConnection db;

  public QueryVersionTask(SqlService sqlService, VersionService versionService) {
    this.sqlService = sqlService;
    this.versionService = versionService;
    jarVersionRef = new Ref<LongAccessor>();
    configVersionRef = new Ref<LongAccessor>();
    groupRef = new Ref<IntegerAccessor>();
    mailRef = new Ref<StringAccessor>();
    db = sqlService.getDb();
    query = db.getQueryBuilder(SoftwareInfo.TYPE)
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
                                  jarVersionRef.get().getValue(), configVersionRef.get().getValue());
      }
      versionService.complete();
      stream.close();
    }
    catch (Exception e) {
      logger.error("query version", e);
      try {
        db.commitAndClose();
      }
      catch (Exception ex) {
        logger.error("commit fail ", ex);
      }
      db = sqlService.getDb();
      query = db.getQueryBuilder(SoftwareInfo.TYPE)
        .select(SoftwareInfo.LATEST_JAR_VERSION, jarVersionRef)
        .select(SoftwareInfo.LATEST_CONFIG_VERSION, configVersionRef)
        .select(SoftwareInfo.GROUP_ID, groupRef)
        .select(SoftwareInfo.MAIL, mailRef)
        .getNotAutoCloseQuery();

    }
  }
}
