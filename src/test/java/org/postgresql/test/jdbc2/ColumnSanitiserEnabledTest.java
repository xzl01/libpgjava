/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.test.jdbc2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.postgresql.core.BaseConnection;
import org.postgresql.test.TestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/*
* This test suite will check the behaviour of the findColumnIndex method. The tests will check the
* behaviour of the method when the sanitiser is enabled. Default behaviour of the driver.
*/
class ColumnSanitiserEnabledTest {
  private Connection conn;

  @BeforeEach
  void setUp() throws Exception {
    Properties props = new Properties();
    props.setProperty("disableColumnSanitiser", Boolean.FALSE.toString());
    conn = TestUtil.openDB(props);
    assertTrue(conn instanceof BaseConnection);
    BaseConnection bc = (BaseConnection) conn;
    assertFalse(bc.isColumnSanitiserDisabled(),
        "Expected state [FALSE] of base connection configuration failed test.");
    TestUtil.createTable(conn, "allmixedup",
        "id int primary key, \"DESCRIPTION\" varchar(40), \"fOo\" varchar(3)");
    Statement data = conn.createStatement();
    data.execute(TestUtil.insertSQL("allmixedup", "1,'mixed case test', 'bar'"));
    data.close();
  }

  protected void tearDown() throws Exception {
    TestUtil.dropTable(conn, "allmixedup");
    TestUtil.closeDB(conn);
  }

  /*
   * Test cases checking different combinations of columns origination from database against
   * application supplied column names.
   */

  @Test
  void tableColumnLowerNowFindFindLowerCaseColumn() throws SQLException {
    findColumn("id", true);
  }

  @Test
  void tableColumnLowerNowFindFindUpperCaseColumn() throws SQLException {
    findColumn("ID", true);
  }

  @Test
  void tableColumnLowerNowFindFindMixedCaseColumn() throws SQLException {
    findColumn("Id", true);
  }

  @Test
  void tableColumnUpperNowFindFindLowerCaseColumn() throws SQLException {
    findColumn("description", true);
  }

  @Test
  void tableColumnUpperNowFindFindUpperCaseColumn() throws SQLException {
    findColumn("DESCRIPTION", true);
  }

  @Test
  void tableColumnUpperNowFindFindMixedCaseColumn() throws SQLException {
    findColumn("Description", true);
  }

  @Test
  void tableColumnMixedNowFindLowerCaseColumn() throws SQLException {
    findColumn("foo", true);
  }

  @Test
  void tableColumnMixedNowFindFindUpperCaseColumn() throws SQLException {
    findColumn("FOO", true);
  }

  @Test
  void tableColumnMixedNowFindFindMixedCaseColumn() throws SQLException {
    findColumn("fOo", true);
  }

  private void findColumn(String label, boolean failOnNotFound) throws SQLException {
    PreparedStatement query = conn.prepareStatement("select * from allmixedup");
    if ((TestUtil.findColumn(query, label) == 0) && failOnNotFound) {
      fail(String.format("Expected to find the column with the label [%1$s].", label));
    }
    query.close();
  }
}
