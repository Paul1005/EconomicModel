package economicModel;

public class SolowSwanGrowthModel {
    public static float capital;
    public static double output;
    public static float a;
    public static float productivityPerWorker;
    public static float Labour;
    public static float eL;
    public static double depreciationPerWorker;
    public static float capitalPerPerson;
    public static double netGainPerPerson;
    public static double netGain;
    public static double steadyStateCapitalPerPerson;
    public static double steadyStateCapital;
    public static double steadyStateOutputPerPerson;
    public static double steadyStateOutput;
    public static double outputPerPerson;

    public static void runCycle(float savingsGrowth, float populationGrowth, float technology, float depreciation) {
        a = 1f / 3f;
        productivityPerWorker = 1;
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
