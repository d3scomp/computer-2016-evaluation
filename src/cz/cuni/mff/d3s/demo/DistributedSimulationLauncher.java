package cz.cuni.mff.d3s.demo;

import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessor;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.integrity.RatingsManagerImpl;
import cz.cuni.mff.d3s.deeco.knowledge.CloningKnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.security.SecurityKeyManagerImpl;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.DirectSimulationHost;
import cz.cuni.mff.d3s.deeco.simulation.NetworkDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimExtractor;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimUpdater;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimSimulation;
import cz.cuni.mff.d3s.demo.components.Vehicle;
import cz.cuni.mff.d3s.demo.ensembles.CapacityExchangeEnsemble;
import cz.cuni.mff.d3s.demo.ensembles.VehicleDestinationsExachangeEnsemble;
import cz.cuni.mff.d3s.demo.environment.MATSimDataProviderReceiver;

public class DistributedSimulationLauncher {

	private static JDEECoAgentSource jdeecoAgentSource;
	private static MATSimSimulation simulation;
	private static MATSimRouter router;
	private static MATSimDataProviderReceiver matSimProviderReceiver;
	private static SimulationRuntimeBuilder builder;

	public static void run(List<String> linksToDisable) throws AnnotationProcessorException, KeyStoreException {
		System.out.println("Preparing simulation");
		jdeecoAgentSource = new JDEECoAgentSource();
		matSimProviderReceiver = new MATSimDataProviderReceiver(linksToDisable);
		NetworkDataHandler networkHandler = new DirectKnowledgeDataHandler();
		simulation = new MATSimSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(), networkHandler,
				Arrays.asList(jdeecoAgentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(simulation.getControler(),
				simulation.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		System.out.println("Creating components");
		builder = new SimulationRuntimeBuilder();

		System.out.println("Deploy components");
		// Prepare for uniform vehicle distribution across the map
		double mapDimension = Settings.CELL_COUNT * Settings.LINKS_PER_EDGE
				* Settings.LINK_LENGTH;
		VehicleUniformDistribution distributor = new VehicleUniformDistribution(
				router, Settings.VEHICLE_COUNT, mapDimension);

		// Deploy each vehicle component
		String leftDestination = router
				.findNearestLink(
						new CoordImpl(mapDimension / 4.0, mapDimension / 2.0))
				.getId().toString();
		String rightDestination = router
				.findNearestLink(
						new CoordImpl(mapDimension * 0.75, mapDimension / 2.0))
				.getId().toString();

		int i;
		for (i = 1; i <= Settings.VEHICLE_COUNT / 2; i++) {
			createAndDeployVehicleComponent(i, distributor.getLinkId(),
					leftDestination);
		}
		for (; i <= Settings.VEHICLE_COUNT; i++) {
			createAndDeployVehicleComponent(i, distributor.getLinkId(),
					rightDestination);
		}

		System.out.println("Run the simulation");
		// Run the simulation
		simulation.run();
		System.out.println("Simulation Finished");
	}

	// ----------- Utility Methods--------------------

	private static void createAndDeployVehicleComponent(int idx,
			String sourceLinkIdString, String destLinkIdString)
			throws AnnotationProcessorException, KeyStoreException {
		String compIdString = "V" + idx;
		Id compId = new IdImpl(compIdString);
		Id sourceLinkId = new IdImpl(sourceLinkIdString);
		Id destLinkId = new IdImpl(destLinkIdString);

		jdeecoAgentSource.addAgent(new JDEECoAgent(compId, sourceLinkId));

		Vehicle component = new Vehicle(compIdString, destLinkId, sourceLinkId,
				matSimProviderReceiver.getActuatorProvider(compId),
				matSimProviderReceiver.getSensorProvider(compId), router,
				simulation);

		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE
				.createRuntimeMetadata();
		AnnotationProcessor processor = new AnnotationProcessor(
				RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		processor.process(component, CapacityExchangeEnsemble.class,
				VehicleDestinationsExachangeEnsemble.class);

		DirectSimulationHost host = simulation.getHost(compIdString);
		RuntimeFramework runtime = builder.build(host, simulation, null, model,
				new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory(), SecurityKeyManagerImpl.getInstance(), RatingsManagerImpl.getInstance());
		runtime.start();
	}

}
