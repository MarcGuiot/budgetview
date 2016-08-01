package com.budgetview.io.importer.json;

import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.PicsouModel;
import com.budgetview.model.RealAccount;
import junit.framework.TestCase;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;

import java.io.StringReader;

import static org.globsframework.model.FieldValue.value;

public class JsonImporterTest extends TestCase {
  public void test() throws Exception {
    String json = "{" +
                  "  \"total\": 59,\n" +
                  "  \"transactions\": [\n" +
                  "    {\n" +
                  "      \"date\": \"2016-05-24\",\n" +
                  "      \"date_scraped\": \"2016-05-24 23:32:47\",\n" +
                  "      \"rdate\": \"2016-05-23\",\n" +
                  "      \"last_update\": \"2016-05-24 23:32:47\",\n" +
                  "      \"application_date\": \"2016-05-23\",\n" +
                  "      \"original_currency\": null,\n" +
                  "      \"country\": null,\n" +
                  "      \"original_wording\": \"FACTURE CB GOOGLE *BUDGEA\",\n" +
                  "      \"id_category\": 9998,\n" +
                  "      \"type\": \"card\",\n" +
                  "      \"stemmed_wording\": \"GOOGLE *BUDGEA\",\n" +
                  "      \"webid\": null,\n" +
                  "      \"commission\": null,\n" +
                  "      \"id\": 3160,\n" +
                  "      \"state\": \"parsed\",\n" +
                  "      \"value\": -34.49,\n" +
                  "      \"new\": true,\n" +
                  "      \"original_value\": null,\n" +
                  "      \"active\": true,\n" +
                  "      \"simplified_wording\": \"GOOGLE *BUDGEA\",\n" +
                  "      \"nopurge\": false,\n" +
                  "      \"id_cluster\": null,\n" +
                  "      \"deleted\": null,\n" +
                  "      \"id_account\": 90,\n" +
                  "      \"comment\": null,\n" +
                  "      \"wording\": \"GOOGLE *BUDGEA\",\n" +
                  "      \"formatted_value\": \"-34,49 \\u20ac\",\n" +
                  "      \"coming\": false,\n" +
                  "      \"documents_count\": 0\n" +
                  "    },\n" +
                  "    {\n" +
                  "      \"date\": \"2016-05-22\",\n" +
                  "      \"original_currency\": null,\n" +
                  "      \"country\": null,\n" +
                  "      \"original_wording\": \"VIREMENT SALAIRE\",\n" +
                  "      \"id_category\": 9998,\n" +
                  "      \"type\": \"transfer\",\n" +
                  "      \"stemmed_wording\": \"SALAIRE\",\n" +
                  "      \"webid\": null,\n" +
                  "      \"rdate\": \"2016-05-22\",\n" +
                  "      \"last_update\": \"2016-05-24 23:32:47\",\n" +
                  "      \"commission\": null,\n" +
                  "      \"id\": 3163,\n" +
                  "      \"state\": \"parsed\",\n" +
                  "      \"value\": 555.9,\n" +
                  "      \"new\": true,\n" +
                  "      \"original_value\": null,\n" +
                  "      \"active\": true,\n" +
                  "      \"date_scraped\": \"2016-05-24 23:32:47\",\n" +
                  "      \"simplified_wording\": \"SALAIRE\",\n" +
                  "      \"nopurge\": false,\n" +
                  "      \"id_cluster\": null,\n" +
                  "      \"deleted\": null,\n" +
                  "      \"id_account\": 90,\n" +
                  "      \"application_date\": \"2016-05-22\",\n" +
                  "      \"comment\": null,\n" +
                  "      \"wording\": \"SALAIRE\",\n" +
                  "      \"formatted_value\": \"555,90 \\u20ac\",\n" +
                  "      \"coming\": false,\n" +
                  "      \"documents_count\": 0\n" +
                  "    }\n" +
                  "  ],\n" +
                  "  \"last_date\": \"2016-05-24\",\n" +
                  "  \"first_date\": \"2016-01-28\"\n" +
                  "}";

    GlobRepository targetRepository = GlobRepositoryBuilder.createEmpty();
    targetRepository.create(RealAccount.TYPE, value(RealAccount.BUDGEA_ID, "90"));

    JsonImporter importer = new JsonImporter();
    importer.loadTransactions(new StringReader(json), null, targetRepository, null);

    GlobChecker checker = new GlobChecker(PicsouModel.get());
    checker.assertEquals(targetRepository, ImportedTransaction.TYPE,
                         "  <importedTransaction account=\"100\" amount=\"-34.49\" date=\"2016/05/24\" id=\"100\"\n" +
                         "                       ofxName=\"GOOGLE *BUDGEA\"/>\n" +
                         "  <importedTransaction account=\"100\" amount=\"555.9\" date=\"2016/05/22\" id=\"101\"\n" +
                         "                       ofxName=\"SALAIRE\"/>\n");
  }
}
