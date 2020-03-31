package economicModel;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.pmml.consumer.GeneralRegression;
import weka.classifiers.functions.IsotonicRegression;
import weka.classifiers.functions.PaceRegression;
import weka.classifiers.pmml.consumer.Regression;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AI {
    private ASADModel asadModel;
    private SolowSwanGrowthModel solowSwanGrowthModel;
    Instances instances;
    File file;
    public AI(ASADModel asadModel, SolowSwanGrowthModel solowSwanGrowthModel) throws IOException {
        this.asadModel = asadModel;
        this.solowSwanGrowthModel = solowSwanGrowthModel;
        ArffLoader arffLoader = new ArffLoader();
        file = new File("src/main/resources/growth-info.arff");
        arffLoader.setFile(file);

        instances =  arffLoader.getDataSet();;
    }

    // rule based AI
    public void ruleBasedDecisions() throws Exception {
        Random random = new Random();
        int choice = (int) random.nextDouble() * 4;

        double bondChange = asadModel.calculateBondChange();
        double reserveMultiplier = asadModel.calculateReserveMultiplier();
        double spendingChange = asadModel.calculateSpendingChange();
        double taxChange = asadModel.calculateTaxChange();

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
        recordInfo();
    }

    // fuzzy logic
    public void fuzzyLogic() throws Exception {
        if (asadModel.overallPublicBalanceInflationAdjusted > 0) {
            asadModel.changeMoneySupply(5);
            asadModel.changeReserveRequirements(0.5);
        } else if (asadModel.overallPublicBalanceInflationAdjusted < 0) {
            asadModel.changeMoneySupply(-5);
            asadModel.changeReserveRequirements(2);
        }

        if (asadModel.overallGovtBalanceInflationAdjusted < 0) {
            asadModel.changeSpending(-5);
            asadModel.changeTaxes(5);
        } else if (asadModel.overallGovtBalanceInflationAdjusted > 0) {
            asadModel.changeSpending(5);
            asadModel.changeTaxes(-5);
        }
        recordInfo();
    }

    // goal oriented behavior
    public void goalOrientedBehavior() throws Exception {
        double inflation = asadModel.overallInflation;
        double publicBalance = asadModel.overallPublicBalanceInflationAdjusted;
        double govtBalance = asadModel.overallGovtBalanceInflationAdjusted;
        double growth = asadModel.overallGrowth;
        double investment = asadModel.I;

        asadModel.changeMoneySupply(5);
        /*
        * inflation++
        * publicBalance--
        * govtBalance =
        * growth ++
        * investment ++
        * */


        asadModel.changeMoneySupply(-5);
        /*
         * inflation--
         * publicBalance++
         * govtBalance =
         * growth --
         * investment --
         * */

        asadModel.changeReserveRequirements(0.5);
        /*
         * inflation++
         * publicBalance--
         * govtBalance =
         * growth ++
         * investment ++
        * */

        asadModel.changeReserveRequirements(2);
        /*
         * inflation--
         * publicBalance++
         * govtBalance =
         * growth --
         * investment --
         * */

        asadModel.changeSpending(5);
        /*
         * inflation++
         * publicBalance=
         * govtBalance--
         * growth ++
         * investment =
        * */

        asadModel.changeSpending(-5);
        /*
         * inflation--
         * publicBalance=
         * govtBalance++
         * growth --
         * investment =
         * */

        asadModel.changeTaxes(-5);
        /*
         * inflation++
         * publicBalance=
         * govtBalance--
         * growth ++
         * investment =
         * */

        asadModel.changeTaxes(5);
        /*
         * inflation--
         * publicBalance=
         * govtBalance++
         * growth --
         * investment =
         * */
        recordInfo();
    }

    public void machineLearningRegression() throws Exception {
        Classifier classifier = new LinearRegression();
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
