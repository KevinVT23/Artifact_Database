import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ArtifactSearchGUI extends JFrame {
    private JTextField searchText;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JLabel statusBar;
    private JLabel imageLabel;
    private static final String URL = "jdbc:sqlite:artifact.db";
    private Map<Integer, String> artifactDescriptions = new HashMap<>();
    public ArtifactSearchGUI() {
        super("Artifact Database Search");
        loadArtifactDescriptions();
        initializeUI();
        searchDatabase("");
    }
    private void setStatusBarText(String text) {
        int maxWidth = statusBar.getWidth();
        String htmlText = "<html><body style='width: " + maxWidth + "px; white-space: pre-wrap;'>" + text.replace("\n", "<br>") + "</body></html>";
        statusBar.setText(htmlText);
    }

    private void initializeUI() {
        searchText = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchText);
        searchPanel.add(searchButton);

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> onArtifactSelected());
        list.setFont(new Font("Arial", Font.PLAIN, 12));
        list.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        imageLabel = new JLabel();
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        statusBar = new JLabel(" Status: Ready");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(statusBar.getWidth(), 40)); // Increase the height

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        bottomPanel.add(imageLabel, BorderLayout.CENTER);

        this.setLayout(new BorderLayout(10, 10));
        this.add(searchPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.WEST);
        this.add(bottomPanel, BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchDatabase(searchText.getText().trim()));

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

private void onArtifactSelected() {
    if (!list.getValueIsAdjusting()) {
        String selectedValue = list.getSelectedValue();
        if (selectedValue != null && !selectedValue.isEmpty()) {
            try {
                int artifactId = Integer.parseInt(selectedValue.split(",")[0].split(":")[1].trim());
                String description = artifactDescriptions.get(artifactId);
                if (description != null) {
                    setStatusBarText("Description: " + description);
                } else {
                    setStatusBarText("Description not available.");
                }
                showArtifactImage(artifactId);
            } catch (NumberFormatException ex) {
                statusBar.setText("Error: Invalid Artifact ID.");
            }
        }
    }
}


    private void searchDatabase(String searchQuery) {
        String query = searchQuery.isEmpty() ? "SELECT * FROM artifact;" :
                "SELECT * FROM artifact WHERE ArtifactName LIKE '%" + searchQuery + "%' OR " +
                        "Shape LIKE '%" + searchQuery + "%' OR " +
                        "Civilization LIKE '%" + searchQuery + "%' OR " +
                        "Eradate LIKE '%" + searchQuery + "%';";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            listModel.clear();
            int count = 0;

            while (rs.next()) {
                listModel.addElement(formatArtifactRecord(rs));
                count++;
            }

            if (count == 0) {
                listModel.addElement("No results found.");
                statusBar.setText(" Status: No results found.");
            } else {
                setStatusBarText(" Status: " + count + " items found.");
            }

        } catch (SQLException ex) {
            listModel.clear();
            listModel.addElement("SQLite error: " + ex.getMessage());
            statusBar.setText(" SQLite error: " + ex.getMessage());
        }
    }

    private String formatArtifactRecord(ResultSet rs) throws SQLException {
        int artifactId = rs.getInt("ArtifactID");
        String artifactName = rs.getString("ArtifactName");
        String shape = rs.getString("Shape");
        String civilization = rs.getString("Civilization");
        String eraDate = rs.getString("Eradate");
        return "ArtifactID: " + artifactId + ", ArtifactName: " + artifactName +
                ", Shape: " + shape + ", Civilization: " + civilization + ", Eradate: " + eraDate;
    }

    private void showArtifactImage(int artifactId) {
        String imagePath = "artifact/" + artifactId + ".jpg";
        ImageIcon originalIcon = new ImageIcon(imagePath);

        if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
            Image originalImage = originalIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            imageLabel.setIcon(scaledIcon);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Image not available");
        }
    }
    private void loadArtifactDescriptions() {
        try (BufferedReader reader = new BufferedReader(new FileReader("artifactText.txt"))) {
            String line;
            int lineNumber = 1;  // Start from 1 as Artifact IDs are assumed to start from 1
            while ((line = reader.readLine()) != null) {
                artifactDescriptions.put(lineNumber, line.trim());
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArtifactSearchGUI::new);
    }
}
