package cz.cuni.mff.d3s.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.matsim.api.core.v01.Coord;
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
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimExtractor;
import cz.cuni.mff.d3s.deeco.simulation.matsim.DefaultMATSimUpdater;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgent;
import cz.cuni.mff.d3s.deeco.simulation.matsim.JDEECoAgentSource;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimOMNetSimulation;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.simulation.omnet.OMNetSimulationHost;
import cz.cuni.mff.d3s.demo.components.Vehicle;
import cz.cuni.mff.d3s.demo.ensembles.CapacityExchangeEnsemble;
import cz.cuni.mff.d3s.demo.ensembles.VehicleDestinationsExachangeEnsemble;
import cz.cuni.mff.d3s.demo.environment.MATSimOMNetDataProviderReceiver;

public class DistributedSimulationWithOMNetLauncher {

	private static JDEECoAgentSource jdeecoAgentSource;
	private static MATSimOMNetSimulation simulation;
	private static MATSimRouter router;
	private static MATSimOMNetDataProviderReceiver matSimProviderReceiver;
	private static SimulationRuntimeBuilder builder;

	public static void run(List<String> linksToDisable) throws AnnotationProcessorException, IOException, KeyStoreException {
		System.out.println("Preparing simulation");
		jdeecoAgentSource = new JDEECoAgentSource();
		matSimProviderReceiver = new MATSimOMNetDataProviderReceiver(linksToDisable);
		simulation = new MATSimOMNetSimulation(matSimProviderReceiver,
				matSimProviderReceiver, new DefaultMATSimUpdater(),
				new DefaultMATSimExtractor(),
				Arrays.asList(jdeecoAgentSource), Settings.MATSIM_CONFIG);
		router = new MATSimRouter(simulation.getControler(),
				simulation.getTravelTime(), Settings.ROUTE_CALCULATION_OFFSET);
		matSimProviderReceiver.initialize(simulation, router);
		System.out.println("Creating components");
		builder = new SimulationRuntimeBuilder();
		
		System.out.println("Deploying components");
		//Prepare for uniform vehicle distribution across the map
		double mapDimension = Settings.CELL_COUNT*Settings.LINKS_PER_EDGE*Settings.LINK_LENGTH;
		VehicleUniformDistribution distributor = new VehicleUniformDistribution(router, Settings.VEHICLE_COUNT, mapDimension);
		
		//Deploy each vehicle component
		String leftDestination = router.findNearestLink(new CoordImpl(mapDimension / 4.0, mapDimension / 2.0)).getId().toString();
		String rightDestination = router.findNearestLink(new CoordImpl(mapDimension * 0.75, mapDimension / 2.0)).getId().toString();
		
		StringBuilder omnetConfig = new StringBuilder();
		
		int i;
		ArrayList<Integer> destinations = new ArrayList<Integer>();
		Random r = new Random(Settings.VEHICLE_COUNT);
		
		for (i = 0; i < Settings.VEHICLE_COUNT/2; i++) {
			destinations.add(0);
		}
		for (; i < Settings.VEHICLE_COUNT; i++) {
			destinations.add(1);
		}
		for (i = 0; i < Settings.VEHICLE_COUNT; i++) {
			createAndDeployVehicleComponent(i, distributor.getLinkId(), (destinations.remove(r.nextInt(destinations.size())) == 0) ? leftDestination : rightDestination, omnetConfig);
		}
		
		//Preparing omnetpp config
		
		String confName = "omnetpp" + Settings.VEHICLE_COUNT;
		String confFile = confName + ".ini";
		Scanner scanner = new Scanner(new File(Settings.OMNET_CONFIG_TEMPLATE));
		String template = scanner.useDelimiter("\\Z").next();
		template = template.replace("<<<configName>>>", confName);
		scanner.close();
		PrintWriter out = new PrintWriter(Files.newOutputStream(
				Paths.get(confFile), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
		out.println(template);
		out.println();
		out.println(String.format("**.playgroundSizeX = %dm", new Double(mapDimension).longValue() + 100));
		out.println(String.format("**.playgroundSizeY = %dm", new Double(mapDimension).longValue() + 100));
		out.println();
		out.println(String.format("**.numNodes = %d", Settings.VEHICLE_COUNT));
		out.println();
		out.println("**.node[*].appl.packet802154ByteLength = 128B");
		out.println();
		out.println();
		out.println(String.format("sim-time-limit = %ds", simulation.getOMNetSimulationDuration()));
		out.println();
		out.println(omnetConfig.toString());
		out.close();
		

		System.out.println("Run the simulation");
		//Run the simulation
		simulation.run("Cmdenv", confFile);
		System.out.println("Simulation Finished");
	}
	
	
	//----------- Utility Methods--------------------

	private static void createAndDeployVehicleComponent(int idx,
			String sourceLinkIdString, String destLinkIdString, StringBuilder omnetConfig)
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
		processor.process(component, CapacityExchangeEnsemble.class, VehicleDestinationsExachangeEnsemble.class);
		
		Coord currentPosition = router.getLink(sourceLinkId).getCoord();

		omnetConfig.append(String.format("**.node[%d].mobility.initialX = %dm %n", idx, new Double(currentPosition.getX()).longValue()));			
		omnetConfig.append(String.format("**.node[%d].mobility.initialY = %dm %n", idx, new Double(currentPosition.getY()).longValue()));
		omnetConfig.append(String.format("**.node[%d].mobility.initialZ = 0m %n", idx));
		omnetConfig.append(String.format("**.node[%d].appl.id = \"%s\" %n%n", idx, component.id));
		
		Collection<? extends TimerTaskListener> listeners = null;
		if (idx == 0) {
			listeners = Arrays.asList(simulation);
		}
		
		OMNetSimulationHost host = simulation.getHost(compIdString, "node["+idx+"]");
		RuntimeFramework runtime = builder.build(host, simulation, listeners, model, new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), new CloningKnowledgeManagerFactory(), SecurityKeyManagerImpl.getInstance(), RatingsManagerImpl.getInstance());
		runtime.start();	
	}

}
