package cz.cuni.mff.d3s.demo.environment;

import java.util.Map;

public interface SensorProvider {
	public <T> Sensor<T> createSensor(SensorType type);
	public Map<SensorType, Sensor<?>> getSensors();
}
