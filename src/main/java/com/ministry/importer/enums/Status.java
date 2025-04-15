package com.ministry.importer.enums;

public enum Status {
    ACTIF("Actif"),
    INACTIF("Inactif"),
    SUSPENDU("Suspendu"),
    EN_ATTENTE("En attente");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Status fromLabel(String label) {
        for (Status status : Status.values()) {
            if (status.getLabel().equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut non valide: " + label);
    }
}
