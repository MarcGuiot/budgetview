package org.designup.picsou.importer;

import junit.framework.TestCase;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.functests.banks.SpecificBankTestCase;
import org.globsframework.utils.Files;

import java.io.File;

public class ObfuscatorTest extends TestCase {

  public void testOfx() throws Exception {
    Obfuscator obfuscator = new Obfuscator();
    File file = new File(SpecificBankTestCase.getFile("banque_pop.ofx", this));
    String s = obfuscator.apply(new TypedInputStream(file));
    assertFalse(s.equals(Files.loadFileToString(file.getAbsolutePath())));
  }

  public void testQif() throws Exception {
    Obfuscator obfuscator = new Obfuscator();
    File file = new File(SpecificBankTestCase.getFile("lcl_money_date_fr.qif", this));
    String s = obfuscator.apply(new TypedInputStream(file));
    assertFalse(s.equals(Files.loadFileToString(file.getAbsolutePath())));
  }

}
