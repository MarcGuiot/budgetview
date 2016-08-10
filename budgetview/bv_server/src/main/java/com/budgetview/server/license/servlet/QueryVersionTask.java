package com.budgetview.server.license.servlet;

import com.budgetview.server.license.model.SoftwareInfo;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.LongAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.utils.Ref;

import java.util.TimerTask;

public class QueryVersionTask extends TimerTask {
  static Logger logger = Logger.getLogger("QueryVersionTask");
  private GlobsDatabase db;
  private VersionService versionService;
  private SelectQuery query;
  private Ref<LongAccessor> jarVersionRef;
  private Ref<LongAccessor> configVersionRef;
  private Ref<StringAccessor> mailRef;
  private Ref<IntegerAccessor> groupRef;
  private SqlConnection connection;

  public QueryVersionTask(GlobsDatabase db, VersionService versionService) {
    this.db = db;
    this.versionService = versionService;
    jarVersionRef = new Ref<LongAccessor>();
    configVersionRef = new Ref<LongAccessor>();
    groupRef = new Ref<IntegerAccessor>();
    mailRef = new Ref<StringAccessor>();
    connection = db.connect();
    query = connection.startSelect(SoftwareInfo.TYPE)
      .select(SoftwareInfo.LATEST_JAR_VERSION, jarVersionRef)
      .select(SoftwareInfo.LATEST_CONFIG_VERSION, configVersionRef)
      .select(SoftwareInfo.GROUP_ID, groupRef)
      .select(SoftwareInfo.MAIL, mailRef)
      .getNotAutoCloseQuery();
  }


  public void run() {
    try {
      GlobStream stream = query.getStream();
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
        connection.commitAndClose();
      }
      catch (Exception ex) {
        logger.error("commit fail ", ex);
      }
      connection = db.connect();
      query = connection.startSelect(SoftwareInfo.TYPE)
        .select(SoftwareInfo.LATEST_JAR_VERSION, jarVersionRef)
        .select(SoftwareInfo.LATEST_CONFIG_VERSION, configVersionRef)
        .select(SoftwareInfo.GROUP_ID, groupRef)
        .select(SoftwareInfo.MAIL, mailRef)
        .getNotAutoCloseQuery();

    }
  }
}
