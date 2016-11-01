package com.budgetview.server.cloud.stub;

public enum BudgeaBankFieldSample {
  BUDGEA_TEST_CONNECTOR("{\n" +
                        "  \"total\": 3,\n" +
                        "  \"fields\": [\n" +
                        "    {\n" +
                        "      \"regex\": null,\n" +
                        "      \"values\": [\n" +
                        "        {\n" +
                        "          \"label\": \"Particuliers\",\n" +
                        "          \"value\": \"par\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"label\": \"Professionnels\",\n" +
                        "          \"value\": \"pro\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"name\": \"website\",\n" +
                        "      \"label\": \"Type de compte\",\n" +
                        "      \"type\": \"list\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"regex\": null,\n" +
                        "      \"name\": \"login\",\n" +
                        "      \"label\": \"Identifiant\",\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"regex\": null,\n" +
                        "      \"name\": \"password\",\n" +
                        "      \"label\": \"Code (1234)\",\n" +
                        "      \"type\": \"password\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"),

  CIC("{\n" +
      "  \"total\": 2,\n" +
      "  \"fields\": [\n" +
      "    {\n" +
      "      \"regex\": null,\n" +
      "      \"name\": \"login\",\n" +
      "      \"label\": \"Identifiant\",\n" +
      "      \"type\": \"text\"\n" +
      "    },\n" +
      "    {\n" +
      "      \"regex\": null,\n" +
      "      \"name\": \"password\",\n" +
      "      \"label\": \"Mot de passe\",\n" +
      "      \"type\": \"password\"\n" +
      "    }\n" +
      "  ]\n" +
      "}");


  private String json;

  BudgeaBankFieldSample(String json) {
    this.json = json;
  }

  public String getJSON() {
    return json;
  }
}
