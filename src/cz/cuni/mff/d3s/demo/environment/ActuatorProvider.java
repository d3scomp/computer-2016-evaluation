package cz.cuni.mff.d3s.demo.environment;

import java.util.Map;


public interface ActuatorProvider {
	public <T> Actuator<T> createActuator(ActuatorType type);
	public Map<ActuatorType, Actuator<?>> getActuators();
}
