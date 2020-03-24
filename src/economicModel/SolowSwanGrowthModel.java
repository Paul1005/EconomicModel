package economicModel;

public class SolowSwanGrowthModel {
    public static double capital; // might want inflation to affect this
    public static double output;
    public static double a;
    public static double productivityPerWorker;
    public static int Labour;
    public static double eL;
    public static double depreciationPerWorker;
    public static double capitalPerPerson;
    public static double netGainPerPerson;
    public static double netGain;
    public static double steadyStateCapitalPerPerson;
    public static double steadyStateCapital;
    public static double steadyStateOutputPerPerson;
    public static double steadyStateOutput;
    public static double outputPerPerson;

    public static void runCycle(double savingsGrowth, double populationGrowth, double technology, double depreciation) {
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
