package org.example.models.Implementations.acceptanceFunction;

import org.example.models.interfaces.AcceptanceFunction;

public class BoltzmannAnnealing implements AcceptanceFunction {
  @Override
  public boolean accept(
      double currentEvaluation, double suggestedValuesEvaluation, double currentTemperature) {
    double difference = currentEvaluation - suggestedValuesEvaluation;
    return Math.random() < 1 / (1 + Math.exp(difference / currentTemperature));
  }
}
