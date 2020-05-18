package game;

import ai.AI;
import economicModels.ASADModel;
import economicModels.SolowSwanGrowthModel;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.Random;
import java.util.Scanner;
//TODO: debt should more directly affect lras
public class Game {

    public void run() throws Exception {
        ASADModel asadModel = new ASADModel();
        //starting variables
        asadModel.setDebtRepaymentAmount(1);
        asadModel.setOwnedBonds(10);
        asadModel.setReserveRequirement(0.125);
        asadModel.setTaxes(100);
        asadModel.setGConstant(100);
        asadModel.setmpc(0.6);
        asadModel.setmpi(0.1);
        asadModel.setmps(1 - asadModel.getmpc() - asadModel.getmpi());
        asadModel.setTaxMultiplier(-asadModel.getmpc() / asadModel.getmps());
        asadModel.setSpendingMultiplier(1 / asadModel.getmps());

        int cyclesToRun = 0;

        double technology = 1;
        double deprecation = 0.005;
        double savingsGrowth = asadModel.getmps() + asadModel.getmpi(); // should we include mpi or mps in this?
        SolowSwanGrowthModel solowSwanGrowthModel = new SolowSwanGrowthModel(18000, 100, 0);
        String mode = "";
        boolean isPlaying = true;

        AI ai = new AI();
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        while (isPlaying) {
            if (cyclesToRun == 0) {
                System.out.println("Press m for manual play, press a for ai play");
                mode = scanner.nextLine();
                if (mode.equals("a")) {
                    System.out.println("Enter number of cycles for AI to run");
                    cyclesToRun = Integer.parseInt(scanner.nextLine());
                }
            }

            System.out.println("Cycle number " + (solowSwanGrowthModel.getCyclesRun() + 1) + '\n');
            double populationGrowth = 0;

            if (solowSwanGrowthModel.getCyclesRun() > 0) {
                double intrinsicGrowth = 1 / (solowSwanGrowthModel.getOutputPerPerson() * 1000);
                double carryingCapacity = (int) solowSwanGrowthModel.getOutputPerPerson() * 1000;
                populationGrowth = calculatePopulationGrowth(intrinsicGrowth, solowSwanGrowthModel.getLabour(), carryingCapacity);
            }

            solowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

            System.out.println("-*Solow Model Information*-");
            System.out.println("Population Growth rate: " + populationGrowth);
            System.out.println("Total Output: " + solowSwanGrowthModel.getOutput() + '\n');

            asadModel.setLongRunAggregateSupply(solowSwanGrowthModel.getOutput());
            // TODO: not sure if these are ideal
            asadModel.setCConstant(asadModel.getLongRunAggregateSupply() * asadModel.getmpc());
            asadModel.setIConstant(asadModel.getLongRunAggregateSupply() * asadModel.getmpi());
            asadModel.runCycle();

            System.out.println("-*AS-AD Model Information pre-adjustment*-");
            printData(asadModel);
            System.out.println("Technology Level: " + technology + '\n');

            if (mode.equals("m")) {
                // if we want to change taxes
                System.out.println("How much do you wish to change taxes by? Type 0 for no change");
                System.out.println("Size of tax change needed to close the gap: " + asadModel.calculateTaxChange());
                double taxChange = scanner.nextDouble();
                if (taxChange != 0) {
                    asadModel.changeTaxes(taxChange);
                }

                // if we want to change spending
                System.out.println("How much do you wish to change spending by? Type 0 for no change");
                System.out.println("Size of spending change needed to close the gap: " + asadModel.calculateSpendingChange());
                double spendingChange = scanner.nextDouble();
                if (spendingChange != 0) {
                    asadModel.changeSpending(spendingChange);
                }

                double investmentRequired = asadModel.calculateInvestmentRequired();
                // if we want to change money supply
                System.out.println("How much do you wish to change bonds owned by? Type 0 for no change");
                System.out.println("Size of bond change needed to close the gap: " + asadModel.calculateBondChange(investmentRequired));
                double bondChange = scanner.nextDouble();
                if (bondChange != 0) {
                    asadModel.changeOwnedBonds(bondChange);
                }

                // if we want to change reserve requirement
                System.out.println("How much do you wish to change reserve requirement by? Type 1 for no change");
                System.out.println("Size of reserve requirement change needed to close the gap: " + asadModel.calculateReserveMultiplier(investmentRequired));
                double reserveMultiplier = scanner.nextDouble();
                if (reserveMultiplier != 0) {
                    asadModel.changeReserveRequirements(reserveMultiplier);
                }
            } else if (mode.equals("a")) {
                ArffLoader arffLoader = new ArffLoader();
                File file = new File(ai.arffFilePath);
                arffLoader.setFile(file);
                Instances instances = arffLoader.getDataSet();
                int bound;
                if (instances.size() == 0) {
                    bound = 3;
                } else {
                    bound = 4;
                }
                Random random = new Random();
                int option = random.nextInt(bound);
                switch (option) {
                    case 0:
                        System.out.println("Rule Based Decisions Selected");
                        asadModel = ai.ruleBasedDecisions(asadModel);
                        break;
                    case 1:
                        System.out.println("Fuzzy Logic Selected");
                        asadModel = ai.fuzzyLogic(asadModel);
                        break;
                    case 2:
                        System.out.println("Goal Oriented Behavior Selected");
                        asadModel = ai.goalOrientedBehavior(asadModel);
                        break;
                    case 3:
                        System.out.println("Machine Learning Selected");
                        asadModel = ai.machineLearningRegression(asadModel);
                        break;
                    default:
                        System.out.println("invalid option");
                        throw new Exception();
                }
            }
            asadModel.runCycle();
            ai.recordInfo(asadModel);
            System.out.println('\n' + "-*AS-AD Model Information Post-adjustment*-");
            printData(asadModel);
            technology = updateTechnology(asadModel, technology);
            System.out.println("Technology Level: " + technology);

            if (mode.equals("m")) {
                System.out.println('\n' + "Press enter to continue to next cycle, or type e and press enter to end program");
                if (scanner.nextLine().equals("e")) {
                    isPlaying = false;
                }
            } else if (mode.equals("a")) {
                cyclesToRun--;
            }
        }

    }

    private double calculatePopulationGrowth(double intrinsicGrowthRate, int currentPopulation, double carryingCapacity) {
        return intrinsicGrowthRate * currentPopulation * (1 - (double) currentPopulation / carryingCapacity);
    }

    private double updateTechnology(ASADModel asadModel, double technology) {
        technology += (Math.sqrt(asadModel.getI() + asadModel.getG() * asadModel.getmpi()) / 200);
        return technology;
    }

    private void printData(ASADModel asadModel) {
        System.out.println("-*Output Gap Data*-");
        System.out.println("Long Run Aggregate Supply: " + asadModel.getLongRunAggregateSupply());
        System.out.println("Aggregate Demand: " + asadModel.getAggregateDemandOutputCurve());
        System.out.println("Short Run aggregate supply: " + asadModel.getShortRunAggregateSupplyCurve());
        System.out.println("Equilibrium output: " + asadModel.getEquilibriumOutput());
        System.out.println("Gap: " + asadModel.getOutputGap());

        System.out.println('\n' + "-*Financial Data*-");
        System.out.println("Taxes: " + asadModel.getTaxes());
        System.out.println("Government Spending: " + asadModel.getG());
        System.out.println("Consumption: " + asadModel.getC());
        System.out.println("Investment: " + asadModel.getI());
        System.out.println("Reserve Requirement: " + asadModel.getReserveRequirement());

        System.out.println('\n' + "-*Inflation Data*-");
        System.out.println("Price Level: " + asadModel.getPriceLevel());
        System.out.println("Inflation Rate for last cycle: " + ((asadModel.getInflation() - 1) * 100) + '%');
        System.out.println("Average Inflation Rate: " + ((asadModel.getAverageInflation() - 1) * 100) + '%');

        System.out.println('\n' + "-*Debt and deficit Data*-");
        System.out.println("Government Balance: " + asadModel.getGovtBalance());
        System.out.println("Total Government Balance: " + asadModel.getOverallGovtBalance());
        System.out.println("Public Balance: " + asadModel.getPublicBalance());
        System.out.println("Total Public Balance: " + asadModel.getOverallPublicBalance());

        System.out.println('\n' + "-*Economic growth information*-");
        System.out.println("Growth Rate for last cycle: " + ((asadModel.getGrowth() - 1) * 100) + '%');
        System.out.println("Average growth Rate: " + (((asadModel.getOverallGrowth() - 1) * 100) / asadModel.getCyclesRun()) + '%' + '\n');
    }
}
