package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {

    String fileName = "tmp/demo.ofx";

    OfxBuilder.init(fileName)
      .addBankAccount(30006, 10678, "00000123456", 560.20, "2008/11/15")
      // Income
      .addTransaction("2008/10/02", 2560.50, "WORLDCO")
      .addTransaction("2008/10/05", 2112.80, "BIGCORP")
      .addTransaction("2008/11/02", 2560.50, "WORLDCO")
      .addTransaction("2008/11/05", 2112.80, "BIGCORP")
      // Fixed
      .addTransaction("2008/10/09", 1250.00, "PRET IMMO N.3325566")
      .addTransaction("2008/11/09", 1250.00, "PRET IMMO N.3325566")
      .addTransaction("2008/10/20", 289.75, "PRET CONSO N.6784562 F657")
      .addTransaction("2008/10/13", 83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/11/15", 83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/10/05", 270.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/11/05", 270.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/10/02", 70.30, "RATP NAVIGO 10/08")
      .addTransaction("2008/11/02", 70.30, "RATP NAVIGO 10/08")
      .addTransaction("2008/10/17", 97.00, "GROUPE SCOLAIRE R.L OCT. 2008")
      .addTransaction("2008/11/17", 97.00, "GROUPE SCOLAIRE R.L NOV. 2008")
      .addTransaction("2008/10/11", 25.50, "TVSAT")
      .addTransaction("2008/11/12", 25.50, "TVSAT")
      .addTransaction("2008/10/08", 45.30, "M TELECOM")
      .addTransaction("2008/11/10", 66.10, "M TELECOM")
      .addTransaction("2008/10/02", 29.90, "OPTIBOX TEL.")
      .addTransaction("2008/11/02", 29.90, "OPTIBOX TEL.")
      // Envelopes
      .addTransaction("2008/10/02", 122.60, "HYPER M")
      .addTransaction("2008/10/07", 260.30, "HYPER M")
      .addTransaction("2008/10/15", 160.00, "HYPER M")
      .addTransaction("2008/10/23", 260.30, "HYPER M")
      .addTransaction("2008/10/08", 20, "RETRAIT GAB 4463")
      .addTransaction("2008/10/12", 40, "RETRAIT GAB 4463")
      .addTransaction("2008/10/22", 20, "RETRAIT GAB 4463")
      .addTransaction("2008/10/30", 20, "RETRAIT GAB 4463")
      .addTransaction("2008/11/01", 20, "RETRAIT GAB 4463")
      .addTransaction("2008/11/09", 20, "RETRAIT GAB 4463")
      // OCCASIONAL
      .save();

    operations.importOfxFile(fileName);

    views.selectCategorization();
    categorization.setIncome("WORLDCO", "Salaire Henri", true);
    categorization.setIncome("BIGCORP", "Salaire Henri", true);
    categorization.setRecurring("PRET IMMO N.3325566", "Credit immo", MasterCategory.HOUSE, true);
    categorization.setRecurring("PRET CONSO N.6784562 F657", "Credit auto", MasterCategory.TRANSPORTS, true);

  }
}
