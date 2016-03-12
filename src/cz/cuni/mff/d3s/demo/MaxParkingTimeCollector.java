package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MaxParkingTimeCollector {

	private static MaxParkingTimeCollector instance;
	
	public synchronized static MaxParkingTimeCollector getInstance() {
		if (instance == null) {
			instance = new MaxParkingTimeCollector();
		}
		return instance;
	}
	
	private final Map<String, Long> parkingTimes = new HashMap<String, Long>();
	
	public void register(String id, long maxTime) {
		parkingTimes.put(id, maxTime);
	}
	
	public boolean allParked(int expectedCount) {
		return parkingTimes.size() == expectedCount;
	}
	
	public long getMaxTime() {
		Collection<Long> maxTimes = parkingTimes.values();
		if (maxTimes == null || maxTimes.isEmpty()) {
			return -1;
		} else {
			Long maxTime = 0l;
			for (Long time: maxTimes) {
				if (time > maxTime) {
					maxTime = time;
				}
			}
			return maxTime;
		}
	}
	
	public void clear() {
		parkingTimes.clear();
	}
	
}
