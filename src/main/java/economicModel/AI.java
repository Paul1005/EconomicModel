package economicModel;

import weka.classifiers.functions.*;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import net.sourceforge.jFuzzyLogic.FIS;

import java.io.File;
import java.util.Random;

public class AI {
    private double bondChange;
    private double reserveMultiplier;
    private double spendingChange;
    private double taxChange;
    private Random random;
    public String arffFilePath;
    private double oldLRAS;

    public AI() {
        random = new Random();
        arffFilePath = "src/main/resources/growth-info.arff";
        oldLRAS = -1;
    }

    public void calculateRequiredChanges(ASADModel asadModel) {
        double investmentRequired = asadModel.getInvestmentRequired(); // find how much investment we need
        bondChange = asadModel.calculateBondChange(investmentRequired);
        reserveMultiplier = asadModel.calculateReserveMultiplier(investmentRequired);
        spendingChange = asadModel.calculateSpendingChange();
        taxChange = asadModel.calculateTaxChange();
    }

    // rule based AI
    public ASADModel ruleBasedDecisions(ASADModel asadModel) throws Exception {
        calculateRequiredChanges(asadModel);
        if (asadModel.overallPublicBalanceInflationAdjusted < asadModel.overallGovtBalanceInflationAdjusted) { // if our govt finances are better than public finances
            govtIsRich(asadModel);
        } else if (asadModel.overallPublicBalanceInflationAdjusted > asadModel.overallGovtBalanceInflationAdjusted) { // if our public finances are better than govt finances
            publicIsRich(asadModel);
        } else if (asadModel.overallPublicBalanceInflationAdjusted == asadModel.overallGovtBalanceInflationAdjusted) { // if they are identical
            int choice = random.nextInt(2);
            if (choice == 0) {
                govtIsRich(asadModel);
            } else if (choice == 1) {
                publicIsRich(asadModel);
            }
        }

        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    private void publicIsRich(ASADModel asadModel) {
        int choice = random.nextInt(2);
        if (asadModel.outputGap > 0) {  // if equilibrium output is above lras
            if (choice == 0) {
                asadModel.changeSpending(spendingChange);
            } else if (choice == 1) {
                asadModel.changeTaxes(taxChange);
            }
        } else if (asadModel.outputGap < 0) { // if equilibrium output is below lras
            if (choice == 0) {
                asadModel.changeMoneySupply(bondChange);
            } else if (choice == 1) {
                asadModel.changeReserveRequirements(reserveMultiplier);
            }
        }
    }

    private void govtIsRich(ASADModel asadModel) {
        int choice = random.nextInt(2);
        if (asadModel.outputGap > 0) { // if equilibrium output is above lras
            if (choice == 0) {
                asadModel.changeMoneySupply(bondChange);
            } else if (choice == 1) {
                asadModel.changeReserveRequirements(reserveMultiplier);
            }
        } else if (asadModel.outputGap < 0) { // if equilibrium output is below lras
            if (choice == 0) {

                asadModel.changeSpending(spendingChange);
            } else if (choice == 1) {
                asadModel.changeTaxes(taxChange);
            }
        }
    }

    // fuzzy logic
    public ASADModel fuzzyLogic(ASADModel asadModel) throws Exception {
        String fileName = "src/main/resources/economy.fcl";
        FIS fis = FIS.load(fileName, true);

        double balanceNeutral = asadModel.longRunAggregateSupply / 2;
        double balanceHigh = asadModel.longRunAggregateSupply;
        double spendingNeutral = asadModel.longRunAggregateSupply / 8;
        double spendingHigh = asadModel.longRunAggregateSupply / 4;
        double ogLow = asadModel.longRunAggregateSupply / 2 / 4;
        double ogHigh = asadModel.longRunAggregateSupply / 2 / 2;

        fis.setVariable("balanceHighNegative", -balanceHigh);
        fis.setVariable("balanceNeutralNegative", -balanceNeutral);
        fis.setVariable("balanceNeutralPositive", balanceNeutral);
        fis.setVariable("balanceHighPositive", balanceHigh);
        fis.setVariable("spendingHighNegative", -spendingHigh);
        fis.setVariable("spendingNeutralNegative", -spendingNeutral);
        fis.setVariable("spendingNeutralPositive", spendingNeutral);
        fis.setVariable("spendingHighPositive", spendingHigh);
        fis.setVariable("ogHighNegative", -ogHigh);
        fis.setVariable("ogLowNegative", -ogLow);
        fis.setVariable("ogLowPositive", ogLow);
        fis.setVariable("ogHighPositive", ogHigh);

        // Set inputs
        fis.setVariable("publicBalance", asadModel.overallPublicBalanceInflationAdjusted);
        fis.setVariable("govtBalance", asadModel.overallGovtBalanceInflationAdjusted);
        fis.setVariable("og", asadModel.outputGap / 2); // since we're doing both public and govt spending, divide og by 2
        // Evaluate
        fis.evaluate();

        double govtSpending = fis.getVariable("govtSpending").getLatestDefuzzifiedValue();
        double publicSpending = fis.getVariable("publicSpending").getLatestDefuzzifiedValue();

        if (asadModel.G + govtSpending <= 0) {
            asadModel.changeTaxes(govtSpending / asadModel.taxMultiplier);
        } else {
            asadModel.changeSpending(govtSpending / asadModel.spendingMultiplier);
        }

        int choice = random.nextInt(2);

        if (choice == 0) {
            double bonds = asadModel.calculateBondChange(publicSpending + asadModel.I);
            asadModel.changeMoneySupply(bonds);
        } else if (choice == 1) {
            double reserveRequirement = asadModel.calculateReserveMultiplier(publicSpending + asadModel.I);
            asadModel.changeReserveRequirements(reserveRequirement);
        }

        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    // goal oriented behavior
    public ASADModel goalOrientedBehavior(ASADModel asadModel) throws Exception {
        double bondChange = asadModel.moneySupply / 128;
        double positiveReserveMultiplier = 2;
        double negativeReserveMultiplier = 0.5f;
        double spendingChange = asadModel.longRunAggregateSupply / 128;
        double taxChange = asadModel.longRunAggregateSupply / 96;

        double economicHealth = 0;
        int option = 0;
        for (int i = 0; i < 9; i++) {
            ASADModel testModel = new ASADModel(asadModel);
            tryOption(testModel, bondChange, positiveReserveMultiplier, negativeReserveMultiplier, spendingChange, taxChange, i);
            testModel.runCycle();
            double inflation = testModel.overallInflation;
            double publicBalance = testModel.overallPublicBalanceInflationAdjusted;
            double govtBalance = testModel.overallGovtBalanceInflationAdjusted;
            double growth = testModel.overallGrowth;
            double gdp = testModel.longRunAggregateSupply;
            if (i != 0) {
                if (getEconomicHealth(inflation, publicBalance, govtBalance, growth, gdp) > economicHealth) {
                    economicHealth = getEconomicHealth(inflation, publicBalance, govtBalance, growth, gdp);
                    option = i;
                }
            } else {
                economicHealth = getEconomicHealth(inflation, publicBalance, govtBalance, growth, gdp);
            }
        }

        tryOption(asadModel, bondChange, positiveReserveMultiplier, negativeReserveMultiplier, spendingChange, taxChange, option);
        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    private double getEconomicHealth(double inflation, double publicBalance, double govtBalance, double growth, double gdp) {
        return gdp * growth - (publicBalance + govtBalance) * inflation;
    }

    private void tryOption(ASADModel asadModel, double bondChange, double positiveReserveMultiplier, double negativeReserveMultiplier, double spendingChange, double taxChange, int option) {
        if (option == 0) {
            asadModel.changeMoneySupply(bondChange);
        } else if (option == 1) {
            asadModel.changeMoneySupply(-bondChange);
        } else if (option == 2) {
            asadModel.changeReserveRequirements(positiveReserveMultiplier);
        } else if (option == 3) {
            asadModel.changeReserveRequirements(negativeReserveMultiplier);
        } else if (option == 4) {
            asadModel.changeSpending(spendingChange);
        } else if (option == 5) {
            asadModel.changeSpending(-spendingChange);
        } else if (option == 6) {
            if (taxChange > asadModel.taxes) {
                asadModel.changeTaxes(-taxChange);
            }
        } else if (option == 7) {
            asadModel.changeTaxes(taxChange);
        } else {
            // do nothing
        }
    }

    public ASADModel machineLearningRegression(ASADModel asadModel) throws Exception {
        ArffLoader arffLoader = new ArffLoader();
        File file = new File(arffFilePath);
        arffLoader.setFile(file);
        Instances instances = arffLoader.getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);
        LinearRegression linearRegression = new LinearRegression();
        GaussianProcesses gaussianProcess = new GaussianProcesses(); // may need to use different regression method
        SMOreg smoReg = new SMOreg();

        linearRegression.buildClassifier(instances);
        smoReg.buildClassifier(instances);
        gaussianProcess.buildClassifier(instances);
        double bondChange = asadModel.moneySupply / 128;
        double positiveReserveMultiplier = 2;
        double negativeReserveMultiplier = 0.5f;
        double spendingChange = asadModel.longRunAggregateSupply / 128;
        double taxChange = asadModel.longRunAggregateSupply / 96;
        double LRASGrowth = 0;
        int option = 0;

        for (int i = 0; i < 9; i++) {
            double[] denseInstance = new double[instances.numAttributes()];
            denseInstance[0] = asadModel.taxes;
            denseInstance[1] = asadModel.G;
            denseInstance[2] = asadModel.ownedBonds;
            denseInstance[3] = asadModel.reserveRequirement;
            if (i == 0) {
                denseInstance[i] = asadModel.taxes + taxChange;
            } else if (i == 1) {
                denseInstance[i] = asadModel.G + spendingChange;
            } else if (i == 2) {
                denseInstance[i] = asadModel.ownedBonds + bondChange;
            } else if (i == 3) {
                denseInstance[i] = asadModel.reserveRequirement * positiveReserveMultiplier;
            } else if (i == 4) {
                denseInstance[i- 4] = asadModel.taxes - taxChange;
            } else if (i == 5) {
                denseInstance[i- 4] = asadModel.G - spendingChange;
            } else if (i == 6) {
                denseInstance[i- 4] = asadModel.ownedBonds - bondChange;
            } else if (i == 7) {
                denseInstance[i- 4] = asadModel.reserveRequirement * negativeReserveMultiplier;
            } else {
                // leave things the same
            }
            Instance predicationDataSet = new DenseInstance(1.0, denseInstance);
            predicationDataSet.setValue(predicationDataSet.numAttributes() - 1, '?');
            predicationDataSet.setDataset(instances);
            double newGrowth = (linearRegression.classifyInstance(predicationDataSet) + smoReg.classifyInstance(predicationDataSet) + gaussianProcess.classifyInstance(predicationDataSet)) / 3; // maybe do a better implementation for this

            if (i != 0) {
                if (newGrowth > LRASGrowth) {
                    LRASGrowth = newGrowth;
                    option = i;
                }
            } else {
                LRASGrowth = newGrowth;
            }
        }

        tryOption(asadModel, bondChange, positiveReserveMultiplier, negativeReserveMultiplier, spendingChange, taxChange, option);
        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    // regression
    public void runCycleAndRecordInfo(ASADModel asadModel) throws Exception {
        double LRASGrowth;
        if (oldLRAS == -1) {
            LRASGrowth = 1;
        } else {
            LRASGrowth = asadModel.longRunAggregateSupply / oldLRAS;
        }
        asadModel.runCycle();
        ArffLoader arffLoader = new ArffLoader();
        File file = new File(arffFilePath);
        arffLoader.setFile(file);
        Instances instances = arffLoader.getDataSet();
        double[] denseInstance = new double[instances.numAttributes()];
        denseInstance[0] = asadModel.taxes;
        denseInstance[1] = asadModel.G;
        denseInstance[2] = asadModel.ownedBonds;
        denseInstance[3] = asadModel.reserveRequirement;
        denseInstance[4] = LRASGrowth;

        instances.add(new DenseInstance(1.0, denseInstance));

        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(instances);
        arffSaver.setFile(file);
        arffSaver.writeBatch();
        oldLRAS = asadModel.longRunAggregateSupply;
    }
}
