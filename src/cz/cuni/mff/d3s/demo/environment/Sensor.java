package cz.cuni.mff.d3s.demo.environment;

public interface Sensor<T> {
	
	SensorType getSensorType();
	T read();
}
