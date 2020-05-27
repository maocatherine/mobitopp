package edu.kit.ifv.mobitopp.data;

import java.util.List;

import edu.kit.ifv.mobitopp.data.demand.Demography;
import edu.kit.ifv.mobitopp.populationsynthesis.RegionalLevel;
import edu.kit.ifv.mobitopp.populationsynthesis.ipu.DefaultRegionalContext;
import edu.kit.ifv.mobitopp.populationsynthesis.ipu.RegionalContext;

public interface DemandRegion {

	String getExternalId();

	RegionalLevel regionalLevel();

	default RegionalContext getRegionalContext() {
		return new DefaultRegionalContext(regionalLevel(), getExternalId());
	}

	List<DemandRegion> parts();

	Demography nominalDemography();

	Demography actualDemography();

}