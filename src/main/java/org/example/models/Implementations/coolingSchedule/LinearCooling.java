package org.example.models.Implementations.coolingSchedule;

import org.example.models.interfaces.CoolingSchedule;

public class LinearCooling implements CoolingSchedule {
  @Override
  public double temperature(
      double currentTemperature,
      double startingTemperature,
      double coolingScheduleConstant,
      int currentGeneration) {
    return currentTemperature - coolingScheduleConstant;
  }
}
