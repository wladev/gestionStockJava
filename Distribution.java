//Distribution.java

import java.time.LocalDate;

public class Distribution {
    private String nom;
    private String prenom;
    private String cp;           // immatriculation
    private String residence;
    private String distribuePar; // nom de l’agent qui distribue
    private LocalDate date;

    public Distribution(String nom, String prenom, String cp, String residence, String distribuePar, LocalDate date) {
        this.nom = nom;
        this.prenom = prenom;
        this.cp = cp;
        this.residence = residence;
        this.distribuePar = distribuePar;
        this.date = date;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getCp() {
        return cp;
    }

    public String getResidence() {
        return residence;
    }

    public String getDistribuePar() {
        return distribuePar;
    }

    public LocalDate getDate() {
        return date;
    }

    // Setters (si tu veux les modifier plus tard)
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public void setDistribuePar(String distribuePar) {
        this.distribuePar = distribuePar;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
