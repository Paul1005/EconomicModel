package economicModel;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;

public class AI {
    private ASADModel asadModel;
    private SolowSwanGrowthModel solowSwanGrowthModel;

    public AI(ASADModel asadModel, SolowSwanGrowthModel solowSwanGrowthModel) {
        this.asadModel = asadModel;
        this.solowSwanGrowthModel = solowSwanGrowthModel;
    }

    // rule based AI
    public void ruleBasedDecisions() throws IOException {
        Random random = new Random();
        int choice = (int) random.nextDouble() * 4;

        double bondChange = asadModel.calculateBondChange();
        double reserveMultiplier = asadModel.calculateReserveMultiplier();
        double spendingChange = asadModel.calculateSpendingChange();
        double taxChange = asadModel.calculateTaxchange();

        if (asadModel.overallPublicBalanceInflationAdjusted < asadModel.overallGovtBalanceInflationAdjusted) { // if our govt finances are better than public finances
            if (asadModel.outputGap < 0) { // if equilibrium output is above lras
                asadModel.changeMoneySupply(bondChange);
                asadModel.changeReserveRequirements(reserveMultiplier);
            } else if (asadModel.outputGap > 0) { // if equilibrium output is below lras
                asadModel.changeSpending(spendingChange);
                asadModel.changeTaxes(taxChange);
            }
        } else if (asadModel.overallPublicBalanceInflationAdjusted > asadModel.overallGovtBalanceInflationAdjusted) { // if our public finances are better than govt finances
            if (asadModel.outputGap < 0) {  // if equilibrium output is above lras
                asadModel.changeSpending(spendingChange);
                asadModel.changeTaxes(taxChange);
            } else if (asadModel.outputGap > 0) { // if equilibrium output is below lras
                asadModel.changeMoneySupply(bondChange);
                asadModel.changeReserveRequirements(reserveMultiplier);
            }
        }

        asadModel.runCycle();

    }

    // fuzzy logic
    public void fuzzyLogic() {
        if (asadModel.overallPublicBalanceInflationAdjusted > 0) {

        } else if (asadModel.overallPublicBalanceInflationAdjusted < 0) {

        }

        if (asadModel.overallGovtBalanceInflationAdjusted < 0) {

        } else if (asadModel.overallGovtBalanceInflationAdjusted > 0) {

        }
    }

    // goal oriented behavior
    public void goalOrientedBehavior() {
        double inflation = asadModel.overallInflation;
        double publicBalance= asadModel.overallPublicBalanceInflationAdjusted;
        double govtBalance= asadModel.overallGovtBalanceInflationAdjusted;
        double growth = asadModel.overallGrowth;
        double investment = asadModel.I;

        
    }

    // regression
    public void machineLearningRegression() {
        ArffLoader arffLoader = new ArffLoader();
        arffLoader.setFile(new File("src/main/resources/growth-info.arff"));
        Enumeration<Attribute> attributeEnumeration = arffLoader.getDataSet().enumerateAttributes();
        ArrayList<Attribute> attributes = Collections.list(attributeEnumeration);

        Instances instances = new Instances("GDP records", attributes, 0);

        double[] denseInstance = new double[instances.numAttributes()];
        denseInstance[0] = asadModel.overallInflation;
        denseInstance[1] = asadModel.outputGap;
        denseInstance[2] = asadModel.overallPublicBalanceInflationAdjusted;
        denseInstance[3] = asadModel.overallGovtBalanceInflationAdjusted;
        denseInstance[4] = asadModel.taxes;
        denseInstance[5] = asadModel.G;
        denseInstance[6] = asadModel.ownedBonds;
        denseInstance[7] = asadModel.reserveRequirement;
        denseInstance[8] = asadModel.overallGrowth;

        instances.add(new DenseInstance(1.0, denseInstance));
    }


}
