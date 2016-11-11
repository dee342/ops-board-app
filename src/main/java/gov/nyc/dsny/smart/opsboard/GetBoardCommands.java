package gov.nyc.dsny.smart.opsboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GetBoardCommands {

	private static final String SQL_DESERIALIZE_OBJECT = "SELECT command FROM board_commands WHERE board_id = ?";

	/**
	 * To de-serialize a java object from database
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("deprecation")
	public static List<String> deSerializeJavaObjectFromDB(Connection connection, String serialized_id)
			throws SQLException, IOException, ClassNotFoundException {
		
		List<String> retval = new ArrayList<String>();
		PreparedStatement pstmt = connection.prepareStatement(SQL_DESERIALIZE_OBJECT);
		pstmt.setString(1, serialized_id);
		ResultSet rs = pstmt.executeQuery();

		LargeObjectManager lobj = ((org.postgresql.PGConnection) connection).getLargeObjectAPI();
		ObjectMapper mapper = new ObjectMapper();
		String buffer = null;

		if (rs != null) {
			while (rs.next()) {

				int oid = rs.getInt("command");
				// Object object = rs.getObject(1);
				LargeObject obj = lobj.open(oid, LargeObjectManager.READ);

				// Read the data
				byte buf[] = new byte[obj.size()];
				obj.read(buf, 0, obj.size());

				// byte[] buf = rs.getBytes("command");
				// long size = rs.getLong("len");

				// System.out.println("Length: " + size);
				ObjectInputStream objectIn = null;
				if (buf != null) {
					objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
				}

				Object deSerializedObject = objectIn.readObject();
				buffer = mapper.writeValueAsString(deSerializedObject);
				retval.add(buffer);
				System.out.println("Java object de-serialized from database. Object: " + deSerializedObject
						+ " Classname: " + deSerializedObject.getClass().getName());
				
				retval.add(buffer);
			}
		}
		rs.close();
		pstmt.close();
		return retval;
	}

	/**
	 * Serialization and de-serialization of java object from mysql
	 *
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException {
		Connection connection = null;

		String driver = "org.postgresql.Driver";
		String url = "jdbc:postgresql://localhost:5432/smart-opsboard";
		String username = "postgres";
		String password = "postgres";
		Class.forName(driver);
		connection = DriverManager.getConnection(url, username, password);
		connection.setAutoCommit(false);

		List<String> jsonStrings = deSerializeJavaObjectFromDB(connection, "BX04_20150415");
		
		for(String json : jsonStrings)
		{
			System.out.println(json);
		}

	}
}