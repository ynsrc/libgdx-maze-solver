package ynsrc.mazesolver;

/**
 * This class represents the DC motors of maze solver robot for simulation.
 */
public class Motor {
    /** PWM output speed in range (0-255) to drive DC motors with a motor driver IC. */
    int speed = 255;

    /** Motor direction for simulation. */
    MotorDirection direction = MotorDirection.NONE;
}
