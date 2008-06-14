package org.globsframework.saxstack.writer;

import java.io.StringWriter;

public class XmlPrettyPrinterTest extends SaxStackWriterTestCase {

  public void test() throws Exception {
    StringWriter writer = new StringWriter();
    XmlPrettyPrinter.write(writer, new FixedXmlNodeBuilderTest.RootExporter(root, false), 1);
    assertEquals(
      "<contacts>\n" +
      "  <contact name=\"me\"\n" +
      "           phone=\"512\"/>\n" +
      "  <category name=\"family\">\n" +
      "    <contacts>\n" +
      "      <contact name=\"Grandpa\"\n" +
      "               phone=\"567\"/>\n" +
      "      <category name=\"home\">\n" +
      "        <contact name=\"Bart\"\n" +
      "                 phone=\"756\"/>\n" +
      "        <contact name=\"Homer\"\n" +
      "                 phone=\"757\"/>\n" +
      "      </category>\n" +
      "    </contacts>\n" +
      "  </category>\n" +
      "  <category name=\"work\">\n" +
      "    <contacts>\n" +
      "      <contact name=\"Ralph\"\n" +
      "               phone=\"123\"/>\n" +
      "    </contacts>\n" +
      "  </category>\n" +
      "</contacts>", writer.toString());
  }


  public void testVeryLongIndent() throws Exception {
    Category root = new Category("root");
    Category tmp = root;
    for (int i = 0; i < 100; i++) {
      Category category = new Category(Integer.toString(i));
      tmp.addCategory(category);
      tmp = category;
    }
    StringWriter writer = new StringWriter();
    XmlPrettyPrinter.write(writer, new SaxStackWriterWithFixedTagNamesTest.RootBuilder(root), 2);
    assertEquals("<contacts>\n" +
                 "  <category name=\"0\">\n" +
                 "    <category name=\"1\">\n" +
                 "      <category name=\"2\">\n" +
                 "        <category name=\"3\">\n" +
                 "          <category name=\"4\">\n" +
                 "            <category name=\"5\">\n" +
                 "              <category name=\"6\">\n" +
                 "                <category name=\"7\">\n" +
                 "                  <category name=\"8\">\n" +
                 "                    <category name=\"9\">\n" +
                 "                      <category name=\"10\">\n" +
                 "                        <category name=\"11\">\n" +
                 "                          <category name=\"12\">\n" +
                 "                            <category name=\"13\">\n" +
                 "                              <category name=\"14\">\n" +
                 "                                <category name=\"15\">\n" +
                 "                                  <category name=\"16\">\n" +
                 "                                    <category name=\"17\">\n" +
                 "                                      <category name=\"18\">\n" +
                 "                                        <category name=\"19\">\n" +
                 "                                          <category name=\"20\">\n" +
                 "                                            <category name=\"21\">\n" +
                 "                                              <category name=\"22\">\n" +
                 "                                                <category name=\"23\">\n" +
                 "                                                  <category name=\"24\">\n" +
                 "                                                    <category name=\"25\">\n" +
                 "                                                      <category name=\"26\">\n" +
                 "                                                        <category name=\"27\">\n" +
                 "                                                          <category name=\"28\">\n" +
                 "                                                            <category name=\"29\">\n" +
                 "                                                              <category name=\"30\">\n" +
                 "                                                                <category name=\"31\">\n" +
                 "                                                                  <category name=\"32\">\n" +
                 "                                                                    <category name=\"33\">\n" +
                 "                                                                      <category name=\"34\">\n" +
                 "                                                                        <category name=\"35\">\n" +
                 "                                                                          <category name=\"36\">\n" +
                 "                                                                            <category name=\"37\">\n" +
                 "                                                                              <category name=\"38\">\n" +
                 "                                                                                <category name=\"39\">\n" +
                 "                                                                                  <category name=\"40\">\n" +
                 "                                                                                    <category name=\"41\">\n" +
                 "                                                                                      <category name=\"42\">\n" +
                 "                                                                                        <category name=\"43\">\n" +
                 "                                                                                          <category name=\"44\">\n" +
                 "                                                                                            <category name=\"45\">\n" +
                 "                                                                                              <category name=\"46\">\n" +
                 "                                                                                                <category name=\"47\">\n" +
                 "                                                                                                  <category name=\"48\">\n" +
                 "                                                                                                    <category name=\"49\">\n" +
                 "                                                                                                      <category name=\"50\">\n" +
                 "                                                                                                        <category name=\"51\">\n" +
                 "                                                                                                          <category name=\"52\">\n" +
                 "                                                                                                            <category name=\"53\">\n" +
                 "                                                                                                              <category name=\"54\">\n" +
                 "                                                                                                                <category name=\"55\">\n" +
                 "                                                                                                                  <category name=\"56\">\n" +
                 "                                                                                                                    <category name=\"57\">\n" +
                 "                                                                                                                      <category name=\"58\">\n" +
                 "                                                                                                                        <category name=\"59\">\n" +
                 "                                                                                                                          <category name=\"60\">\n" +
                 "                                                                                                                            <category name=\"61\">\n" +
                 "                                                                                                                              <category name=\"62\">\n" +
                 "                                                                                                                                <category name=\"63\">\n" +
                 "                                                                                                                                  <category name=\"64\">\n" +
                 "                                                                                                                                    <category name=\"65\">\n" +
                 "                                                                                                                                      <category name=\"66\">\n" +
                 "                                                                                                                                        <category name=\"67\">\n" +
                 "                                                                                                                                          <category name=\"68\">\n" +
                 "                                                                                                                                            <category name=\"69\">\n" +
                 "                                                                                                                                              <category name=\"70\">\n" +
                 "                                                                                                                                                <category name=\"71\">\n" +
                 "                                                                                                                                                  <category name=\"72\">\n" +
                 "                                                                                                                                                    <category name=\"73\">\n" +
                 "                                                                                                                                                      <category name=\"74\">\n" +
                 "                                                                                                                                                        <category name=\"75\">\n" +
                 "                                                                                                                                                          <category name=\"76\">\n" +
                 "                                                                                                                                                            <category name=\"77\">\n" +
                 "                                                                                                                                                              <category name=\"78\">\n" +
                 "                                                                                                                                                                <category name=\"79\">\n" +
                 "                                                                                                                                                                  <category name=\"80\">\n" +
                 "                                                                                                                                                                    <category name=\"81\">\n" +
                 "                                                                                                                                                                      <category name=\"82\">\n" +
                 "                                                                                                                                                                        <category name=\"83\">\n" +
                 "                                                                                                                                                                          <category name=\"84\">\n" +
                 "                                                                                                                                                                            <category name=\"85\">\n" +
                 "                                                                                                                                                                              <category name=\"86\">\n" +
                 "                                                                                                                                                                                <category name=\"87\">\n" +
                 "                                                                                                                                                                                  <category name=\"88\">\n" +
                 "                                                                                                                                                                                    <category name=\"89\">\n" +
                 "                                                                                                                                                                                      <category name=\"90\">\n" +
                 "                                                                                                                                                                                        <category name=\"91\">\n" +
                 "                                                                                                                                                                                          <category name=\"92\">\n" +
                 "                                                                                                                                                                                            <category name=\"93\">\n" +
                 "                                                                                                                                                                                              <category name=\"94\">\n" +
                 "                                                                                                                                                                                                <category name=\"95\">\n" +
                 "                                                                                                                                                                                                  <category name=\"96\">\n" +
                 "                                                                                                                                                                                                    <category name=\"97\">\n" +
                 "                                                                                                                                                                                                      <category name=\"98\">\n" +
                 "                                                                                                                                                                                                        <category name=\"99\"/>\n" +
                 "                                                                                                                                                                                                      </category>\n" +
                 "                                                                                                                                                                                                    </category>\n" +
                 "                                                                                                                                                                                                  </category>\n" +
                 "                                                                                                                                                                                                </category>\n" +
                 "                                                                                                                                                                                              </category>\n" +
                 "                                                                                                                                                                                            </category>\n" +
                 "                                                                                                                                                                                          </category>\n" +
                 "                                                                                                                                                                                        </category>\n" +
                 "                                                                                                                                                                                      </category>\n" +
                 "                                                                                                                                                                                    </category>\n" +
                 "                                                                                                                                                                                  </category>\n" +
                 "                                                                                                                                                                                </category>\n" +
                 "                                                                                                                                                                              </category>\n" +
                 "                                                                                                                                                                            </category>\n" +
                 "                                                                                                                                                                          </category>\n" +
                 "                                                                                                                                                                        </category>\n" +
                 "                                                                                                                                                                      </category>\n" +
                 "                                                                                                                                                                    </category>\n" +
                 "                                                                                                                                                                  </category>\n" +
                 "                                                                                                                                                                </category>\n" +
                 "                                                                                                                                                              </category>\n" +
                 "                                                                                                                                                            </category>\n" +
                 "                                                                                                                                                          </category>\n" +
                 "                                                                                                                                                        </category>\n" +
                 "                                                                                                                                                      </category>\n" +
                 "                                                                                                                                                    </category>\n" +
                 "                                                                                                                                                  </category>\n" +
                 "                                                                                                                                                </category>\n" +
                 "                                                                                                                                              </category>\n" +
                 "                                                                                                                                            </category>\n" +
                 "                                                                                                                                          </category>\n" +
                 "                                                                                                                                        </category>\n" +
                 "                                                                                                                                      </category>\n" +
                 "                                                                                                                                    </category>\n" +
                 "                                                                                                                                  </category>\n" +
                 "                                                                                                                                </category>\n" +
                 "                                                                                                                              </category>\n" +
                 "                                                                                                                            </category>\n" +
                 "                                                                                                                          </category>\n" +
                 "                                                                                                                        </category>\n" +
                 "                                                                                                                      </category>\n" +
                 "                                                                                                                    </category>\n" +
                 "                                                                                                                  </category>\n" +
                 "                                                                                                                </category>\n" +
                 "                                                                                                              </category>\n" +
                 "                                                                                                            </category>\n" +
                 "                                                                                                          </category>\n" +
                 "                                                                                                        </category>\n" +
                 "                                                                                                      </category>\n" +
                 "                                                                                                    </category>\n" +
                 "                                                                                                  </category>\n" +
                 "                                                                                                </category>\n" +
                 "                                                                                              </category>\n" +
                 "                                                                                            </category>\n" +
                 "                                                                                          </category>\n" +
                 "                                                                                        </category>\n" +
                 "                                                                                      </category>\n" +
                 "                                                                                    </category>\n" +
                 "                                                                                  </category>\n" +
                 "                                                                                </category>\n" +
                 "                                                                              </category>\n" +
                 "                                                                            </category>\n" +
                 "                                                                          </category>\n" +
                 "                                                                        </category>\n" +
                 "                                                                      </category>\n" +
                 "                                                                    </category>\n" +
                 "                                                                  </category>\n" +
                 "                                                                </category>\n" +
                 "                                                              </category>\n" +
                 "                                                            </category>\n" +
                 "                                                          </category>\n" +
                 "                                                        </category>\n" +
                 "                                                      </category>\n" +
                 "                                                    </category>\n" +
                 "                                                  </category>\n" +
                 "                                                </category>\n" +
                 "                                              </category>\n" +
                 "                                            </category>\n" +
                 "                                          </category>\n" +
                 "                                        </category>\n" +
                 "                                      </category>\n" +
                 "                                    </category>\n" +
                 "                                  </category>\n" +
                 "                                </category>\n" +
                 "                              </category>\n" +
                 "                            </category>\n" +
                 "                          </category>\n" +
                 "                        </category>\n" +
                 "                      </category>\n" +
                 "                    </category>\n" +
                 "                  </category>\n" +
                 "                </category>\n" +
                 "              </category>\n" +
                 "            </category>\n" +
                 "          </category>\n" +
                 "        </category>\n" +
                 "      </category>\n" +
                 "    </category>\n" +
                 "  </category>\n" +
                 "</contacts>", writer.toString());
  }
}
