package economicModel;

import java.util.Scanner;

//TODO: still need to incorporate inflation somehow, maybe price should affect capital?
public class Main {
    public static void main(String args[]) throws Exception {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object

        ASADModel asadModel = new ASADModel();
        SolowSwanGrowthModel solowSwanGrowthModel = new SolowSwanGrowthModel();
        //starting variables
        asadModel.debtRepaymentAmount = 1;
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
        double savingsGrowth = asadModel.mps + asadModel.mpi;

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
                //System.out.println("Capital per person: " + SolowSwanGrowthModel.capitalPerPerson);
                //System.out.println("Output/GDP per person: " + SolowSwanGrowthModel.outputPerPerson);
                //System.out.println("Gain per person: " + SolowSwanGrowthModel.netGainPerPerson);
                System.out.println("Total Output: " + solowSwanGrowthModel.output);
                //System.out.println("Steady state capital per person: " + SolowSwanGrowthModel.steadyStateCapitalPerPerson);
                //System.out.println("Steady state capital: " + SolowSwanGrowthModel.steadyStateCapital);
                //System.out.println("Steady state output per person: " + SolowSwanGrowthModel.steadyStateOutputPerPerson);
                //System.out.println("Steady state output: " + SolowSwanGrowthModel.steadyStateOutput);

                asadModel.longRunAggregateSupply = solowSwanGrowthModel.output;

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
                        asadModel.changeTaxes();
                        System.out.println("Taxes: " + asadModel.taxes);
                        break;
                    case "g":
                        // if we want to change spending
                        asadModel.changeSpending();
                        System.out.println("Government Spending: " + asadModel.G);
                        break;
                    case "m":
                        // if we want to change money supply
                        asadModel.changeMoneySupply();
                        System.out.println("Money supply: " + asadModel.moneySupply);
                        break;
                    case "r":
                        // if we want to change reserve requirement
                        asadModel.changeReserveRequirements();
                        System.out.println("Reserve Requirement: " + asadModel.reserveRequirement);
                    default:
                        System.out.println("invalid option");
                        throw new Exception();
                }
                asadModel.runCycle();

                System.out.println('\n' + "-*ASAD Model Information Post-adjustment*-");
                printData(asadModel);

                technology += (asadModel.I / 1000);
                System.out.println("Technology Level: " + technology);
                System.out.println('\n' + "Press enter to continue to next cycle, or type e and press enter to end program");
                if (scanner.nextLine().equals("e")) {
                    isPlaying = false;
                }
            } else if (mode.equals("a")) {
                AI ai = new AI(asadModel, solowSwanGrowthModel);

            }
        }
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
        System.out.println("Inflation Rate for last cycle: "  + ((asadModel.inflation - 1) * 100) + '%');
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
        return intrinsicGrowthRate * currentPopulation * (1 - (double) currentPopulation /  carryingCapacity);
    }
}
