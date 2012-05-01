package org.globsframework.utils.stream;

import junit.framework.TestCase;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementInputStreamTest extends TestCase {

  public void testReplace() throws Exception {
    ReplacementInputStreamBuilder builder = new ReplacementInputStreamBuilder();
    builder.replace("somm data", "some data");
    builder.replace("somm tata", "somm titi");
    builder.replace("toto", "titi");

    check(builder, "somm data to toto", "some data to titi");
  }

  public void testRegexp() throws Exception {
    ReplacementInputStreamBuilder builder = new ReplacementInputStreamBuilder();
    builder.replace("<SCRIPT language=javascript>.*</SCRIPT>\n" +
                    "</font>",
                    "<INPUT size=\"10\" maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled &gt;>\n" +
                    "</font>");

    check(builder, "<font face=\"Arial, Helvetica, sans-serif\" class=\"gras\">\n" +
                   "<font size=\"2\"><b style=\"color:#000000;font-size:12px;font-weight:900;\">Code secret</b>\n" +
                   "<SCRIPT language=javascript>\n" +
                   "  if (navigator.appName != \"Microsoft Internet Explorer\") { document.write('<INPUT size=\"5\" '); } \n" +
                   "   else { document.write('<INPUT size=\"10\" '); }\n" +
                   "   </SCRIPT>\n" +
                   "     maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled &gt;>\n" +
                   "</font>",
          "<font face=\"Arial, Helvetica, sans-serif\" class=\"gras\">\n" +
          "<font size=\"2\"><b style=\"color:#000000;font-size:12px;font-weight:900;\">Code secret</b>\n" +
          "<INPUT size=\"10\" maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled &gt;>\n" +
          "</font>");

  }

  public void testSimple() throws Exception {
    ReplacementInputStreamBuilder builder = new ReplacementInputStreamBuilder();
    builder.replace("a", "b");
    builder.replace("bb", "bc");
    builder.replace("bbc", "cb");

    check(builder, "a cat", "b cbt");
    check(builder, "bb dat", "bc dbt");
    check(builder, "bbc cat", "cb cbt");
    check(builder, "bzbze babc cat", "bzbze bbbc cbt");
    check(builder, "abb bb bbc sdf bbdat", "bbc bc cb sdf bcdbt");
    check(builder, "bbcbbcbbcbbc", "cbcbcbcb");
    check(builder, "bb bb bba bba a a aaa s bb", "bc bc bcb bcb b b bbb s bc");
  }

  private void check(ReplacementInputStreamBuilder builder, final String input, final String expected) throws IOException {
    assertEquals(expected, new BufferedReader(new InputStreamReader(builder.create(new ByteArrayInputStream(input.getBytes())))).readLine());
  }

  public void testBigFile() throws Exception {
    File regexp = createFile();
    ReplacementInputStreamBuilder builder = new ReplacementInputStreamBuilder();
    builder.replace("bofy ", "body ");
    InputStream stream = builder.create(new BufferedInputStream(new FileInputStream(regexp)));
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    read2(regexp);
    read1(reader);    
  }
  

  private void read2(File regexp) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(regexp));
    long start = System.currentTimeMillis();
    String s = reader.readLine();
    File file = createFile();
    OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
    while (s != null) {
      int i = 0;
      int p = 0;
      while (true){
        i = s.indexOf("bofy", i + 1);
        if (i > 0){
          stream.write(s.substring(p, i).getBytes());
          stream.write("body".getBytes());
          p = i + 4;
        }
        else {
          break;
        }
      }
      s = reader.readLine();
    }
    long stop = System.currentTimeMillis();
    System.out.println("ReplacementInputStreamTest.testBigFile indexOf  " + (stop - start));
  }

  private void read1(BufferedReader reader) throws IOException {
    long start = System.currentTimeMillis();
    String s = reader.readLine();
    while (s != null) {
      s = reader.readLine();
    }
    long stop = System.currentTimeMillis();
    System.out.println("ReplacementInputStreamTest.testBigFile replacement stream  " + (stop - start));
  }


  private File createFile() throws IOException {
    File regexp = File.createTempFile("regexp", ".txt");
    FileWriter writer = new FileWriter(regexp);
    for (int i = 0; i < 1000; i++) {
      writer.append("<head >\n");
      writer.append("<bofy > lksjlfjslkfjlskjflksjdflkjsdfkljl lkj <bofy >\n");
      writer.append("<body > skdfjskjfnksjnfkjsndfkjn skfj skjnf ksjnf k<body><body><body><body><body>\n");
      writer.append("</head >\n");
    }
    return regexp;
  }
  
  public void testRegexp2() throws Exception {
    Pattern compile = Pattern.compile(".*a.*b.*c.*d.*");
    Matcher matcher = compile.matcher("sdfasd fza,s  cds bs  sdf  c  sd ka  b c   d sdf d");
    assertTrue(matcher.matches());

  }
}
