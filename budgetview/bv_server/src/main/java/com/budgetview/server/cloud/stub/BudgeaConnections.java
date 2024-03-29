package com.budgetview.server.cloud.stub;

import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;

public class BudgeaConnections {
  private StringWriter writer;
  private JSONWriter json;
  private int total = 0;

  public static BudgeaConnections init() {
    return new BudgeaConnections();
  }

  private BudgeaConnections() {
    writer = new StringWriter();
    json = new JSONWriter(writer);
    json.object();
    json.key("connections");
    json.array();
  }

  public BudgeaConnections add(int id, int userId, int idBank, boolean active, String lastUpdate) {
    return add(id, userId, idBank, active, lastUpdate, null);
  }

  public BudgeaConnections add(int id, int userId, int idBank, boolean active, String lastUpdate, String error) {
    json.object();
    json.key("id").value(id);
    json.key("id_user").value(userId);
    json.key("id_bank").value(idBank);
    json.key("active").value(active);
    json.key("last_update").value(lastUpdate);
    json.key("error").value(error);
    json.endObject();
    total++;
    return this;
  }

  public String get() throws IOException {
    json.endArray();
    json.key("total").value(total);
    json.endObject();
    writer.close();
    return writer.toString();
  }
}
