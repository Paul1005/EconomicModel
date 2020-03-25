package economicModel;

public class SolowSwanGrowthModel {
    public double capital; // might want inflation to affect this
    public double output;
    public double a;
    public double productivityPerWorker;
    public int Labour;
    public double eL;
    public double depreciationPerWorker;
    public double capitalPerPerson;
    public double netGainPerPerson;
    public double netGain;
    public double steadyStateCapitalPerPerson;
    public double steadyStateCapital;
    public double steadyStateOutputPerPerson;
    public double steadyStateOutput;
    public double outputPerPerson;

    public void runCycle(double savingsGrowth, double populationGrowth, double technology, double depreciation) {
        a = 1.0 / 3.0;
        productivityPerWorker = 1.0;
        eL = productivityPerWorker * Labour;

        capitalPerPerson = capital / Labour;
        depreciationPerWorker = (depreciation + populationGrowth) * capitalPerPerson;
        netGainPerPerson = savingsGrowth * technology * Math.pow(capitalPerPerson, a) - depreciationPerWorker;
        netGain = netGainPerPerson * Labour;

        steadyStateCapitalPerPerson = Math.pow((savingsGrowth * technology) / (depreciation + populationGrowth), 1 / (1 - a));
        steadyStateCapital = steadyStateCapitalPerPerson * Labour;
        steadyStateOutputPerPerson = Math.pow(technology, 1 / (1 - a)) * Math.pow(savingsGrowth / (depreciation + populationGrowth), a / (1 - a));
        steadyStateOutput = steadyStateOutputPerPerson * Labour;

        output = technology * Math.pow(capital, a) * Math.pow(eL, (1 - a));

        outputPerPerson = technology * Math.pow(capitalPerPerson, a) * Math.pow(productivityPerWorker, 1 - a);

        capital += netGain;

        Labour += Labour * populationGrowth;
    }

}
