package org.designup.picsou.client;

import junit.framework.TestCase;
import org.designup.picsou.model.Transaction;

public class AllocationLearningServiceTest extends TestCase {

  public void testAnonymize() throws Exception {

    check("Menu K",
          "Menu K 11");

    check("M.N.P.A.F. M.N.P.A.F.",
          "M.N.P.A.F. M.N.P.A.F. 8811941800");

    check("EDF PRELEVTS NANTE EDF PR",
          "EDF PRELEVTS NANTE *123631470383 21420 728 EDF PR");

    check("GAN VIE-BPF PRIMES GAN-BPF GROUPEES",
          "GAN VIE-BPF PRIMES GAN-BPF GROUPEES 13 K161");

    check("R.A.M P.L. PARIS PREL.",
          "R.A.M  P.L. PARIS PREL. R.O53T11  1720586194296 ");

    check("VIR.LOGITEL SG CPT",
          "VIR.LOGITEL 21.07 SG 04042 CPT 00050741769 ");

    check("VIR.LOGITEL",
          "VIR.LOGITEL 999 ");
  }

  private void check(String expected, String input) {
    assertEquals(expected,
                 Transaction.anonymise(null, input, null));
  }
}
