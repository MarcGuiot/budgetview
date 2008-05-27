package org.designup.picsou.client;

import junit.framework.TestCase;

public class AllocationLearningServiceTest extends TestCase {

  public void testSimplify() throws Exception {
    assertEquals("M.N.P.A.F. M.N.P.A.F.",
                 AllocationLearningService.anonymise(null, "M.N.P.A.F. M.N.P.A.F. 8811941800", null));
    assertEquals("EDF PRELEVTS NANTE EDF PR",
                 AllocationLearningService.anonymise(null, "EDF PRELEVTS NANTE *123631470383 21420 728 EDF PR", null));
    assertEquals("GAN VIE-BPF PRIMES GAN-BPF GROUPEES",
                 AllocationLearningService.anonymise(null, "GAN VIE-BPF PRIMES GAN-BPF GROUPEES 13 K161", null));
    assertEquals("R.A.M P.L. PARIS PREL.",
                 AllocationLearningService.anonymise(null, "R.A.M  P.L. PARIS PREL. R.O53T11  1720586194296 ", null));
    assertEquals("VIR.LOGITEL SG CPT",
                 AllocationLearningService.anonymise(null, "VIR.LOGITEL 21.07 SG 04042 CPT 00050741769 ", null));
  }
}
