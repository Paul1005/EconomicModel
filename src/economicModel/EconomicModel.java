package economicModel;

public class EconomicModel {
    private float taxRate;
    private float spending;
    private String weather;
    private float interestRate;
    private float timePeriod;
    private int population;

    public float inflation;
    public float gdp;
    public float gdpGrowth;
    public float giniCoefficient;
    public float budget;
    public int populationGrowth;
    public float unemployment;

    public EconomicModel(float taxRate, float spending, String weather, float interestRate, float timePeriod) {
        this.taxRate = taxRate;
        this.spending = spending;
        this.weather = weather;
        this.interestRate = interestRate;
        this.timePeriod = timePeriod;
    }


}
