package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.font.FontService;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.utils.DummyImageLocator;
import org.globsframework.gui.splits.utils.DummyTextLocator;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public abstract class SplitsTestCase extends UISpecTestCase {
  protected SplitsBuilder builder;
  protected ColorService colorService = new ColorService();
  protected FontService fontService = new FontService();
  protected DummyImageLocator iconLocator = new DummyImageLocator();
  protected DummyTextLocator textLocator = new DummyTextLocator();
  protected UIService uiService = new UIService();

  protected JTable aTable = new JTable();
  protected JList aList = new JList();
  protected JButton aButton = new JButton();
  protected Directory directory;

  protected void setUp() throws Exception {
    super.setUp();
    aTable.setName("aTable");
    aList.setName("aList");
    aButton.setName("aButton");
    directory = new DefaultDirectory();
    directory.add(colorService);
    directory.add(FontLocator.class, fontService);
    directory.add(TextLocator.class, textLocator);
    directory.add(ImageLocator.class, iconLocator);
    directory.add(UIService.class, uiService);
    builder = new SplitsBuilder(directory);
  }

  protected <T extends Component> T parse(final String xml) throws Exception {
    System.out.println("SplitsTestCase.parse " + xml);
    validateDocument(toStream(xml));
    return builder.setSource(complete(xml)).<T>load();
  }

  protected Component parseWithoutSchemaValidation(String xml) throws Exception {
    return builder.setSource(complete(xml)).load();
  }

  protected StringReader toStream(String xml) {
    return new StringReader(complete(xml));
  }

  private String complete(String xml) {
    return "<splits>" + xml + "</splits>";
  }

  protected void validateDocument(Reader reader) throws Exception {
    String schemaLang = "http://www.w3.org/2001/XMLSchema";
    SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
    Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream("/splits.xsd")));
    Validator validator = schema.newValidator();
    validator.validate(new StreamSource(reader));
  }

  protected void checkParsingError(String xml, String error) throws Exception {
    try {
      parse(xml);
      fail();
    }
    catch (Exception e) {
      checkException(e, error);
    }
  }

  protected void checkException(Exception e, String message) {
    if (!e.getMessage().contains(message)) {
      fail("Exception does not contain message: " + message + "\n" +
           "Actual message is: " + e.getMessage());
    }
  }

  protected void checkGridPos(JPanel panel, Component component,
                              int x, int y, int w, int h,
                              double weightX, double weightY,
                              Fill fill, Anchor anchor, Insets insets) {

    assertEquals(panel, component.getParent());
    assertTrue(Arrays.asList(panel.getComponents()).contains(component));

    GridBagConstraints constraints = getConstraints(panel, component);
    assertEquals(x, constraints.gridx);
    assertEquals(y, constraints.gridy);
    assertEquals(w, constraints.gridwidth);
    assertEquals(h, constraints.gridheight);
    assertEquals(weightX, constraints.weightx);
    assertEquals(weightY, constraints.weighty);
    assertEquals(fill.getValue(), constraints.fill);
    assertEquals(anchor.getValue(), constraints.anchor);
    assertEquals(insets, constraints.insets);
  }

  protected GridBagConstraints getConstraints(Component parent, Component component) {
    JPanel panel = (JPanel)parent;
    GridBagLayout layout = (GridBagLayout)panel.getLayout();
    return layout.getConstraints(component);
  }

  protected Insets getInsets(Component parent, Component component) {
    JPanel panel = (JPanel)parent;
    if (panel.getLayout() instanceof SingleComponentLayout) {
      return ((SingleComponentLayout)panel.getLayout()).getInsets();
    }
    GridBagLayout layout = (GridBagLayout)panel.getLayout();
    return layout.getConstraints(component).insets;
  }
}
