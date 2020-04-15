package economicModel;

public class SolowSwanGrowthModel {
    public double capital; // might want inflation to affect this
    public double output;
    public double a;
    public double productivityPerWorker;
    public int labour;
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
    private int cyclesRun;

    public void runCycle(double savingsGrowth, double populationGrowth, double technology, double depreciation) {
        a = 1.0 / 3.0;
        productivityPerWorker = 1.0;
        eL = productivityPerWorker * labour;

        capitalPerPerson = capital / labour;
        depreciationPerWorker = (depreciation + populationGrowth) * capitalPerPerson;
        netGainPerPerson = savingsGrowth * technology * Math.pow(capitalPerPerson, a) - depreciationPerWorker;
        netGain = netGainPerPerson * labour;

        steadyStateCapitalPerPerson = Math.pow((savingsGrowth * technology) / (depreciation + populationGrowth), 1 / (1 - a));
        steadyStateCapital = steadyStateCapitalPerPerson * labour;
        steadyStateOutputPerPerson = Math.pow(technology, 1 / (1 - a)) * Math.pow(savingsGrowth / (depreciation + populationGrowth), a / (1 - a));
        steadyStateOutput = steadyStateOutputPerPerson * labour;

        output = technology * Math.pow(capital, a) * Math.pow(eL, (1 - a));

        outputPerPerson = technology * Math.pow(capitalPerPerson, a) * Math.pow(productivityPerWorker, 1 - a);

        capital += netGain;

        labour += labour * populationGrowth;

        cyclesRun++;
    }

    public void setCapital(int i) {
        capital = i;
    }

    public void setLabour(int i) {
        labour = i;
    }

    public double getOutput() {
        return output;
    }

    public void setCyclesRun(int i){
        cyclesRun = i;
    }

    public int getCyclesRun(){
        return cyclesRun;
    }
}
