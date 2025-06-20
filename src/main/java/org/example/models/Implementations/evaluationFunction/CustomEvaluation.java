package org.example.models.Implementations.evaluationFunction;

import org.example.models.Implementations.model.BemData;
import org.example.models.interfaces.EvaluationFunction;

public class CustomEvaluation implements EvaluationFunction {
  private double interpolate(double[] x, double[] y, double xi) {
    for (int i = 0; i < x.length - 1; i++) {
      if (xi >= x[i] && xi <= x[i + 1]) {
        double t = (xi - x[i]) / (x[i + 1] - x[i]);
        return y[i] + t * (y[i + 1] - y[i]);
      }
    }
    if (xi < x[0]) {    // If xi is below x[0], return y[0]; if above x[last], return y[last].
      return y[0];
    } else {
      return y[y.length - 1];
    }
  }
  @Override
  public double evaluate(BemData bemData) {

    double radius = bemData.radius();
    int RPM = bemData.RPM();
    double[] chord = bemData.chord();
    int B = bemData.B();
    final double thrustTolerance = 0.02; // Percentage away from final thrust value allowed
    final int segments = 20;            // Number of radial segments for BEM discretization
    final double betaLimit = Math.toRadians(70);    //A limit on how high the pitch angle (beta) gets ---->(it was found that this one makes sense. idk about the rest)

    final int MaxRefine = 200;           // Refinement times for pitch angle (beta)
    final int MaxIteration = 10000;      // Iterations for correct inflow factors (a, a_omega) and inflow angle (phi)
    final double factorsTolerance = 1e-6;

    // *******************************
    // ***** GIVEN CONSTANTS *********
    // *******************************
    final double rho = 0.4135;          // Air density rho at 10,000 m altitude (kg/m^3).
    final double u0 = 30.0;             // Freestream (flight) velocity u0 in m/s, as specified (30 m/s).
    final double aSound = 299.5;        // Speed of sound at 10,000 m altitude (approximate, in m/s).Used to check Mach number at the tip.

    // Thrust target and allowable tolerance.
    final double thrustTarget = 100.0;
    final double thrustLowerBound = thrustTarget - thrustTarget * thrustTolerance;
    final double thrustUpperBound = thrustTarget + thrustTarget * thrustTolerance;

    //double radius = 0.5;     //should be less than 1.5
    double r_hub = 0.1 * radius;
    double dr = (radius - r_hub) / segments;
    double[] r = new double[segments];
    for (int i = 0; i < segments; i++) {
      r[i] = r_hub + dr * (i + 0.5);
    }

    //int RPM = 3950;
    double omega = RPM * Math.PI / 30.0; // rad/s
    double tipSpeed = omega * radius;
    double machTip = tipSpeed / aSound;      // Compute Mach number at the tip. Should be less than 0.8

    //int B = 2;                // number of blades, should be more than 1.

    //double[] chord = new double[segments]; //chord
    //for (int i = 0; i < segments; i++) {
    //    chord[i] = 0.5 * (0.15 - (0.10 * i / segments)); //chord distribution
    //}

    // *******************************
    // ***** AIRFOIL POLAR DATA ******
    // *******************************
    double[] AoA_table = {-2, 0, 2, 4, 6, 8, 10, 12, 14, 16, 18};                                                       // Angles of attack (degrees) for the NACA 0012 polar.
    double[] CL_table = {-0.2187, 0.0, 0.2188, 0.4366, 0.6528, 0.8596, 1.0715, 1.2671, 1.4461, 1.5998, 1.6895};        // Corresponding lift coefficients (CL) for NACA 0012 at those AoA values.
    double[] CD_table = {0.0096, 0.0102, 0.0114, 0.0132, 0.0158, 0.0193, 0.0241, 0.0307, 0.0390, 0.0553, 0.0960};     // Corresponding drag coefficients (CD) for NACA 0012 at those AoA values.


    // Variables to store the design parameters.
    double[] aValues = new double[segments]; // axial induction
    double[] a_omegaValues = new double[segments]; // tangential induction
    double[] PhiValues = new double[segments]; // advance angle
    double[] betaValues = new double[segments]; // blade-pitch distribution

    // **************************
    // **** TWIST-REFINEMENT ****
    // **************************
    double alphaOptDeg = AoA_table[0];
    double maxLD = CL_table[0] / CD_table[0];
    for (int i = 1; i < AoA_table.length; i++) {
      double LD = CL_table[i] / CD_table[i];
      if (LD > maxLD) {
        maxLD = LD;
        alphaOptDeg = AoA_table[i];
      }
    }
    double alphaOpt = Math.toRadians(alphaOptDeg);

    for (int i = 0; i < segments; i++) {                // 1) Initial guess: beta_i = Phi0_i + α_opt,  where Phi0_i = arctan(u0/(ω*r[i]))
      double Phi0 = Math.atan(u0 / (omega * r[i]));
      betaValues[i] = Phi0 + alphaOpt;
    }

    // 4) Twist-refinement loop: update beta until max|Δbeta| < tolerance or maxRefines reached.
    final double tolerancebeta = Math.toRadians(0.1);  // 0.1° tolerance
    for (int refine = 0; refine < MaxRefine; refine++) {
      for (int i = 0; i < segments; i++) {    // 2a) Run a BEM pass with the current beta(r):
        double a = 0.1;
        double a_omega = 0.01;

        for (int iteration = 0; iteration < MaxIteration; iteration++) {
          double Phi = Math.atan(((1 + a) * u0) / ((1 - a_omega) * omega * r[i]));
          double alphaLocal = betaValues[i] - Phi;
          double alphaDeg = Math.toDegrees(alphaLocal);

          double CL = interpolate(AoA_table, CL_table, alphaDeg);
          double CD = interpolate(AoA_table, CD_table, alphaDeg);

          double sinPhi = Math.sin(Phi);
          double cosPhi = Math.cos(Phi);

          double X = (B * chord[i] * (CL * cosPhi - CD * sinPhi)) / (8.0 * Math.PI * r[i] * sinPhi * sinPhi);// equation for a
          double Y = (B * chord[i] * (CL * sinPhi + CD * cosPhi)) / (8.0 * Math.PI * r[i] * sinPhi * cosPhi);// equation for a_omega

          double a_initial = X / (1 - X);
          double aNew = 0.5 * (a + a_initial);
          double a_omega_Initial = Y / (1 + Y);
          double a_omegaNew = 0.5 * (a_omega + a_omega_Initial);


          if (Math.abs(aNew - a) < factorsTolerance && Math.abs(a_omegaNew - a_omega) < factorsTolerance) {
            a = aNew;
            a_omega = a_omegaNew;
            break;
          }
          a = aNew;
          a_omega = a_omegaNew;
        }

        aValues[i] = a;
        a_omegaValues[i] = a_omega;
        PhiValues[i] = Math.atan2((1 + a) * u0, (1 - a_omega) * omega * r[i]);

      }

      double maxdifference = 0.0;                             // 2b) Update beta_new(i) = Phi(i) + α_opt and check convergence.
      double[] newbeta = new double[segments];
      for (int i = 0; i < segments; i++) {
        newbeta[i] = betaValues[i] + 0.5 * (PhiValues[i] + alphaOpt - betaValues[i]);
        double difference = Math.abs(newbeta[i] - betaValues[i]);
        if (difference > maxdifference) {
          maxdifference = difference;
        }

      }

      System.arraycopy(newbeta, 0, betaValues, 0, segments);  // Copy newbeta into beta and break if converged.
      if (maxdifference < tolerancebeta) {
        break;
      }
    }
    boolean betaCheck = true;
    for (int i = 0; i < segments; i++) {
      if (betaValues[i] >= betaLimit) {
        betaCheck = false;
        break;
      }
    }

    // ***********************
    // **** FINAL BEM RUN ****
    // ***********************
    // (Recompute aVals[], apVals[], phiVals[] one last time with converged beta(r))
    for (int i = 0; i < segments; i++) {
      double a = 0.1;
      double a_omega = 0.01;
      for (int iteration = 0; iteration < MaxIteration; iteration++) {
        double Phi = Math.atan(((1 + a) * u0) / ((1 - a_omega) * omega * r[i]));
        double alphaLocal = betaValues[i] - Phi;
        double alphaDeg = Math.toDegrees(alphaLocal);

        double CL = interpolate(AoA_table, CL_table, alphaDeg);
        double CD = interpolate(AoA_table, CD_table, alphaDeg);

        double sinPhi = Math.sin(Phi);
        double cosPhi = Math.cos(Phi);

        double X = (B * chord[i] * (CL * cosPhi - CD * sinPhi)) / (8.0 * Math.PI * r[i] * (sinPhi * sinPhi));// equation for a
        double Y = (B * chord[i] * (CL * sinPhi + CD * cosPhi)) / (8.0 * Math.PI * r[i] * sinPhi * cosPhi);// equation for a_omega

        double a_initial = X / (1 - X);
        double aNew = 0.5 * (a + a_initial);
        double a_omega_Initial = Y / (1 + Y);
        double a_omegaNew = 0.5 * (a_omega + a_omega_Initial);

        if (Math.abs(aNew - a) < factorsTolerance && Math.abs(a_omegaNew - a_omega) < factorsTolerance) {
          a = aNew;
          a_omega = a_omegaNew;
          break;
        }
        a = aNew;
        a_omega = a_omegaNew;
      }
      aValues[i] = a;
      a_omegaValues[i] = a_omega;
      PhiValues[i] = Math.atan(((1 + a) * u0) / ((1 - a_omega) * omega * r[i]));
    }
    //find the results

    double Tsum = 0.0;
    double Qsum = 0.0;
    for (int i = 0; i < segments; i++) {
      double alphaLocal = betaValues[i] - PhiValues[i];            // local AoA
      double alphaDeg = Math.toDegrees(alphaLocal);      // convert to degrees
      double CL = interpolate(AoA_table, CL_table, alphaDeg);
      double CD = interpolate(AoA_table, CD_table, alphaDeg);

      double sinPhi = Math.sin(PhiValues[i]);
      double cosPhi = Math.cos(PhiValues[i]);

      double common = 0.5 * rho * (u0 * u0) * B * chord[i] * ((1 + aValues[i]) * (1 + aValues[i])) / (sinPhi * sinPhi);

      double dT = common * (CL * cosPhi - CD * sinPhi) * dr;
      double dQ = common * (CL * sinPhi + CD * cosPhi) * r[i] * dr;

      // Accumulate sums
      Tsum += dT;
      Qsum += dQ;
    }

    // Compute required shaft power = Qsum * omega (in Watts).
    double power = Qsum * omega;

    // Propulsive efficiency η = (T * u0) / P.
    double efficiency = (Tsum * u0) / power;

// check if solution is valid
    boolean NotValid = false;   // Flag indicating whether we have found a valid design.
    for (int i = 0; i < segments; i++) {

      if (Tsum < thrustLowerBound || Tsum > thrustUpperBound || radius > 1.5 || machTip > 0.8 || chord[i] < 0.01 || B <= 1
              || efficiency<0 || aValues[i] <= 0 || a_omegaValues[i] <= 0 || aValues[i] >= 0.4 || a_omegaValues[i] >= 0.3) {
        // design NOT valid.
        NotValid = true;
        break;
      }
    }
    if (NotValid) {
      System.out.println("design is not valid");
      return -1;
    } else return efficiency;


  }

}
