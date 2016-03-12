package cz.cuni.mff.d3s.demo;

import org.matsim.api.core.v01.Coord;

public class GeoUtils {
	public static double getEuclidDistance(Coord p1, Coord p2) {
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY(); 
		
		return Math.sqrt(dx*dx + dy*dy);
	}
}
