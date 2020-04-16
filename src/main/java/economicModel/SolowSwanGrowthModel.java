package economicModel;

public class SolowSwanGrowthModel {
    private double capital; // might want inflation to affect this
    private double output;
    private int labour;
    private int cyclesRun;
    private double outputPerPerson;

    public void runCycle(double savingsGrowth, double populationGrowth, double technology, double depreciation) {
        double a = 1.0 / 3.0;
        double productivityPerWorker = 1.0;
        double eL = productivityPerWorker * labour;

        double capitalPerPerson = capital / labour;
        double depreciationPerWorker = (depreciation + populationGrowth) * capitalPerPerson;
        double netGainPerPerson = savingsGrowth * technology * Math.pow(capitalPerPerson, a) - depreciationPerWorker;
        double netGain = netGainPerPerson * labour;

        double steadyStateCapitalPerPerson = Math.pow((savingsGrowth * technology) / (depreciation + populationGrowth), 1 / (1 - a));
        double steadyStateCapital = steadyStateCapitalPerPerson * labour;
        double steadyStateOutputPerPerson = Math.pow(technology, 1 / (1 - a)) * Math.pow(savingsGrowth / (depreciation + populationGrowth), a / (1 - a));
        double steadyStateOutput = steadyStateOutputPerPerson * labour;

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

    public int getLabour() {
        return labour;
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

    public double getOutputPerPerson(){
        return outputPerPerson;
    }
}
