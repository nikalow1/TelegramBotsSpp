package org.example.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Zone {
    @Id
    private String engName;
    private String ruName;

    public Zone(){}

    public Zone(String engName, String ruName){
        this.engName = engName;
        this.ruName = ruName;
    }

    public String getEngName() {
        return engName;
    }

    public String getRuName() {
        return ruName;
    }
}
