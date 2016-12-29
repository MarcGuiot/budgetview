package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import static com.budgetview.shared.json.Json.json;

public class BudgeaCategoriesDump {
  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();

    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl("/auth/init")), "/auth/init");
    String token = auth.getString("auth_token");

    JSONObject categories = json(Request.Get(BudgeaConstants.getServerUrl("/categories"))
                                   .addHeader("Authorization", "Bearer " + token), "/categories");

    dumpCategories(categories);
  }

  private static void dumpCategories(JSONObject categories) {
    for (Object item : categories.getJSONArray("categories")) {
      JSONObject jsonItem = (JSONObject) item;
      printCategory(jsonItem);

      for (Object child : jsonItem.getJSONArray("children")) {
        JSONObject jsonChild = (JSONObject) child;
        printCategory(jsonItem, jsonChild);
      }

      System.out.println("");
    }
  }

  private static void printCategory(JSONObject item) {
    int id = item.getInt("id");
    String name = item.getString("name");
    System.out.println("  " + Strings.toUpperCaseLabel(name) + "(" + id + ", \"" + name + "\"),");
  }

  private static void printCategory(JSONObject item, JSONObject subItem) {
    int id = subItem.getInt("id");
    int parentId = item.getInt("id");
    String name = subItem.getString("name");
    System.out.println("  " + Strings.toUpperCaseLabel(name) + "(" + id + ", " + parentId + ", \"" + name + "\"),");
  }

}

