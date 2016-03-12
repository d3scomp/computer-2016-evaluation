package cz.cuni.mff.d3s.demo.environment;

public interface Actuator<T> {
	public void set(T value);
	public ActuatorType getActuatorType();
}
