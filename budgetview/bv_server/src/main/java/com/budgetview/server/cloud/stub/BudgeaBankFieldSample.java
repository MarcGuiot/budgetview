package com.budgetview.server.cloud.stub;

public enum BudgeaBankFieldSample {
  BUDGEA_FIELDS_STEP_1("{\n" +
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

  BUDGEA_FIELDS_STEP_2("{\n" +
                       "   \"expire\" : \"2016-04-03 19:03:20\",\n" +
                       "   \"id_bank\" : 3,\n" +
                       "   \"id\" : 487,\n" +
                       "   \"fields\" : [\n" +
                       "      {\n" +
                       "         \"name\" : \"pin_code\",\n" +
                       "         \"label\" : \"Please enter the PIN code\",\n" +
                       "         \"type\" : \"text\",\n" +
                       "         \"regex\" : null\n" +
                       "      }\n" +
                       "   ],\n" +
                       "   \"id_user\" : 687,\n" +
                       "   \"error\" : \"additionalInformationNeeded\",\n" +
                       "   \"last_update\" : null,\n" +
                       "   \"active\" : true\n" +
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
      "}"),

  ING_DIRECT("{\n" +
             "   \"total\": 2,\n" +
             "   \"fields\": [\n" +
             "      {\n" +
             "        \"regex\": \"^[0-9]+$\",\n" +
             "        \"name\": \"login\",\n" +
             "        \"label\": \"Numero client\",\n" +
             "        \"type\": \"text\"\n" +
             "      },\n" +
             "      {\n" +
             "        \"regex\": \"^[0-9]+$\",\n" +
             "        \"name\": \"password\",\n" +
             "        \"label\": \"Code secret\",\n" +
             "        \"type\": \"password\"\n" +
             "      },\n" +
             "      {\n" +
             "        \"regex\": \"^[0-9]{2}[/-]?[0-9]{2}[/-]?[0-9]{4}$\",\n" +
             "        \"name\": \"birthday\",\n" +
             "        \"label\": \"Date de naissance\",\n" +
             "        \"type\": \"date\"\n" +
             "      }\n" +
             "   ],\n" +
             "}");


  private String json;

  BudgeaBankFieldSample(String json) {
    this.json = json;
  }

  public String getJSON() {
    return json;
  }
  }
