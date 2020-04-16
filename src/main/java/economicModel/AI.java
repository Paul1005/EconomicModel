package economicModel;

import weka.classifiers.functions.*;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import net.sourceforge.jFuzzyLogic.FIS;

import java.io.File;
import java.io.IOException;
import java.util.Random;

//TODO: need to test to see which of these techniques works best, and if they perform better than a human
public class AI {
    private Random random;
    public String arffFilePath;
    private double oldLRAS;

    public AI() {
        random = new Random();
        arffFilePath = "src/main/resources/growth-info.arff";
        oldLRAS = -1;
    }

    // rule based AI
    public ASADModel ruleBasedDecisions(ASADModel asadModel) throws Exception {
        double spendingChange = asadModel.calculateSpendingChange();
        double taxChange = asadModel.calculateTaxChange();

        double investmentRequired = asadModel.calculateInvestmentRequired();
        double bondChange = asadModel.calculateBondChange(investmentRequired);
        double reserveMultiplier = asadModel.calculateReserveMultiplier(investmentRequired);

        if (asadModel.getOverallPublicBalance() < asadModel.getOverallGovtBalance()) { // if our govt finances are better than public finances
            govtIsRich(asadModel, spendingChange, taxChange, bondChange, reserveMultiplier);
        } else if (asadModel.getOverallPublicBalance() > asadModel.getOverallGovtBalance()) { // if our public finances are better than govt finances
            publicIsRich(asadModel, spendingChange, taxChange, bondChange, reserveMultiplier);
        } else if (asadModel.getOverallPublicBalance() == asadModel.getOverallGovtBalance()) { // if they are identical
            int choice = random.nextInt(2);
            if (choice == 0) {
                govtIsRich(asadModel, spendingChange, taxChange, bondChange, reserveMultiplier);
            } else if (choice == 1) {
                publicIsRich(asadModel, spendingChange, taxChange, bondChange, reserveMultiplier);
            }
        }

        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    private void publicIsRich(ASADModel asadModel, double spendingChange, double taxChange, double bondChange, double reserveMultiplier) {
        int choice = random.nextInt(2);
        if (asadModel.getOutputGap() > 0) {  // if equilibrium output is above lras
            if (choice == 0) {
                asadModel.changeSpending(spendingChange);
            } else if (choice == 1) {
                asadModel.changeTaxes(taxChange);
            }
        } else if (asadModel.getOutputGap() < 0) { // if equilibrium output is below lras
            if (choice == 0) {
                asadModel.changeMoneySupply(bondChange);
            } else if (choice == 1) {
                asadModel.changeReserveRequirements(reserveMultiplier);
            }
        }
    }

    private void govtIsRich(ASADModel asadModel, double spendingChange, double taxChange, double bondChange, double reserveMultiplier) {
        int choice = random.nextInt(2);
        if (asadModel.getOutputGap() > 0) { // if equilibrium output is above lras
            if (choice == 0) {
                asadModel.changeMoneySupply(bondChange);
            } else if (choice == 1) {
                asadModel.changeReserveRequirements(reserveMultiplier);
            }
        } else if (asadModel.getOutputGap() < 0) { // if equilibrium output is below lras
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

        double balanceNeutral = asadModel.getLongRunAggregateSupply() / 2;
        double balanceHigh = asadModel.getLongRunAggregateSupply();
        double spendingNeutral = asadModel.getLongRunAggregateSupply() / 8;
        double spendingHigh = asadModel.getLongRunAggregateSupply() / 4;
        double ogLow = asadModel.getLongRunAggregateSupply() / 2 / 4;
        double ogHigh = asadModel.getLongRunAggregateSupply() / 2 / 2;

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
        fis.setVariable("publicBalance", asadModel.getOverallPublicBalance());
        fis.setVariable("govtBalance", asadModel.getOverallGovtBalance());
        fis.setVariable("og", asadModel.getOutputGap() / 2); // since we're doing both public and govt spending, divide og by 2
        // Evaluate
        fis.evaluate();

        double govtSpending = fis.getVariable("govtSpending").getLatestDefuzzifiedValue();
        double publicSpending = fis.getVariable("publicSpending").getLatestDefuzzifiedValue();

        if (asadModel.getG() + govtSpending <= 0) {
            asadModel.changeTaxes(govtSpending / asadModel.getTaxMultiplier());
        } else {
            asadModel.changeSpending(govtSpending / asadModel.getSpendingMultiplier());
        }

        int choice = random.nextInt(2);

        if (choice == 0) {
            double bonds = asadModel.calculateBondChange(publicSpending + asadModel.getI());
            asadModel.changeMoneySupply(bonds);
        } else if (choice == 1) {
            double reserveRequirement = asadModel.calculateReserveMultiplier(publicSpending + asadModel.getI());
            asadModel.changeReserveRequirements(reserveRequirement);
        }

        runCycleAndRecordInfo(asadModel);
        return asadModel;
    }

    // goal oriented behavior
    public ASADModel goalOrientedBehavior(ASADModel asadModel) throws Exception {
        double bondChange = asadModel.getMoneySupply() / 128;
        double positiveReserveMultiplier = 2;
        double negativeReserveMultiplier = 0.5f;
        double spendingChange = asadModel.getLongRunAggregateSupply() / 128;
        double taxChange = asadModel.getLongRunAggregateSupply() / 96;

        double economicHealth = 0;
        int option = 0;
        for (int i = 0; i < 9; i++) {
            ASADModel testModel = new ASADModel(asadModel);
            tryOption(testModel, bondChange, positiveReserveMultiplier, negativeReserveMultiplier, spendingChange, taxChange, i);
            testModel.runCycle();
            double inflation = testModel.getOverallInflation();
            double publicBalance = testModel.getOverallPublicBalance();
            double govtBalance = testModel.getOverallGovtBalance();
            double growth = testModel.getOverallGrowth();
            double gdp = testModel.getLongRunAggregateSupply();
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
        return gdp * growth - ((publicBalance + govtBalance) / inflation);
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
            if (taxChange > asadModel.getTaxes()) {
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
        double bondChange = asadModel.getMoneySupply() / 128;
        double positiveReserveMultiplier = 2;
        double negativeReserveMultiplier = 0.5f;
        double spendingChange = asadModel.getLongRunAggregateSupply() / 128;
        double taxChange = asadModel.getLongRunAggregateSupply() / 96;
        double LRASGrowth = 0;
        int option = 0;

        for (int i = 0; i < 9; i++) {
            double[] denseInstance = new double[instances.numAttributes()];
            denseInstance[0] = asadModel.getTaxes();
            denseInstance[1] = asadModel.getG();
            denseInstance[2] = asadModel.getOwnedBonds();
            denseInstance[3] = asadModel.getReserveRequirement();
            if (i == 0) {
                denseInstance[i] = asadModel.getTaxes() + taxChange;
            } else if (i == 1) {
                denseInstance[i] = asadModel.getG() + spendingChange;
            } else if (i == 2) {
                denseInstance[i] = asadModel.getOwnedBonds() + bondChange;
            } else if (i == 3) {
                denseInstance[i] = asadModel.getReserveRequirement() * positiveReserveMultiplier;
            } else if (i == 4) {
                denseInstance[i - 4] = asadModel.getTaxes() - taxChange;
            } else if (i == 5) {
                denseInstance[i - 4] = asadModel.getG() - spendingChange;
            } else if (i == 6) {
                denseInstance[i - 4] = asadModel.getOwnedBonds() - bondChange;
            } else if (i == 7) {
                denseInstance[i - 4] = asadModel.getReserveRequirement() * negativeReserveMultiplier;
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
    private void runCycleAndRecordInfo(ASADModel asadModel) throws Exception {
        asadModel.runCycle();
        recordInfo(asadModel);
    }

    public void recordInfo(ASADModel asadModel) throws IOException {
        double LRASGrowth;
        if (oldLRAS == -1) {
            LRASGrowth = 1;
        } else {
            LRASGrowth = asadModel.getLongRunAggregateSupply() / oldLRAS;
        }
        if (asadModel.getCyclesRun() > 3) { // only run it if we have run enough cycles to see lras growth
            ArffLoader arffLoader = new ArffLoader();
            File file = new File(arffFilePath);
            arffLoader.setFile(file);
            Instances instances = arffLoader.getDataSet();
            double[] denseInstance = new double[instances.numAttributes()];
            denseInstance[0] = asadModel.getTaxes();
            denseInstance[1] = asadModel.getG();
            denseInstance[2] = asadModel.getOwnedBonds();
            denseInstance[3] = asadModel.getReserveRequirement();
            denseInstance[4] = LRASGrowth;

            instances.add(new DenseInstance(1.0, denseInstance));

            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(instances);
            arffSaver.setFile(file);
            arffSaver.writeBatch();
        }
        oldLRAS = asadModel.getLongRunAggregateSupply();
    }
}
