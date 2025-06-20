package org.example.models.Implementations.moveFunction;

import org.example.models.Implementations.model.BemData;
import org.example.models.interfaces.MoveFunction;

import java.util.Random;

public class CustomMove implements MoveFunction {
  @Override
  public BemData move(BemData bemData) {
    /*
    each "generation" a collection of new suggested values are generated based on the previous values.
    implement the logic for generating these suggested new values and returning them here.
    */
    Random rand= new Random();
    // BemData(double radius, int RPM, int B, double[] chord)
    double radius_move = Math.random()*0.1;
    int RPM_move = rand.nextInt(26 );
    int B_move = rand.nextInt(2 );

    double new_radius = bemData.radius() + radius_move;
    int new_RPM = bemData.RPM()+RPM_move;
    int new_B = bemData.B()+B_move;
    double[] new_Chord = generateNewChord(bemData.chord());

    return new BemData(new_radius, new_RPM, new_B, new_Chord);

  }

  private double[] generateNewChord(double[] chord){
    for(int i=0;i<chord.length;i++){
      chord[i]=chord[i]+Math.random()*0.1;
    }
    return chord.clone();
  }
}
