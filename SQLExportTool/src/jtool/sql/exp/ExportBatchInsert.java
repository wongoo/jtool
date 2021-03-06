/**
 * Created Date: Dec 14, 2011 9:35:31 AM
 */
package jtool.sql.exp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import jtool.sql.domain.Column;
import jtool.sql.util.JdbcUtil;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geln Yang
 * @version 1.0
 */
public class ExportBatchInsert {
  private static final Logger logger = LoggerFactory.getLogger(ExportBatchInsert.class);

  public static void main(String[] args) throws Exception {
    String driverName = args[0];
    String linkUrl = args[1];
    String userName = args[2];
    String password = args[3];
    String saveFileName = args[4];
    String batchSql = args[5];
    String batchSqlFilePath = "./" + batchSql;
    String outputFilePath = "./" + saveFileName + ".sql";

    logger.info(driverName);
    logger.info(linkUrl);
    logger.info(userName);
    logger.info(password);
    logger.info(batchSql);
    logger.info(outputFilePath);

    Class.forName(driverName).newInstance();
    Connection connection = DriverManager.getConnection(linkUrl, userName, password);

    StringBuffer buffer = new StringBuffer();
    List<String> lines =
        FileUtils.readLines(new File(batchSqlFilePath), ExportConstants.DEFAULT_ENCODE);

    if (lines != null) {
      for (String sql : lines) {
        Statement myStmt = connection.createStatement();
        ResultSet rs = myStmt.executeQuery(sql);
        ResultSetMetaData rmeta = rs.getMetaData();

        String sqlString = sql.toUpperCase();
        int fromIndex = sqlString.indexOf("FROM");
        sqlString = sqlString.substring(fromIndex + 4).trim();
        int blankIndex = sqlString.indexOf(" ");
        String tableName = sqlString.substring(0, blankIndex);

        Map<String, Column> columnMap = JdbcUtil.getColumnMap(connection, userName, tableName);

        StringBuffer result = SqlExportUtil.exportInsert(rmeta, rs, tableName, columnMap);
        buffer.append("-- " + tableName + ExportConstants.NEW_LINE);
        buffer.append(result + ExportConstants.NEW_LINE);
        rs.close();
        myStmt.close();
      }
    }

    SqlExportUtil.saveToFile(outputFilePath, buffer);

    connection.close();
    logger.info("------------------------");
    logger.info("over");
  }
}
