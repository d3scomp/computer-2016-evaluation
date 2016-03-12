package cz.cuni.mff.d3s.demo;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.DeecoProperties;
import cz.cuni.mff.d3s.demo.parking.network.generator.Generator;

public class AgregatedLaunch {
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new Exception("The number of arguments needs to be more than 2");
		}
		StringBuilder sb = new StringBuilder();
		
		System.setProperty(DeecoProperties.PUBLISHING_PERIOD, "2000");
		System.setProperty(DeecoProperties.PACKET_SIZE, "50000");
		System.setProperty(DeecoProperties.MAXIMUM_REBROADCAST_DELAY, "100");
		
		
		System.out.println("Generating network");
		Generator generator = new Generator(Settings.CELL_COUNT, Settings.LINKS_PER_EDGE, Settings.LINK_LENGTH, Settings.NETWORK_OUTPUT, Settings.BIDIRECTIONAL_STREETS);
		List<String> toDisable = generator.generate();
		
		String [] densitiesStrings = Arrays.copyOfRange(args, 1, args.length);
		String modifier;
		String fileName = args[0] + "-";
		
		for (String density: densitiesStrings) {
			fileName += density + "-";
			Settings.VEHICLE_COUNT = Integer.parseInt(density);
			
			if (args[0].equals("omnet")) {
				//PARTIAL
				modifier = args[0] + "-" + density + "-partial";
				Settings.MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + modifier + ".xml";
				MATSimConfigFileGenerator.getInstance().generateConfig(Settings.MATSIM_CONFIG_TEMPLATE, Settings.PARTIAL_SIMULATION_DURATION, Settings.MATSIM_CONFIG, Settings.MATSIM_OUTPUT + "/" + modifier);
				DistributedSimulationWithOMNetLauncher.run(toDisable);
				sb.append(modifier + ": " + MaxParkingTimeCollector.getInstance().getMaxTime() + ", " + MaxParkingTimeCollector.getInstance().allParked(Settings.VEHICLE_COUNT) + "\n");
				MaxParkingTimeCollector.getInstance().clear();
			} else {
				//FULL
				modifier = args[0] + "-" + density + "-full";
				Settings.MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + modifier + ".xml";
				MATSimConfigFileGenerator.getInstance().generateConfig(Settings.MATSIM_CONFIG_TEMPLATE, Settings.FULL_SIMULATION_DURATION, Settings.MATSIM_CONFIG, Settings.MATSIM_OUTPUT + "/" + modifier);
				CentralizedSimulationLauncher.run(toDisable);
				sb.append(modifier + ": " + MaxParkingTimeCollector.getInstance().getMaxTime() + ", " + MaxParkingTimeCollector.getInstance().allParked(Settings.VEHICLE_COUNT) + "\n");
				MaxParkingTimeCollector.getInstance().clear();
				
				//NONE
//				modifier = args[0] + "-" + density + "-none";
//				Settings.MATSIM_CONFIG = Settings.MATSIM_INPUT + "/" + modifier + ".xml";
//				MATSimConfigFileGenerator.getInstance().generateConfig(Settings.MATSIM_CONFIG_TEMPLATE, Settings.NONE_SIMULATION_DURATION, Settings.MATSIM_CONFIG, Settings.MATSIM_OUTPUT + "/" + modifier);
//				DistributedNoCommunicationSimulationLauncher.run(toDisable);
//				sb.append(modifier + ": " + MaxParkingTimeCollector.getInstance().getMaxTime() + ", " + MaxParkingTimeCollector.getInstance().allParked(Settings.VEHICLE_COUNT) + "\n");
//				MaxParkingTimeCollector.getInstance().clear();
			}
		}
		PrintWriter out = new PrintWriter(Settings.ANALYSES_DIRECTORY + "/" + fileName+"analyses.txt");
		out.write(sb.toString());
		out.close();
	}

}
