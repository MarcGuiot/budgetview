package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Args;
import com.budgetview.server.utils.DateConverter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.DateAccessor;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.streams.accessors.TimestampAccessor;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ListCloudUsers {
  public static void main(String... args) throws Exception {
    dump(System.out, args);
  }

  public static void dump(PrintStream out, String... args) throws Exception {
    String configFile = Args.toString(args, 0);
    if (configFile == null) {
      out.println("Usage: script <config_file>");
      return;
    }

    ConfigService config = new ConfigService(configFile);
    GlobsDatabase database = CloudDb.create(config);
    SqlConnection connection = database.connect();

    Map<Integer, Date> lastSeen = retrieveLastSeenDates(connection);
    Map<Integer, Date> lastUpdated = retrieveLastUpdatedDates(connection);


    Ref<IntegerAccessor> id = new Ref<IntegerAccessor>();
    Ref<StringAccessor> email = new Ref<StringAccessor>();
    Ref<TimestampAccessor> creationDate = new Ref<TimestampAccessor>();
    Ref<DateAccessor> subscriptionEndDate = new Ref<DateAccessor>();
    GlobStream stream = connection.startSelect(CloudUser.TYPE)
      .orderBy(CloudUser.CREATION_DATE)
      .select(CloudUser.ID, id)
      .select(CloudUser.EMAIL, email)
      .select(CloudUser.CREATION_DATE, creationDate)
      .select(CloudUser.SUBSCRIPTION_END_DATE, subscriptionEndDate)
      .getQuery()
      .getStream();
    out.println("id    email                                     creation  subsEnd   lastSeen  lastUpdated");
    while (stream.next()) {
      out.print(Strings.leftAlign(Integer.toString(id.get().getValue()), 4));
      out.print("  ");
      out.print(Strings.leftAlign(email.get().getString(), 40));
      out.print("  ");
      out.print(toYyyyMmDd(creationDate.get().getDate()));
      out.print("  ");
      out.print(toYyyyMmDd(subscriptionEndDate.get().getDate()));
      out.print("  ");
      out.print(toYyyyMmDd(lastSeen.get(id.get().getValue())));
      out.print("  ");
      out.print(toYyyyMmDd(lastUpdated.get(id.get().getValue())));
      out.println();
    }
    stream.close();

    connection.commitAndClose();
  }

  public static Map<Integer, Date> retrieveLastSeenDates(SqlConnection connection) {
    Map<Integer, Date> lastSeen = new HashMap<Integer, Date>();
    Ref<IntegerAccessor> userId = new Ref<IntegerAccessor>();
    Ref<DateAccessor> lastSeenDate = new Ref<DateAccessor>();
    GlobStream deviceStream = connection.startSelect(CloudUserDevice.TYPE)
      .select(CloudUserDevice.USER, userId)
      .select(CloudUserDevice.LAST_SEEN, lastSeenDate)
      .getQuery()
      .getStream();
    while (deviceStream.next()) {
      Integer id = userId.get().getInteger();
      if (id == null) {
        continue;
      }
      Date previous = lastSeen.get(id);
      lastSeen.put(id, Dates.last(previous, lastSeenDate.get().getDate()));
    }
    deviceStream.close();
    return lastSeen;
  }

  public static Map<Integer, Date> retrieveLastUpdatedDates(SqlConnection connection) {
    Map<Integer, Date> lastUpdated = new HashMap<Integer, Date>();
    Ref<IntegerAccessor> userId = new Ref<IntegerAccessor>();
    Ref<TimestampAccessor> lastSeenDate = new Ref<TimestampAccessor>();
    GlobStream deviceStream = connection.startSelect(ProviderUpdate.TYPE)
      .select(ProviderUpdate.USER, userId)
      .select(ProviderUpdate.DATE, lastSeenDate)
      .getQuery()
      .getStream();
    while (deviceStream.next()) {
      Integer id = userId.get().getInteger();
      if (id == null) {
        continue;
      }
      Date previous = lastUpdated.get(id);
      lastUpdated.put(id, Dates.last(previous, lastSeenDate.get().getDate()));
    }
    deviceStream.close();
    return lastUpdated;
  }

  private static String toYyyyMmDd(Date date) {
    if (date == null) {
      return "-       ";
    }
    return Integer.toString(DateConverter.toYyyyMmDd(date));
  }
}
