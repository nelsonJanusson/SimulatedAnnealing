package org.example.models.interfaces;

public interface CoolingSchedule {
  public double temperature(
      double currentTemperature,
      double startingTemperature,
      double coolingScheduleConstant,
      int currentGeneration);
}
