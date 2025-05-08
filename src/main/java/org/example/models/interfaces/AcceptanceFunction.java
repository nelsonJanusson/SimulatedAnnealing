package org.example.models.interfaces;

public interface AcceptanceFunction {
    public boolean accept(double oldCost, double newCost, double temperature);

}
