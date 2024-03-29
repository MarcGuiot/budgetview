package com.budgetview.persistence.prevayler.users;

import com.budgetview.session.model.HiddenUser;
import com.budgetview.session.model.User;
import junit.framework.TestCase;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;

import java.util.Arrays;

public class PRootDataTest extends TestCase {

  public void testReadWrite() throws Exception {
    PRootData rootData = new PRootData();
    GlobBuilder user = GlobBuilder.init(HiddenUser.TYPE)
      .set(HiddenUser.ENCRYPTED_LINK_INFO, "cryptedLinkInfo")
      .set(HiddenUser.USER_ID, 231);

    rootData.addHiddenUser("cryptedInfo", user.get());
    rootData.addUser("isName",
                     GlobBuilder.init(User.TYPE)
                       .set(User.ENCRYPTED_PASSWORD, "cryptedPass".getBytes())
                       .set(User.LINK_INFO, "linkInfo".getBytes())
                       .set(User.NAME, "isName").get());
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    rootData.write(output.getOutput(), null);
    PRootData actual = new PRootData();
    actual.read(output.getInput(), null);
    Glob glob = actual.getUser("isName");
    assertNotNull(glob);
    assertTrue(Arrays.equals("linkInfo".getBytes(), glob.get(User.LINK_INFO)));
    Glob hiddenUser = actual.getHiddenUser("cryptedInfo");
    assertNotNull(hiddenUser);
    assertEquals(231, hiddenUser.get(HiddenUser.USER_ID).intValue());

  }
}
