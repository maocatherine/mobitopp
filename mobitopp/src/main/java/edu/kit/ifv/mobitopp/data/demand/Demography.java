package edu.kit.ifv.mobitopp.data.demand;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import edu.kit.ifv.mobitopp.populationsynthesis.ipu.AttributeType;
import edu.kit.ifv.mobitopp.populationsynthesis.ipu.StandardAttribute;
import edu.kit.ifv.mobitopp.simulation.Employment;
import edu.kit.ifv.mobitopp.simulation.Gender;

public class Demography implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EmploymentDistribution employment;
    private final Map<AttributeType, RangeDistributionIfc> rangeDistributions;

    public Demography(
            EmploymentDistribution employment,
            Map<AttributeType, RangeDistributionIfc> rangeDistributions) {
        super();
        this.employment = employment;
        this.rangeDistributions = new LinkedHashMap<>(rangeDistributions);
    }

    public EmploymentDistribution employment() {
        return employment;
    }

    public RangeDistributionIfc household() {
        return getDistribution(StandardAttribute.householdSize);
    }

    public RangeDistributionIfc femaleAge() {
        return getDistribution(StandardAttribute.femaleAge);
    }

    public RangeDistributionIfc maleAge() {
        return getDistribution(StandardAttribute.maleAge);
    }

    /**
     * May return <code>null</code>.
     *
     * @param type
     * @return
     */
    public RangeDistributionIfc getDistribution(AttributeType type) {
        if (!rangeDistributions.containsKey(type)) {
            rangeDistributions.put(type, new RangeDistribution());
        }
        return rangeDistributions.get(type);
    }

    public RangeDistributionIfc income() {
        return getDistribution(StandardAttribute.income);
    }

    public boolean hasData() {
        return rangeDistributions.values().stream().anyMatch(RangeDistributionIfc::hasItems);
    }

    public void increment(AttributeType type, int value) {
        RangeDistributionIfc distribution = getDistribution(type);
        if (!distribution.hasItem(value)) {
            distribution.addItem(new RangeDistributionItem(value, 0));
        }
        distribution.increment(value);
    }

    public void incrementHousehold(int type) {
        household().increment(type);
    }

    public void incrementEmployment(Employment employment) {
        this.employment.getItem(employment).increment();
    }

    public void incrementAge(Gender gender, int age) {
        if (Gender.MALE == gender) {
            increment(StandardAttribute.maleAge, age);
        } else {
            increment(StandardAttribute.femaleAge, age);
        }
    }

    public Demography createEmpty() {
        EmploymentDistribution emptyEmployment = employment.createEmpty();
        Map<AttributeType, RangeDistributionIfc> emptyDistributions = emptyDistributions();
        return new Demography(emptyEmployment, emptyDistributions);
    }

    private Map<AttributeType, RangeDistributionIfc> emptyDistributions() {
        LinkedHashMap<AttributeType, RangeDistributionIfc> emptyDistributions = new LinkedHashMap<>();
        for (Entry<AttributeType, RangeDistributionIfc> entry : rangeDistributions.entrySet()) {
            emptyDistributions.put(entry.getKey(), entry.getValue().createEmpty());
        }
        return emptyDistributions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(employment, rangeDistributions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Demography other = (Demography) obj;
        return Objects.equals(employment, other.employment)
                && Objects.equals(rangeDistributions, other.rangeDistributions);
    }

    @Override
    public String toString() {
        return "Demography [employment=" + employment + ", rangeDistributions=" + rangeDistributions
                + "]";
    }

}
