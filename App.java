
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;

public class App extends JFrame {

    private int stockDisponible = 0;
    private final ArrayList<Distribution> historique = new ArrayList<>();
    private JLabel stockLabel;
    private JTable historiqueTable;
    private DefaultListModel<String> historiqueListModel;

    public App() {
        setTitle("Gestion de stock de batteries");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chargerStockDepuisBase(); // 💡 CHARGE LE STOCK AU DÉMARRAGE

        JTabbedPane onglets = new JTabbedPane();
        onglets.add("Distribuer", creerPanelDistribution());
        onglets.add("Historique", creerPanelHistorique());
        onglets.add("Stock", creerPanelAjout());

        add(onglets);
        chargerHistoriqueDepuisBDD();
        rafraichirHistorique(); // pour mettre à jour la liste affichée
        setVisible(true);
    }

    // Connexion à la base
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://********";
        String user = "******";
        String password = "*******";
        return DriverManager.getConnection(url, user, password);
    }

    // Lecture du stock actuel depuis MySQL
    private void chargerStockDepuisBase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT quantite FROM stock LIMIT 1")) {

            if (rs.next()) {
                stockDisponible = rs.getInt("quantite");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement stock : " + e.getMessage());
        }
    }

    // Mise à jour du stock dans MySQL
    private void mettreAJourStockEnBase() {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("UPDATE stock SET quantite = ? WHERE id = 1")) {
            stmt.setInt(1, stockDisponible);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur mise à jour stock : " + e.getMessage());
        }
    }

    private JPanel creerPanelAjout() {
        JPanel panel = new JPanel(null);

        JLabel label = new JLabel("Quantité à ajouter :");
        label.setBounds(20, 20, 150, 25);
        panel.add(label);

        JTextField champQuantite = new JTextField();
        champQuantite.setBounds(180, 20, 100, 25);
        panel.add(champQuantite);

        JButton boutonAjouter = new JButton("Ajouter au stock");
        boutonAjouter.setBounds(300, 20, 150, 25);
        panel.add(boutonAjouter);

        stockLabel = new JLabel("Stock actuel : " + stockDisponible);
        stockLabel.setBounds(20, 60, 300, 25);
        panel.add(stockLabel);

        boutonAjouter.addActionListener(e -> {
            try {
                int quantite = Integer.parseInt(champQuantite.getText());
                stockDisponible += quantite;
                mettreAJourStockEnBase();
                stockLabel.setText("Stock actuel : " + stockDisponible);
                champQuantite.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
            }
        });

        return panel;
    }

    private JPanel creerPanelDistribution() {
        JPanel panel = new JPanel(null);

        JTextField champNom = champ(panel, "Nom :", 20);
        JTextField champPrenom = champ(panel, "Prénom :", 60);
        JTextField champCP = champ(panel, "Immatriculation (CP) :", 100);
        JTextField champResidence = champ(panel, "Résidence :", 140);
        JTextField champDistribuePar = champ(panel, "Distribué par :", 180);

        JButton boutonDistribuer = new JButton("Distribuer");
        boutonDistribuer.setBounds(200, 230, 120, 30);
        panel.add(boutonDistribuer);

        boutonDistribuer.addActionListener(e -> {
            if (stockDisponible <= 0) {
                JOptionPane.showMessageDialog(this, "Plus de stock disponible !");
                return;
            }

            String nom = champNom.getText().trim();
            String prenom = champPrenom.getText().trim();
            String cp = champCP.getText().trim();
            String residence = champResidence.getText().trim();
            String distribuePar = champDistribuePar.getText().trim();

            if (nom.isEmpty() || prenom.isEmpty() || cp.isEmpty() || residence.isEmpty() || distribuePar.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.");
                return;
            }

            Distribution dist = new Distribution(nom, prenom, cp, residence, distribuePar, LocalDate.now());
            historique.add(dist);
            stockDisponible--;
            mettreAJourStockEnBase();
            stockLabel.setText("Stock actuel : " + stockDisponible);

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO distributions (nom, prenom, cp, residence, distribue_par, date_distribution) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, cp);
                stmt.setString(4, residence);
                stmt.setString(5, distribuePar);
                stmt.setDate(6, java.sql.Date.valueOf(dist.getDate()));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur BDD : " + ex.getMessage());
            }

            champNom.setText("");
            champPrenom.setText("");
            champCP.setText("");
            champResidence.setText("");
            champDistribuePar.setText("");

            rafraichirHistorique();
        });

        return panel;
    }

    private JTextField champ(JPanel panel, String label, int y) {
        JLabel l = new JLabel(label);
        l.setBounds(20, y, 150, 25);
        panel.add(l);

        JTextField champ = new JTextField();
        champ.setBounds(180, y, 200, 25);
        panel.add(champ);

        return champ;
    }

    private JPanel creerPanelHistorique() {
        JPanel panel = new JPanel(new BorderLayout());

        historiqueListModel = new DefaultListModel<>();
        JList<String> liste = new JList<>(historiqueListModel);
        JScrollPane scroll = new JScrollPane(liste);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void rafraichirHistorique() {
        historiqueListModel.clear();
        for (Distribution d : historique) {
            String ligne = String.format(
                    "%s - %s %s | CP: %s | Résidence: %s | Distribué par: %s",
                    d.getDate(), d.getNom(), d.getPrenom(), d.getCp(), d.getResidence(), d.getDistribuePar()
            );
            historiqueListModel.addElement(ligne);
        }
    }

    private void chargerHistoriqueDepuisBDD() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT nom, prenom, cp, residence, distribue_par, date_distribution FROM distributions";
            PreparedStatement stmt = conn.prepareStatement(sql);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String cp = rs.getString("cp");
                String residence = rs.getString("residence");
                String distribuePar = rs.getString("distribue_par");
                LocalDate date = rs.getDate("date_distribution").toLocalDate();

                Distribution d = new Distribution(nom, prenom, cp, residence, distribuePar, date);
                historique.add(d);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement de l'historique : " + ex.getMessage());
        }
    }
}
