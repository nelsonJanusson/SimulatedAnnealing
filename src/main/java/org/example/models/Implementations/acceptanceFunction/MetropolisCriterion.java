package org.example.models.Implementations.acceptanceFunction;

import org.example.models.interfaces.AcceptanceFunction;

public class MetropolisCriterion implements AcceptanceFunction {
  @Override
  public boolean accept(
      double currentEvaluation, double suggestedValuesEvaluation, double currentTemperature) {
    double difference = currentEvaluation - suggestedValuesEvaluation;
    return difference < 0 || Math.random() < Math.exp(-difference / currentTemperature);
  }
}
