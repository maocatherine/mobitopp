package edu.kit.ifv.mobitopp.matsim;

import java.io.File;
import java.util.Stack;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.Modes4Accessibility;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigReader;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.FacilitiesConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import edu.kit.ifv.mobitopp.routing.ValidateLinks;
import edu.kit.ifv.mobitopp.simulation.Matsim;
import edu.kit.ifv.mobitopp.visum.VisumRoadNetwork;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.facilities.*;
import org.xml.sax.Attributes;

public class PrepareMatsim {

	static final String resultFolder = "matsim";
	private final MatsimContext context;

	public PrepareMatsim(MatsimContext context) {
		this.context = context;
	}
	
	public static Matsim from(MatsimContext context) {
		return from(context, new CarOnly());
	}

	public static Matsim from(MatsimContext context, ActivityFilter filter) {
		return new PrepareMatsim(context).create(filter);
	}

	private Matsim create(ActivityFilter filter) {
		Scenario scenario = createScenario();
		ScenarioUtils.loadScenario(scenario);
		loadNetwork(scenario);
		return new Matsim(context, scenario, filter);
	}

	private void loadNetwork(Scenario scenario) {
		VisumRoadNetwork visumNetwork = new ValidateLinks().of(context.network());
		MatsimNetworkCreator creator = new MatsimNetworkCreator(scenario, visumNetwork);
		creator.createFromVisumNetwork();
	}

	private Scenario createScenario() {
		return ScenarioUtils.createScenario(fromConfig());
	}

	private Config fromConfig() {
		float fractionOfPopulation = context.fractionOfPopulation();
		String matsimConfig = context.experimentalParameters().value("matsimConfig");
		String facilityConfig = context.experimentalParameters().value("facilities");

		Config config = ConfigUtils.loadConfig(matsimConfig);
		config.facilities().setInputFile(facilityConfig);

		config.qsim().setFlowCapFactor(fractionOfPopulation);
		config.qsim().setStorageCapFactor(fractionOfPopulation);
		config
				.controler()
				.setOverwriteFileSetting(
						OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		AccessibilityConfigGroup accConfig = ConfigUtils.addOrGetModule(config, AccessibilityConfigGroup.class ) ;
		accConfig.setComputingAccessibilityForMode(Modes4Accessibility.car, true);

		updateResultFolder(config);
		return config;
	}

	void updateResultFolder(Config config) {
		String baseFolder = context.configuration().getResultFolder();
		String matsimResults = new File(baseFolder, resultFolder).getAbsolutePath();
		config.controler().setOutputDirectory(matsimResults);
	}

}
