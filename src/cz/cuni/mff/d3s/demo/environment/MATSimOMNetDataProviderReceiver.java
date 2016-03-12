package cz.cuni.mff.d3s.demo.environment;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.network.NetworkInterface;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimOMNetCoordinatesTranslator;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimOMNetSimulation;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;


public class MATSimOMNetDataProviderReceiver extends MATSimDataProviderReceiver {

	private MATSimOMNetCoordinatesTranslator translator;
	private MATSimOMNetSimulation simulation;
	private MATSimRouter router;
	
	public MATSimOMNetDataProviderReceiver(List<String> linksToDisable) {
		super(linksToDisable);
		// TODO Auto-generated constructor stub
	}
	
	public void initialize(MATSimOMNetSimulation simulation, MATSimRouter router) {
		this.simulation = simulation;
		this.translator = simulation.getPositionTranslator();
		this.router = router;
	}
	
	public void setMATSimData(Object data) {
		super.setMATSimData(data);
		Coord omnetCoord;
		NetworkInterface hostInterface;
		for (Id key : outputs.keySet()) {
			omnetCoord = translator.fromMATSimToOMNet(router.getLink(outputs.get(key).currentLinkId).getCoord());
			hostInterface = simulation.getNetworkInterfaceByHostId(key.toString());
			simulation.setPosition(hostInterface.getHostId(), omnetCoord.getX(), omnetCoord.getY(), 0);
		}
	}
}
