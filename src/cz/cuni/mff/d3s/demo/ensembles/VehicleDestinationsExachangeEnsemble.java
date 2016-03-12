package cz.cuni.mff.d3s.demo.ensembles;

import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.CommunicationBoundary;
import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.KnowledgePathBuilder;
import cz.cuni.mff.d3s.demo.Settings;

@Ensemble
@PeriodicScheduling(period = 1000, order = 20)
public class VehicleDestinationsExachangeEnsemble {

	
	@Membership
	public static boolean membership(@In("coord.id") String cId,
			@In("member.id") String mId, 
			@In("member.destinationLink") Id mDestinationLink,
			@In("coord.destinationLink") Id cDestinationLink) {
		return !cId.equals(mId) && cDestinationLink.equals(mDestinationLink);
	}

	@KnowledgeExchange
	public static void exchange(
			@InOut("coord.otherVehicles") ParamHolder<List<String>> cOtherVehicles,
			@In("member.id") String mId,
			@In("member.parked") Boolean mParked) {
		if (mParked) {
			cOtherVehicles.value.remove(mId);
		} else if (!cOtherVehicles.value.contains(mId)){
			cOtherVehicles.value.add(mId);
		}
	}
	
	@CommunicationBoundary
	public static boolean boundary(KnowledgeData data, ReadOnlyKnowledgeManager sender) throws KnowledgeNotFoundException {
		KnowledgePath kpPosition = KnowledgePathBuilder.buildSimplePath("position");
		KnowledgePath kpDestination = KnowledgePathBuilder.buildSimplePath("destination");
		Coord ownerPosition = (Coord) data.getKnowledge().getValue(kpPosition);
		Coord senderPosition = (Coord) sender.get(Arrays.asList(kpPosition)).getValue(kpPosition);
		Coord ownerDestination = (Coord) data.getKnowledge().getValue(kpDestination);
		return isInDestinationRadius(senderPosition, ownerDestination) && isInDestinationRadius(ownerPosition, ownerDestination);
	}
	
	
	private static boolean isInDestinationRadius(Coord position, Coord destination) {
		return getEuclidDistance(position, destination) < Settings.ENSEMBLE_RADIUS;
	}
	
	private static double getEuclidDistance(Coord p1, Coord p2) {
		if (p1 == null || p2 == null) {
			return Double.POSITIVE_INFINITY;
		}

		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY(); 

		return Math.sqrt(dx*dx + dy*dy);
	}
}
