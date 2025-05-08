package org.example.models;

import lombok.AllArgsConstructor;
import org.example.models.interfaces.AcceptanceFunction;
import org.example.models.interfaces.CoolingSchedule;
import org.example.models.interfaces.EvaluationFunction;
import org.example.models.interfaces.MoveFunction;

@AllArgsConstructor
public class SimulationExecutor {
    private final AcceptanceFunction acceptanceFunction;
    private final CoolingSchedule coolingSchedule;
    private final EvaluationFunction evaluationFunction;
    private final MoveFunction moveFunction;
    private final double temperatureConstant;
    private double[] values;
    private double currentEvaluation;
    private double temperature;
    private int generation = 0;

    private void newGeneration() {
        double[] suggestedValues = moveFunction.move(values);
        double newValuesEvaluation = evaluationFunction.evaluate(suggestedValues);
        boolean accept =acceptanceFunction.accept(currentEvaluation,newValuesEvaluation,temperature);
        if(accept) {
            values = suggestedValues;
            currentEvaluation = newValuesEvaluation;
        }
        temperature = coolingSchedule.temperature(temperature,temperatureConstant,generation);
        generation = generation+1;
    }
    public void execute(int generationMax) {
        currentEvaluation= evaluationFunction.evaluate(values);
        while(generation <= generationMax) {
            newGeneration();
        }
        System.out.println("Evaluation: " + currentEvaluation);
        System.out.println("Values: " );
        for(double value : values) {
            System.out.print(value + ", ");
        }
    }

}
