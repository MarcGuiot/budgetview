package org.designup.picsou.client;

import junit.framework.TestCase;
import org.designup.picsou.model.Transaction;

public class AllocationLearningServiceTest extends TestCase {

  public void testSimplify() throws Exception {
    assertEquals("M.N.P.A.F. M.N.P.A.F.",
                 Transaction.anonymise(null, "M.N.P.A.F. M.N.P.A.F. 8811941800", null));
    assertEquals("EDF PRELEVTS NANTE EDF PR",
                 Transaction.anonymise(null, "EDF PRELEVTS NANTE *123631470383 21420 728 EDF PR", null));
    assertEquals("GAN VIE-BPF PRIMES GAN-BPF GROUPEES",
                 Transaction.anonymise(null, "GAN VIE-BPF PRIMES GAN-BPF GROUPEES 13 K161", null));
    assertEquals("R.A.M P.L. PARIS PREL.",
                 Transaction.anonymise(null, "R.A.M  P.L. PARIS PREL. R.O53T11  1720586194296 ", null));
    assertEquals("VIR.LOGITEL SG CPT",
                 Transaction.anonymise(null, "VIR.LOGITEL 21.07 SG 04042 CPT 00050741769 ", null));
  }
}
