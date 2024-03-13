import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQL {
    public static void main(String[] args) {
        // Path to your SQLite DB
        String url = "jdbc:sqlite:artifact.db";

        // SQL SELECT query
        String query = "SELECT * FROM artifact;";

        // Load the SQLite JDBC driver (if not using JDBC 4.0 drivers or later)
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Connect to the database and execute the query
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Iterate over the result set and print the records
            while (rs.next()) {
                int artifactId = rs.getInt("ArtifactID");
                String artifactName = rs.getString("ArtifactName");
                String shape = rs.getString("Shape");
                String civilization = rs.getString("Civilization");
                String eraDate = rs.getString("Eradate");

                // Print the record
                System.out.println("ArtifactID: " + artifactId + ", ArtifactName: " + artifactName +
                        ", Shape: " + shape + ", Civilization: " + civilization + ", Eradate: " + eraDate);
            }

        } catch (SQLException e) {
            System.out.println("SQLite error: " + e.getMessage());
        }
    }
}
