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
        asadModel.setDebtRepaymentAmount(1);
        asadModel.setCyclesRun(0);
        asadModel.setOverallGrowth(1);
        asadModel.setOverallInflation(1);
        double technology = 1;
        double deprecation = 0.005;
        solowSwanGrowthModel.setCapital(18000);
        solowSwanGrowthModel.setLabour(100);
        asadModel.setOwnedBonds(10);
        asadModel.setReserveRequirement(0.125);
        asadModel.setTaxes(100);
        asadModel.setGConstant(100);
        asadModel.setmpc(0.6);
        asadModel.setmpi(0.1);
        asadModel.setmps(1 - asadModel.getmpc() - asadModel.getmpi());
        asadModel.setTaxMultiplier(-asadModel.getmpc() / asadModel.getmps());
        asadModel.setSpendingMultiplier(1 / asadModel.getmps());
        double savingsGrowth = asadModel.getmps() + asadModel.getmpi();
        AI ai = new AI();
        boolean isPlaying = true;
        while (isPlaying) {
            System.out.println("Press m for manual play, press a for ai play");
            String mode = scanner.nextLine();
            if (mode.equals("m")) {
                System.out.println("Cycle number " + (asadModel.getCyclesRun() + 1));
                System.out.println("Press enter to run Solow Model cycle");
                scanner.nextLine();
                double populationGrowth = 0;

                if (asadModel.getCyclesRun() != 0) {
                    double intrinsicGrowth = 1 / (solowSwanGrowthModel.outputPerPerson * 1000);
                    double carryingCapacity = (int) solowSwanGrowthModel.outputPerPerson * 1000;
                    populationGrowth = calculatePopulationGrowth(intrinsicGrowth, solowSwanGrowthModel.labour, carryingCapacity);
                }

                solowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

                System.out.println("-*Solow Model Information*-");
                System.out.println("Population Growth rate: " + populationGrowth);
                System.out.println("Total Output: " + solowSwanGrowthModel.output);

                asadModel.setLongRunAggregateSupply(solowSwanGrowthModel.output);

                // TODO: not sure if these are ideal
                asadModel.setC(asadModel.getLongRunAggregateSupply() * asadModel.getmpc());
                asadModel.setIConstant(asadModel.getLongRunAggregateSupply() * asadModel.getmpi());
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
                        System.out.println("Size of tax change needed: " + asadModel.calculateTaxChange());
                        double taxChange = scanner.nextDouble();
                        asadModel.changeTaxes(taxChange);
                        System.out.println("Taxes: " + asadModel.getTaxes());
                        break;
                    case "g":
                        // if we want to change spending
                        System.out.println("How much do you wish to change spending by?");
                        System.out.println("Size of spending change needed: " + asadModel.calculateSpendingChange());
                        double spendingChange = scanner.nextDouble();
                        asadModel.changeSpending(spendingChange);
                        System.out.println("Government Spending: " + asadModel.getG());
                        break;
                    case "m":
                        // if we want to change money supply
                        System.out.println("How much do you wish to change bonds owned by?");
                        System.out.println("Size of bond change needed: " + asadModel.calculateBondChange(asadModel.getInvestmentRequired()));
                        double bondChange = scanner.nextDouble();
                        asadModel.changeMoneySupply(bondChange);
                        System.out.println("Money supply: " + asadModel.getMoneySupply());
                        break;
                    case "r":
                        // if we want to change reserve requirement
                        System.out.println("How much do you wish to change reserve requirement by?");
                        System.out.println("Size of reserve requirement change needed: " + asadModel.calculateReserveMultiplier(asadModel.getInvestmentRequired()));
                        double reserveMultiplier = scanner.nextDouble();
                        asadModel.changeReserveRequirements(reserveMultiplier);
                        System.out.println("Reserve Requirement: " + asadModel.getReserveRequirement());
                        break;
                    default:
                        System.out.println("invalid option");
                        throw new Exception();
                }
                asadModel.runCycle();
                ai.recordInfo(asadModel);
                System.out.println('\n' + "-*ASAD Model Information Post-adjustment*-");
                printData(asadModel);

                technology += (asadModel.getI() / 1000);
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
        System.out.println("Cycle number " + (asadModel.getCyclesRun() + 1));
        //System.out.println("Press enter to run Solow Model cycle");
        //scanner.nextLine();
        double populationGrowth = 0;

        if (asadModel.getCyclesRun() != 0) {
            double intrinsicGrowth = 1 / (solowSwanGrowthModel.outputPerPerson * 1000);
            double carryingCapacity = (int) solowSwanGrowthModel.outputPerPerson * 1000;
            populationGrowth = calculatePopulationGrowth(intrinsicGrowth, solowSwanGrowthModel.labour, carryingCapacity);
        }

        solowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

        //System.out.println("-*Solow Model Information*-");
        //System.out.println("Population Growth rate: " + populationGrowth);
        //System.out.println("Total Output: " + solowSwanGrowthModel.output);

        asadModel.setLongRunAggregateSupply(solowSwanGrowthModel.getOutput());

        asadModel.setC(asadModel.getLongRunAggregateSupply() * asadModel.getmpc());
        asadModel.setIConstant(asadModel.getLongRunAggregateSupply() * asadModel.getmpi());
        //inflation = quantity * velocity;
        //money supply * velocity of money = price level * real gdp
        //price level * real gdp = nominal gdp
        //System.out.println('\n' + "Press enter to run ASAD Model cycle");
        //scanner.nextLine();
        asadModel.runCycle();
        System.out.println("-*ASAD Model Information pre-adjustment*-");
        printData(asadModel);
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
        int option = 0;//random.nextInt(bound);
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
        printData(asadModel);
        //System.out.println("Long Run Aggregate Supply: " + asadModel.longRunAggregateSupply);
        //System.out.println("Average growth Rate: " + (((asadModel.overallGrowth - 1) * 100) / asadModel.cyclesRun) + '%' + '\n');

        technology += (asadModel.getI() / 1000);
        System.out.println("Technology Level: " + technology);
        //System.out.println('\n' + "Press enter to continue to next cycle, or type e and press enter to end program");
        /*if (scanner.nextLine().equals("e")) {
            isPlaying = false;
        }*/
    }

    private static void printData(ASADModel asadModel) {
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
        System.out.println("Average Inflation Rate: " + (((asadModel.getOverallInflation() - 1) * 100) / asadModel.getCyclesRun()) + '%');

        System.out.println('\n' + "-*Debt and deficit Data*-");
        System.out.println("Government Balance: " + asadModel.getGovtBalance());
        System.out.println("Public Balance: " + asadModel.getPublicBalance());
        System.out.println("Total Government Debt: " + asadModel.getOverallGovtBalance());
        System.out.println("Total Public Debt: " + asadModel.getOverallPublicBalance());

        System.out.println('\n' + "-*Economic growth information*-");
        System.out.println("Growth Rate for last cycle: " + ((asadModel.getGrowth() - 1) * 100) + '%');
        System.out.println("Average growth Rate: " + (((asadModel.getOverallGrowth() - 1) * 100) / asadModel.getCyclesRun()) + '%');
    }

    private static double calculatePopulationGrowth(double intrinsicGrowthRate, int currentPopulation, double carryingCapacity) {
        return intrinsicGrowthRate * currentPopulation * (1 - (double) currentPopulation / carryingCapacity);
    }
}
