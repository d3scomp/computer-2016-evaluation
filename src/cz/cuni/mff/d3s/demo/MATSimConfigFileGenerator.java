package cz.cuni.mff.d3s.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class MATSimConfigFileGenerator {

	private static final String TIME_FORMAT = "HH:mm:ss";
	private static final long TIME_OFFSET = 82800000;
	private static MATSimConfigFileGenerator instance;
	
	public synchronized static MATSimConfigFileGenerator getInstance() {
		if (instance == null) {
			instance = new MATSimConfigFileGenerator();
		}
		return instance;
	}
	
	public void generateConfig(String template, long simulationDuration, String output, String matsimOutputDirectory) throws IOException {
		Scanner scanner = new Scanner(new File(template));
		String templateString = scanner.useDelimiter("\\Z").next();
		templateString = templateString.replace("$0$", matsimOutputDirectory);
		templateString = templateString.replace("$1$", getEndTime(simulationDuration));
		scanner.close();
		PrintWriter out = new PrintWriter(Files.newOutputStream(
				Paths.get(output), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
		out.write(templateString);
		out.close();
	}
	
	private String getEndTime(long duration) {
		DateFormat df = new SimpleDateFormat(TIME_FORMAT);
		return df.format(new Date(duration + TIME_OFFSET));
	}
}
