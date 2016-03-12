package cz.cuni.mff.d3s.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;

/**
 * @author Michal Kit <kit@d3s.mff.cuni.cz>
 *
 */
public class VehicleUniformDistribution {

	private final List<String> linkIds;
	private final Random random;
	
	private final int sqrtVehicleCount;
	private final double distanceInterval;
	
	private final MATSimRouter router;
	
	public VehicleUniformDistribution(MATSimRouter router, int vehicleCount, double mapDimension) {
		this.random = new Random(vehicleCount);
		this.linkIds = new LinkedList<String>();
		this.router = router;
		
		//Distribute vehicles uniformly
		Coord coord;
		Link link;
		int c,r;
		sqrtVehicleCount = (int) Math.round(Math.floor(Math.sqrt(vehicleCount)));
		distanceInterval = mapDimension / (sqrtVehicleCount-1);
		for (r = 0; r < sqrtVehicleCount; r++) {
			for (c = 0; c < sqrtVehicleCount; c++) {
				coord = new CoordImpl(c*distanceInterval, r*distanceInterval);
				link = router.findNearestLink(coord);
				linkIds.add(link.getId().toString());
			}
		}
		//Distribute randomly remaining vehicles
		long remainingVehicles = vehicleCount - (sqrtVehicleCount*sqrtVehicleCount);
		for (int v = 0; v < remainingVehicles; v++) {
			linkIds.add(getRandomLinkId());
		}
	}
	
	public String getLinkId() {
		if (linkIds.isEmpty()) {
			return getRandomLinkId();
		} else {
			return linkIds.remove(random.nextInt(linkIds.size()));
		}
	}
	
	private String getRandomLinkId() {
		int r = random.nextInt(sqrtVehicleCount);
		int c = random.nextInt(sqrtVehicleCount);
		Coord coord = new CoordImpl(c*distanceInterval, r*distanceInterval);
		Link link = router.findNearestLink(coord);
		return link.getId().toString();
	}
	
}
