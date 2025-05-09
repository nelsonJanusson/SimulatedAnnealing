package org.example.models.interfaces;

public interface AcceptanceFunction {
  public boolean accept(
      double currentEvaluation, double suggestedValuesEvaluation, double currentTemperature);
}
