package org.example;

import org.example.models.Implementations.acceptanceFunction.MetropolisCriterion;
import org.example.models.Implementations.coolingSchedule.GeometricCooling;
import org.example.models.Implementations.evaluationFunction.CustomEvaluation;
import org.example.models.Implementations.moveFunction.CustomMove;
import org.example.models.SimulationExecutor;

public class Main {
  public static void main(String[] args) {
    /*
    To run the algorithm you need to initialize the SimulationExecutor and then call the execute method as shown below.
    before the algorithm can be run you need to implement the move function in the CustomMove object and the evaluate
    function in the CustomEvaluation object.

    If the results aren't good enough then you can try using another AcceptanceFunction implementation instead of
    MetropolisCriterion, using another CoolingSchedule implementation instead of GeometricCooling or Using a new
    implementation of the MoveFunction (or just change the CustomMove object that is already implemented). You can also try
    changing the coolingSchedule constant, startingTemperature constant and the maxGeneration constant.

    When changing the startingTemperature constant a good rule of thumb is that you want to accept 80-90 percent of worse
    solutions initially (this can be calculated by looking at the AcceptanceFunction implementation you have chosen as well as the
    range of your evaluate function in the CustomEvaluation object). The startingTemperature also has to be positive.

    When changing the coolingSchedule constant you should consider which CoolingSchedule implementation you have chosen.
    The coolingSchedule constant has a different meaning in each implementation so if you change it you should look at
    what role it plays in your implementation and then google what reasonable/useful values are for the given value in
    your specific implementation.

    If the results are still bad or other errors come upp then contact the maintainer Nelson Janusson.
    */

    /*
    these re the initial values chosen for the algorithm. you should replace them with reasonable starting values for your
    situation.
    */
    double[] values = {10, 2, 6, 5, 4, 8};

    SimulationExecutor simulationExecutor =
        new SimulationExecutor(
            new MetropolisCriterion(),
            new GeometricCooling(),
            new CustomEvaluation(),
            new CustomMove(),
            0.9,
            0.5,
            5000,
            values);

    simulationExecutor.execute();
  }
}
