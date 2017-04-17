package com.budgetview.server.cloud.stub;

import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;

public class BudgeaStatement {
  private StringWriter writer;
  private JSONWriter json;

  public static BudgeaStatement init() {
    return new BudgeaStatement();
  }

  public BudgeaStatement() {
    writer = new StringWriter();
    json = new JSONWriter(writer);
    json.object();
    json.key("connections");
    json.array();

  }

  public BudgeaStatement addConnection(int id, int userId, int bankId, String bankName, String lastUpdate) {
    return addConnection(id, userId, bankId, bankName, lastUpdate, null);
  }

  public BudgeaStatement addConnection(int id, int userId, int bankId, String bankName, String lastUpdate, String error) {
    json.object();
    json.key("id").value(id);
    json.key("id_user").value(userId);
    json.key("id_bank").value(bankId);
    json.key("last_update").value(lastUpdate);
    json.key("error").value(error);

    json.key("bank");
    json.object();
    json.key("id").value(bankId);
    json.key("name").value(bankName);
    json.endObject();

    json.key("accounts");
    json.array();

    return this;
  }

  public BudgeaStatement addAccount(int id, String name, String number, String type, double balance, String lastUpdate) {
    json.object();
    json.key("id").value(id);
    json.key("name").value(name);
    json.key("number").value(number);
    json.key("last_update").value(lastUpdate);
    json.key("balance").value(balance);
    json.key("type").value(type);
    json.key("deleted").value(null);

    json.key("transactions");
    json.array();

    return this;
  }

  public BudgeaStatement addTransaction(int id, String date, double amount, String label) {
    return addTransaction(id, date, date, amount, label, label, 9998, "Indéfini", null);
  }

  public BudgeaStatement addTransaction(int id, String date, double amount, String label, BudgeaCategory budgeaCategory) {
    return addTransaction(id, date, date, amount, label, label, budgeaCategory.getId(), budgeaCategory.getName(), null);
  }

  public BudgeaStatement addTransaction(int id, String date, double amount, String label, BudgeaCategory budgeaCategory, boolean deleted) {
    return addTransaction(id, date, date, amount, label, label, budgeaCategory.getId(), budgeaCategory.getName(), deleted);
  }

  public BudgeaStatement addTransaction(int id, String operationDate, String bankDate, double amount, String label, String originalLabel, int category, String categoryName, Boolean deleted) {
    json.object();
    json.key("id").value(id);
    json.key("date").value(operationDate);
    json.key("rdate").value(bankDate);
    json.key("wording").value(label);
    json.key("original_wording").value(originalLabel);
    json.key("value").value(amount);
    json.key("deleted").value(deleted);

    json.key("category");
    json.object();
    json.key("id").value(category);
    json.key("name").value(categoryName);
    json.endObject();

    json.endObject();

    return this;
  }

  public BudgeaStatement endAccount() {
    json.endArray();
    json.endObject();
    return this;
  }

  public BudgeaStatement endConnection() {
    json.endArray();
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
