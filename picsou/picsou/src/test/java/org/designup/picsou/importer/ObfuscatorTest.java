package org.designup.picsou.importer;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.designup.picsou.functests.specificbanks.SpecificBankTestCase;
import org.designup.picsou.importer.utils.TypedInputStream;

import java.io.File;
import java.io.IOException;

public class ObfuscatorTest extends TestCase {

  public void testOfx() throws Exception {
    String obfuscated = obfuscate("banque_pop.ofx");
    checkContains(obfuscated,
                  "OFXHEADER:100\n" +
                  "DATA:OFXSGML\n" +
                  "VERSION:102\n" +
                  "SECURITY:NONE\n" +
                  "ENCODING:USASCII\n" +
                  "CHARSET:1252\n" +
                  "COMPRESSION:NONE\n" +
                  "OLDFILEUID:NONE\n" +
                  "NEWFILEUID:NONE\n" +
                  "<OFX>\n" +
                  "<SIGNONMSGSRSV1>\n" +
                  "<SONRS>\n" +
                  "<STATUS>\n" +
                  "<CODE>0");

    checkContains(obfuscated,
                  "<STMTTRN>\n" +
                  "<TRNTYPE>DEBIT\n" +
                  "<DTPOSTED>20090205\n" +
                  "<TRNAMT>1000010\n" +
                  "<FITID>200900200006BD27\n" +
                  "<CHECKNUM>0wxy1z2\n" +
                  "<NAME>stuv abcdefgh ijklmno\n" +
                  "<MEMO>*345p6789012 34 567890 p.qrstuvwx: 123456\n" +
                  "</STMTTRN>");

    checkContains(obfuscated,
                  "<LEDGERBAL>\n" +
                  "<BALAMT>1000015\n" +
                  "<DTASOF>20090214\n" +
                  "</LEDGERBAL>\n" +
                  "<AVAILBAL>\n" +
                  "<BALAMT>1000015\n" +
                  "<DTASOF>20090214\n" +
                  "</AVAILBAL>");
  }

  public void testQif() throws Exception {
    String obfuscated = obfuscate("lcl_money_date_fr.qif");

    checkContains(obfuscated,
                  "!Type:Bank\n" +
                  "D29/05/08\n" +
                  "T1000000\n" +
                  "Nbcdef\n" +
                  "Phi  jklmnop qr  01/23\n" +
                  "L\n" +
                  "^");

    checkContains(obfuscated,
                  "D29/05/08\n" +
                  "T1000001\n" +
                  "Nuvwxy\n" +
                  "Pab  cd efghij kl456  78/90/12\n" +
                  "L\n" +
                  "^");

    checkContains(obfuscated,
                  "D05/06/08\n" +
                  "T1000011\n" +
                  "Ntuvwxyza\n" +
                  "Mcde fgh ij klmnopq rs tuvwxy\n" +
                  "^");
  }

  public void testQifWithSplitTransactions() throws Exception {
    String obfuscated = obfuscate("money_export_standard_2.qif");
    checkContains(obfuscated,
                  "D20/06'2008\n" +
                  "Mvwxyzab cdefgh\n" +
                  "T1000003\n" +
                  "N0123\n" +
                  "Pklmno\n" +
                  "Lqrstuvwxyzab:cdefghij\n" +
                  "SAlimentation:Epicerie\n" +
                  "ECourses quelconques\n" +
                  "$1000004\n" +
                  "SLoisirs-culture-sport:Sport\n" +
                  "EMotomag\n" +
                  "$1000005\n" +
                  "^");
  }

  private String obfuscate(String fileNameToImport) throws IOException {
    Obfuscator obfuscator = new Obfuscator();
    File file = new File(SpecificBankTestCase.getFile(fileNameToImport, this));
    return obfuscator.apply(new TypedInputStream(file));
  }

  private void checkContains(String file, String content) {
    if (!file.contains(content)) {
      Assert.fail("Content:\n\n" + content + "\n\nnot found in:\n\n" + file);
    }
  }
}
