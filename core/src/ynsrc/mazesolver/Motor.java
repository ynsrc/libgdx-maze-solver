package ynsrc.mazesolver;

/**
 * This class represents the DC motors of maze solver robot for simulation.
 */
public class Motor {
    /** PWM output speed in range (0-255) to drive DC motors with a motor driver IC. */
    private int speed = 255;

    /** Motor direction for simulation. */
    MotorDirection direction = MotorDirection.NONE;

    /** Set the speed of this DC motor in range (0.0 - 1.0)  */
    public void setSpeed(float speed) {
        this.speed = (int)(speed * 255);
    }

    /** Get the speed of this DC motor in range (0.0 - 1.0) */
    public float getSpeed() {
        return speed / 255f;
    }
}
