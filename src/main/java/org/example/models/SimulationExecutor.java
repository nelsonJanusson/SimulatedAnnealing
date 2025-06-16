package org.example.models;

import java.util.Arrays;

import org.example.models.Implementations.model.BemData;
import org.example.models.interfaces.AcceptanceFunction;
import org.example.models.interfaces.CoolingSchedule;
import org.example.models.interfaces.EvaluationFunction;
import org.example.models.interfaces.MoveFunction;

public class SimulationExecutor {
  public SimulationExecutor(
      AcceptanceFunction acceptanceFunction,
      CoolingSchedule coolingSchedule,
      EvaluationFunction evaluationFunction,
      MoveFunction moveFunction,
      double coolingScheduleConstant,
      double startingTemperature,
      int maxGeneration,
      BemData bemData) {
    this.acceptanceFunction = acceptanceFunction;
    this.coolingSchedule = coolingSchedule;
    this.evaluationFunction = evaluationFunction;
    this.moveFunction = moveFunction;
    this.coolingScheduleConstant = coolingScheduleConstant;
    this.startingTemperature = startingTemperature;
    this.maxGeneration = maxGeneration;
    this.bemData = bemData;
    this.currentTemperature = startingTemperature;
    this.currentEvaluation = evaluationFunction.evaluate(bemData);
    this.currentGeneration = 0;
  }

  private static final double MIN_TEMPERATURE = 0.00001;

  private final AcceptanceFunction acceptanceFunction;
  private final CoolingSchedule coolingSchedule;
  private final EvaluationFunction evaluationFunction;
  private final MoveFunction moveFunction;
  private final double coolingScheduleConstant;
  private final double startingTemperature;
  private final int maxGeneration;
  private BemData bemData;
  private double currentTemperature;
  private double currentEvaluation;
  private int currentGeneration;

  private void newGeneration() {
    BemData suggestedBemData = moveFunction.move(bemData);
    double suggestedValuesEvaluation = -1;
    while (suggestedValuesEvaluation==-1){
       suggestedValuesEvaluation = evaluationFunction.evaluate(suggestedBemData);
    }
    boolean accept =
        acceptanceFunction.accept(currentEvaluation, suggestedValuesEvaluation, currentTemperature);
    if (accept) {
      bemData = suggestedBemData;
      currentEvaluation = suggestedValuesEvaluation;
    }
    currentTemperature =
        coolingSchedule.temperature(
            currentTemperature, startingTemperature, coolingScheduleConstant, currentGeneration);
    currentGeneration++;
  }

  public void execute() {
    boolean maxGenerationReached = false;
    boolean minTemperatureReached = false;

    while (!maxGenerationReached && !minTemperatureReached) {
      newGeneration();
      maxGenerationReached = currentGeneration >= maxGeneration;
      minTemperatureReached = currentTemperature <= MIN_TEMPERATURE;
    }
    if (maxGenerationReached) System.out.println("max generation reached");
    if (minTemperatureReached) System.out.println("minimum temperature reached");

    System.out.println("Evaluation: " + currentEvaluation);

    System.out.println("B : "+bemData.B());
    System.out.println("chord : "+Arrays.toString(    bemData.chord()));
    System.out.println("RPM : "+bemData.RPM());
    System.out.println("radius : "+bemData.radius());

  }
}
