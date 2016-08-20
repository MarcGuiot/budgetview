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
    String json = "{\n" +
                  "    \"position_month\": 201608,\n" +
                  "    \"number\": \"3002900000\",\n" +
                  "    \"provider_bank_id\": 40,\n" +
                  "    \"provider_account_id\": 614,\n" +
                  "    \"provider\": 2,\n" +
                  "    \"provider_bank_name\": \"Connecteur de test\",\n" +
                  "    \"name\": \"Compte chèque\",\n" +
                  "    \"position_day\": 10,\n" +
                  "    \"id\": 1,\n" +
                  "    \"position\": 9346.71,\n" +
                  "    \"type\": \"checking\",\n" +
                  "    \"transactions\": [{\n" +
                  "      \"amount\": -96.02,\n" +
                  "      \"bank_date\": \"2016-08-10\",\n" +
                  "      \"category_name\": \"Indéfini\",\n" +
                  "      \"category_id\": 9998,\n" +
                  "      \"provider\": null,\n" +
                  "      \"provider_id\": null,\n" +
                  "      \"operation_date\": \"2016-08-09\",\n" +
                  "      \"id\": 1,\n" +
                  "      \"label\": \"GREEN ET GREEN PARIS\",\n" +
                  "      \"original_label\": \"FACTURE CB GREEN ET GREEN PARIS\"\n" +
                  "    }]\n" +
                  "  }";

    GlobRepository targetRepository = GlobRepositoryBuilder.createEmpty();
    targetRepository.create(RealAccount.TYPE, value(RealAccount.PROVIDER_ACCOUNT_ID, 90));

    JsonImporter importer = new JsonImporter();
    importer.loadTransactions(new StringReader(json), null, targetRepository, null);

    GlobChecker checker = new GlobChecker(PicsouModel.get());
    checker.assertEquals(targetRepository, ImportedTransaction.TYPE,
                         "");
  }
}
