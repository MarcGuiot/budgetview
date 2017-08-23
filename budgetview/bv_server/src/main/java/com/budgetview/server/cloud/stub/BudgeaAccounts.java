package com.budgetview.server.cloud.stub;

import com.budgetview.server.cloud.budgea.Budgea;
import org.globsframework.utils.Dates;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;

public class BudgeaAccounts {
  private StringWriter writer;
  private JSONWriter json;

  public static BudgeaAccounts init() {
    return new BudgeaAccounts();
  }

  private BudgeaAccounts() {
    writer = new StringWriter();
    json = new JSONWriter(writer);
    json.object();
    json.key("accounts");
    json.array();
  }

  public BudgeaAccounts add(int id, int connectionId, String name, String number, boolean enabled) {
    json.object();
    json.key("id").value(id);
    json.key("id_connection").value(connectionId);
    json.key("name").value(name);
    json.key("number").value(number);
    json.key("disabled").value(enabled ? null : Budgea.toTimeStampString(Dates.now()));
    json.endObject();
    return this;
  }

  public String get() throws IOException {
    json.endArray();
    json.endObject();
    writer.close();
    return writer.toString();
  }
}
