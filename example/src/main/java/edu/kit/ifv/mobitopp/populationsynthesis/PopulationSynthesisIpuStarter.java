package edu.kit.ifv.mobitopp.populationsynthesis;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import edu.kit.ifv.mobitopp.util.dataimport.Bbsr17Repository;

public class PopulationSynthesisIpuStarter {

	public static void main(String... args) throws Exception {
		if (1 > args.length) {
			System.out.println("Usage: ... <configuration file>");
			System.exit(-1);
		}

		File configurationFile = new File(args[0]);
		LocalDateTime start = LocalDateTime.now();
		startSynthesis(configurationFile);
		LocalDateTime end = LocalDateTime.now();
		Duration runtime = Duration.between(start, end);
		System.out.println("Population synthesis took " + runtime);
	}

	private static void startSynthesis(File configurationFile) throws Exception {
		Bbsr17Repository areaTypeRepository = new Bbsr17Repository();
		SynthesisContext context = new ContextBuilder(areaTypeRepository)
				.buildFrom(configurationFile);
		startSynthesis(context);
	}

	public static void startSynthesis(SynthesisContext context) {
		context.printStartupInformationOn(System.out);
		PopulationSynthesis synthesizer = new PopulationSynthesisIpuExample(context);
		synthesizer.createPopulation();
	}
}