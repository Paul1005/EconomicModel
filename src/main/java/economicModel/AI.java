package economicModel;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import net.sourceforge.jFuzzyLogic.FIS;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AI {
    private ASADModel asadModel;
    private SolowSwanGrowthModel solowSwanGrowthModel;
    Instances instances;
    File file;
    double bondChange;
    double reserveMultiplier;
    double spendingChange;
    double taxChange;

    public AI(ASADModel asadModel, SolowSwanGrowthModel solowSwanGrowthModel) throws IOException {
        this.asadModel = asadModel;
        this.solowSwanGrowthModel = solowSwanGrowthModel;
        ArffLoader arffLoader = new ArffLoader();
        file = new File("src/main/resources/growth-info.arff");
        arffLoader.setFile(file);
        instances = arffLoader.getDataSet();
    }

    public void calculateRequiredChanges() {
        bondChange = asadModel.calculateBondChange();
        reserveMultiplier = asadModel.calculateReserveMultiplier();
        spendingChange = asadModel.calculateSpendingChange();
        taxChange = asadModel.calculateTaxChange();
    }

    public int makeRandomChoice(int range) {
        Random random = new Random();
        return (int) random.nextDouble() * range;
    }

    // rule based AI
    public void ruleBasedDecisions() throws Exception {
        calculateRequiredChanges();
        int choice = makeRandomChoice(2);
        if (asadModel.overallPublicBalanceInflationAdjusted < asadModel.overallGovtBalanceInflationAdjusted) { // if our govt finances are better than public finances
            if (asadModel.outputGap < 0) { // if equilibrium output is above lras
                if (choice == 0) {
                    asadModel.changeMoneySupply(bondChange);
                } else if (choice == 1) {
                    asadModel.changeReserveRequirements(reserveMultiplier);
                }
            } else if (asadModel.outputGap > 0) { // if equilibrium output is below lras
                if (choice == 0) {

                    asadModel.changeSpending(spendingChange);
                } else if (choice == 1) {
                    asadModel.changeTaxes(taxChange);
                }
            }
        } else if (asadModel.overallPublicBalanceInflationAdjusted > asadModel.overallGovtBalanceInflationAdjusted) { // if our public finances are better than govt finances
            if (asadModel.outputGap < 0) {  // if equilibrium output is above lras
                if (choice == 0) {
                    asadModel.changeSpending(spendingChange);
                } else if (choice == 1) {
                    asadModel.changeTaxes(taxChange);
                }
            } else if (asadModel.outputGap > 0) { // if equilibrium output is below lras
                if (choice == 0) {
                    asadModel.changeMoneySupply(bondChange);
                } else if (choice == 1) {
                    asadModel.changeReserveRequirements(reserveMultiplier);
                }
            }
        }
        asadModel.runCycle();
        recordInfo();
    }

    // fuzzy logic
    public void fuzzyLogic() throws Exception {
        calculateRequiredChanges();
        int choice = makeRandomChoice(2);

        String fileName = "src/main/resources/economy.fcl";
        FIS fis = FIS.load(fileName,true);

        // Set inputs
        //fis.setVariable("publicBalance", asadModel.overallPublicBalanceInflationAdjusted);
        fis.setVariable("govtBalance", asadModel.overallGovtBalanceInflationAdjusted);
       // fis.setVariable("growth", asadModel.overallGrowth);
        fis.setVariable("og", asadModel.outputGap);
        // Evaluate
        fis.evaluate();

        double spending = fis.getVariable("govtSpending").getLatestDefuzzifiedValue();

        if(choice == 0){
            asadModel.changeTaxes(spending / asadModel.taxMultiplier);
        } else if (choice == 1){
            asadModel.changeSpending(spending / asadModel.spendingMultiplier);
        }

        recordInfo();
    }

    // goal oriented behavior
    public void goalOrientedBehavior() throws Exception {
        calculateRequiredChanges();
        double inflation;
        double publicBalance;
        double govtBalance;
        double growth;
        double gdp;

        int positiveBondChange = 5;
        int negativeBondChange = -5;
        float positiveReserveMultiplier = 2;
        float negativeReserveMultiplier = 0.5f;
        float positiveSpendingChange = 5;
        float negativeSpendingChange = -5;
        float negativeTaxChange = -5;
        float positiveTaxChange = 5;

        double economicHealth = 0;
        int option = 0;
        for (int i = 0; i < 8; i++) {
            ASADModel testModel = asadModel;
            if (i == 0) {
                testModel.changeMoneySupply(bondChange);
                testModel.runCycle();
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
            } else if (i == 1) {
                testModel.changeMoneySupply(bondChange);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else if (i == 2) {
                testModel.changeReserveRequirements(reserveMultiplier);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else if (i == 3) {
                testModel.changeReserveRequirements(reserveMultiplier);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else if (i == 4) {
                testModel.changeSpending(spendingChange);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else if (i == 5) {
                testModel.changeSpending(spendingChange);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else if (i == 6) {
                testModel.changeTaxes(taxChange);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            } else {
                testModel.changeTaxes(taxChange);
                inflation = testModel.overallInflation;
                publicBalance = testModel.overallPublicBalanceInflationAdjusted;
                govtBalance = testModel.overallGovtBalanceInflationAdjusted;
                growth = testModel.overallGrowth;
                gdp = testModel.longRunAggregateSupply;
                economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                if (gdp * growth - (publicBalance + govtBalance) * inflation > economicHealth) {
                    economicHealth = gdp * growth - (publicBalance + govtBalance) * inflation;
                    option = i;
                }
            }
        }

        if (option == 0) {
            asadModel.changeMoneySupply(positiveBondChange);
        } else if (option == 1) {
            asadModel.changeMoneySupply(negativeBondChange);
        } else if (option == 2) {
            asadModel.changeReserveRequirements(positiveReserveMultiplier);
        } else if (option == 3) {
            asadModel.changeReserveRequirements(negativeReserveMultiplier);
        } else if (option == 4) {
            asadModel.changeSpending(positiveSpendingChange);
        } else if (option == 5) {
            asadModel.changeSpending(negativeSpendingChange);
        } else if (option == 6) {
            asadModel.changeTaxes(negativeTaxChange);
        } else {
            asadModel.changeTaxes(positiveTaxChange);
        }
        recordInfo();
    }

    public void machineLearningRegression() throws Exception {
        Classifier classifier = new LinearRegression(); // may need to use different regression method
        classifier.buildClassifier(instances);

        //Evaluation eval = new Evaluation(instances);
        //eval.evaluateModel(classifier, instances); // where testing dataset would be
        double[] test = new double[instances.numAttributes()];
        /*test[0] = asadModel.overallInflation;
        test[1] = asadModel.outputGap;
        test[2] = asadModel.overallPublicBalanceInflationAdjusted;
        test[3] = asadModel.overallGovtBalanceInflationAdjusted;*/

        test[0] = 0;
        test[1] = 0;
        test[2] = 0;
        test[3] = 0;
        test[4] = 0;
        Instance predicationDataSet = new DenseInstance(1.0, test);
        double value = classifier.classifyInstance(predicationDataSet);

        recordInfo();
    }

    // regression
    public void recordInfo() throws Exception {
        double[] denseInstance = new double[instances.numAttributes()];
        /*denseInstance[0] = asadModel.overallInflation;
        denseInstance[1] = asadModel.outputGap;
        denseInstance[2] = asadModel.overallPublicBalanceInflationAdjusted;
        denseInstance[3] = asadModel.overallGovtBalanceInflationAdjusted;*/
        denseInstance[0] = asadModel.taxes;
        denseInstance[1] = asadModel.G;
        denseInstance[2] = asadModel.ownedBonds;
        denseInstance[3] = asadModel.reserveRequirement;
        denseInstance[4] = asadModel.overallGrowth;

        instances.add(new DenseInstance(1.0, denseInstance));

        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(instances);
        arffSaver.setFile(file);
        arffSaver.writeBatch();
    }


}
