package economicModel;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.Random;
import java.util.Scanner;

//TODO: still need to incorporate inflation somehow, maybe price should affect capital?
public class Main {
    public static void main(String args[]) throws Exception {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object

        ASADModel asadModel = new ASADModel();
        SolowSwanGrowthModel solowSwanGrowthModel = new SolowSwanGrowthModel();
        //starting variables
        asadModel.debtRepaymentAmount = 10;
        asadModel.cyclesRun = 0;
        asadModel.growth = 1;
        asadModel.overallGrowth = 1;
        asadModel.inflation = 1;
        asadModel.overallInflation = 1;
        double technology = 1;
        double deprecation = 0.005;
        solowSwanGrowthModel.capital = 18000;
        solowSwanGrowthModel.Labour = 100;
        asadModel.ownedBonds = 10;
        asadModel.reserveRequirement = 0.125;
        asadModel.taxes = 100;
        asadModel.GConstant = 100;
        asadModel.mpc = 0.6;
        asadModel.mpi = 0.1;
        asadModel.mps = 1 - asadModel.mpc - asadModel.mpi;
        asadModel.taxMultiplier = -asadModel.mpc / asadModel.mps;
        asadModel.spendingMultiplier = 1 / asadModel.mps;
        double savingsGrowth = asadModel.mps + asadModel.mpi;
        AI ai = new AI();
        boolean isPlaying = true;
        while (isPlaying) {
            System.out.println("Press m for manual play, press a for ai play");
            String mode = scanner.nextLine();
            if (mode.equals("m")) {
                System.out.println("Cycle number " + (asadModel.cyclesRun + 1));
                System.out.println("Press enter to run Solow Model cycle");
                scanner.nextLine();
                double populationGrowth = 0;

                if (asadModel.cyclesRun != 0) {
                    double intrinsicGrowth = 1 / (solowSwanGrowthModel.outputPerPerson * 1000);
                    double carryingCapacity = (int) solowSwanGrowthModel.outputPerPerson * 1000;
                    populationGrowth = calculatePopulationGrowth(intrinsicGrowth, solowSwanGrowthModel.Labour, carryingCapacity);
                }

                solowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

                System.out.println("-*Solow Model Information*-");
                System.out.println("Population Growth rate: " + populationGrowth);
                System.out.println("Total Output: " + solowSwanGrowthModel.output);

                asadModel.longRunAggregateSupply = solowSwanGrowthModel.output;

                // TODO: not sure if these are ideal
                asadModel.C = asadModel.longRunAggregateSupply * asadModel.mpc;
                asadModel.IConstant = asadModel.longRunAggregateSupply * asadModel.mpi;
                //inflation = quantity * velocity;
                //money supply * velocity of money = price level * real gdp
                //price level * real gdp = nominal gdp
                System.out.println('\n' + "Press enter to run ASAD Model cycle");
                scanner.nextLine();
                asadModel.runCycle();

                System.out.println("-*ASAD Model Information pre-adjustment*-");
                printData(asadModel);

                System.out.println('\n' + "Select option for policy adjustment:" +
                        '\n' + "t for taxes" +
                        '\n' + "g for government spending" +
                        '\n' + "m for money supply" +
                        '\n' + "r for reserve requirement");
                String option = scanner.nextLine();
                switch (option) {
                    case "t":
                        // if we want to change taxes
                        System.out.println("How much do you wish to change taxes by?");
                        double taxChange = scanner.nextDouble();
                        asadModel.changeTaxes(taxChange);
                        System.out.println("Taxes: " + asadModel.taxes);
                        break;
                    case "g":
                        // if we want to change spending
                        System.out.println("How much do you wish to change spending by?");
                        double spendingChange = scanner.nextDouble();
                        asadModel.changeSpending(spendingChange);
                        System.out.println("Government Spending: " + asadModel.G);
                        break;
                    case "m":
                        // if we want to change money supply
                        System.out.println("How much do you wish to change bonds owned by?");
                        double bondChange = scanner.nextDouble();
                        asadModel.changeMoneySupply(bondChange);
                        System.out.println("Money supply: " + asadModel.moneySupply);
                        break;
                    case "r":
                        // if we want to change reserve requirement
                        System.out.println("How much do you wish to change reserve requirement by?");
                        double reserveMultiplier = scanner.nextDouble();
                        asadModel.changeReserveRequirements(reserveMultiplier);
                        System.out.println("Reserve Requirement: " + asadModel.reserveRequirement);
                    default:
                        System.out.println("invalid option");
                        throw new Exception();
                }
                asadModel.runCycle();
                ai.runCycleAndRecordInfo(asadModel);
                System.out.println('\n' + "-*ASAD Model Information Post-adjustment*-");
                printData(asadModel);

                technology += (asadModel.I / 1000);
                System.out.println("Technology Level: " + technology);
                System.out.println('\n' + "Press enter to continue to next cycle, or type e and press enter to end program");
                if (scanner.nextLine().equals("e")) {
                    isPlaying = false;
                }
            } else if (mode.equals("a")) {
                System.out.println("Enter number of cycles for AI to run");
                int cyclesToRun = Integer.parseInt(scanner.nextLine());
                while(cyclesToRun > 0){
                    cyclesToRun--;
                    runAICycle(ai, asadModel, solowSwanGrowthModel, savingsGrowth, technology, deprecation);
                }
            }
        }
    }

    private static void runAICycle(AI ai, ASADModel asadModel, SolowSwanGrowthModel solowSwanGrowthModel, double savingsGrowth, double technology, double deprecation) throws Exception {
        System.out.println("Cycle number " + (asadModel.cyclesRun + 1));
        //System.out.println("Press enter to run Solow Model cycle");
        //scanner.nextLine();
        double populationGrowth = 0;

        if (asadModel.cyclesRun != 0) {
            double intrinsicGrowth = 1 / (solowSwanGrowthModel.outputPerPerson * 1000);
            double carryingCapacity = (int) solowSwanGrowthModel.outputPerPerson * 1000;
            populationGrowth = calculatePopulationGrowth(intrinsicGrowth, solowSwanGrowthModel.Labour, carryingCapacity);
        }

        solowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

        //System.out.println("-*Solow Model Information*-");
        //System.out.println("Population Growth rate: " + populationGrowth);
        //System.out.println("Total Output: " + solowSwanGrowthModel.output);

        asadModel.longRunAggregateSupply = solowSwanGrowthModel.output;

        asadModel.C = asadModel.longRunAggregateSupply * asadModel.mpc;
        asadModel.IConstant = asadModel.longRunAggregateSupply * asadModel.mpi;
        //inflation = quantity * velocity;
        //money supply * velocity of money = price level * real gdp
        //price level * real gdp = nominal gdp
        //System.out.println('\n' + "Press enter to run ASAD Model cycle");
        //scanner.nextLine();
        asadModel.runCycle();

        //printData(asadModel);
        //System.out.println("-*ASAD Model Information pre-adjustment*-");
       // System.out.println("Long Run Aggregate Supply: " + asadModel.longRunAggregateSupply);
       // System.out.println("Average growth Rate: " + (((asadModel.overallGrowth - 1) * 100) / asadModel.cyclesRun) + '%');

       /* System.out.println('\n' + "Select option for ai testing:" +
                '\n' + "r for rule based decisions" +
                '\n' + "f for fuzzy logic" +
                '\n' + "g for goal oriented behavior" +
                '\n' + "m for machine learning");
        String option = scanner.nextLine();*/

        ArffLoader arffLoader = new ArffLoader();
        File file = new File(ai.arffFilePath);
        arffLoader.setFile(file);
        Instances instances = arffLoader.getDataSet();
        int bound;
        if(instances.size() == 0){
            bound = 3;
        } else {
            bound = 4;
        }
        Random random = new Random();
        int option = random.nextInt(bound);
        switch (option) {
            case 0:
                asadModel = ai.ruleBasedDecisions(asadModel);
                break;
            case 1:
                asadModel = ai.fuzzyLogic(asadModel);
                break;
            case 2:
                asadModel = ai.goalOrientedBehavior(asadModel);
                break;
            case 3:
                asadModel = ai.machineLearningRegression(asadModel);
                break;
            default:
                System.out.println("invalid option");
                throw new Exception();
        }
        System.out.println('\n' + "-*ASAD Model Information Post-adjustment*-");
        //printData(asadModel);
        System.out.println("Long Run Aggregate Supply: " + asadModel.longRunAggregateSupply);
        System.out.println("Average growth Rate: " + (((asadModel.overallGrowth - 1) * 100) / asadModel.cyclesRun) + '%' + '\n');

        technology += (asadModel.I / 1000);
        //System.out.println("Technology Level: " + technology);
        //System.out.println('\n' + "Press enter to continue to next cycle, or type e and press enter to end program");
        /*if (scanner.nextLine().equals("e")) {
            isPlaying = false;
        }*/
    }

    private static void printData(ASADModel asadModel) {
        System.out.println("-*Output Gap Data*-");
        System.out.println("Long Run Aggregate Supply: " + asadModel.longRunAggregateSupply);
        System.out.println("Aggregate Demand: " + asadModel.aggregateDemandOutputCurve);
        System.out.println("Short Run aggregate supply: " + asadModel.shortRunAggregateSupplyCurve);
        System.out.println("Equilibrium output: " + asadModel.equilibriumOutput);
        System.out.println("Gap: " + asadModel.outputGap);

        System.out.println('\n' + "-*Financial Data*-");
        System.out.println("Taxes: " + asadModel.taxes);
        System.out.println("Government Spending: " + asadModel.G);
        System.out.println("Consumption: " + asadModel.C);
        System.out.println("Investment: " + asadModel.I);
        System.out.println("Reserve Requirement: " + asadModel.reserveRequirement);

        System.out.println('\n' + "-*Inflation Data*-");
        System.out.println("Price Level: " + asadModel.priceLevel);
        System.out.println("Inflation Rate for last cycle: " + ((asadModel.inflation - 1) * 100) + '%');
        System.out.println("Average Inflation Rate: " + (((asadModel.overallInflation - 1) * 100) / asadModel.cyclesRun) + '%');

        System.out.println('\n' + "-*Debt and deficit Data*-");
        System.out.println("Government Balance: " + asadModel.govtBalance);
        System.out.println("Public Balance: " + asadModel.publicBalance);
        System.out.println("Total Government Debt: " + asadModel.overallGovtBalanceInflationAdjusted);
        System.out.println("Total Public Debt: " + asadModel.overallPublicBalanceInflationAdjusted);

        System.out.println('\n' + "-*Economic growth information*-");
        System.out.println("Growth Rate for last cycle: " + ((asadModel.growth - 1) * 100) + '%');
        System.out.println("Average growth Rate: " + (((asadModel.overallGrowth - 1) * 100) / asadModel.cyclesRun) + '%');
    }

    private static double calculatePopulationGrowth(double intrinsicGrowthRate, int currentPopulation, double carryingCapacity) {
        return intrinsicGrowthRate * currentPopulation * (1 - (double) currentPopulation / carryingCapacity);
    }
}
