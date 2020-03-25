package economicModel;

public class EconomicModel {
    private float taxRate;
    private float spending;
    private String weather;
    public float interestRate;
    private float timePeriod;
    private int population = 100000;
    private int timeTillLoanDue = 10;
    private float technologyLevel;
    private float technologicalAdvancement = 0.1f;

    public float inflation;
    public float gdp;
    public float gdpGrowth = 0.3f;
    public float averageGDPGrowth;
    public float giniCoefficient;
    public float budget;
    public float populationGrowth = 0.02f;
    public float unemployment;
    public float difference;
    public float deficitOrSurplus;
    public float debtOrReserves;
    public float mpc = 0.3f;
    public float availableGDP;
    public float priceLevelDemand;
    public float priceLevelSupply;
    public float aggregateDemand;
    public float shortRunAggregateSupply;
    public float fullEmploymentOutput;

    public EconomicModel(float consumption, float investment, float govtSpending, float exports, float imports) {
        gdp = calculateGDP(consumption, investment, govtSpending, exports, imports); //initial settings
        debtOrReserves = 0;
    }

    public void runCycle(float taxationPercentage, float newGovtSpending, float money) {
        float taxRevenue = (taxationPercentage / 100) * gdp;
        availableGDP = gdp - taxRevenue;
        difference = taxRevenue - newGovtSpending;
        if (debtOrReserves > 0) {
            difference -= debtOrReserves;
        }

        averageGDPGrowth = gdpGrowth;

        if (difference < 0) {
            float debtToGDPRatio = -1 * (debtOrReserves + difference) / gdp;
            calculateInterestRate(averageGDPGrowth, debtToGDPRatio);
            deficitOrSurplus = difference + difference * interestRate;
        } else {
            deficitOrSurplus = difference;
        }

        debtOrReserves += deficitOrSurplus;

        calculateNewGDP(newGovtSpending);
    }

    private float calculateGDP(float consumption, float investment, float govtSpending, float exports, float imports){
        return consumption + investment + govtSpending + exports - imports;
    }

    private void calculateNewGDP(float newGovtSpending) {
        float thing = newGovtSpending * (float)Math.pow(mpc, 10);

        float newGDP = availableGDP + newGovtSpending;

        int newPopulation = getNewPopluation();

        newGDP = newPopulation;
    }

    private int getNewPopluation() {
        //Growth=rN(1−N/K)
        /*
        r = intrinsic growth rate, we can set this based on the current income per person, higher income = lower growth rate.
        N = current population
        K = carrying capacity, we can set this based on the total GDP, higher GDP = higher K
         */
        float r = calculateIntrinsicGrowthRate();
        int K = calculateCarryingCapacity();

        populationGrowth = r * population *(1 - (float)population/K);
        return (int)(population * populationGrowth + population);
    }

    private float calculateIntrinsicGrowthRate(){
        float r = 0.02f;
        return r;
    }

    private int  calculateCarryingCapacity() {
        int K = 1000000;
        return K;
    }

    public void calculateInterestRate(float previousGrowthRate, float debtToGDPRatio) {
        interestRate = debtToGDPRatio / (gdp + gdp * averageGDPGrowth * timeTillLoanDue);
    }

    public void adasModel() {
        float m = 1;
        float b = 10;
        priceLevelDemand = -m * gdp + b;
        priceLevelSupply = m * gdp - b;

        fullEmploymentOutput = 22000f;
    }
}
