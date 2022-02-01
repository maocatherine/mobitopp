package edu.kit.ifv.mobitopp.simulation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class VisumToMobitopp {

	private String carTransportSystemCode = "CAR";
	private String ptTransportSystemCode = "BUS";
	private String individualWalkTransportSystemCode = "PED";
	private String ptWalkTransportSystemCode = "PUTW";

}
