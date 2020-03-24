package economicModel;

import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object

        //starting variables
        ASADModel.debtRepaymentAmount = 1;
        ASADModel.cyclesRun = 0;
        double technology = 1;
        double deprecation = 0.005;
        SolowSwanGrowthModel.capital = 18000;
        SolowSwanGrowthModel.Labour = 100;
        ASADModel.ownedBonds = 10;
        ASADModel.reserveRequirement = 0.125;
        ASADModel.taxes = 100;
        ASADModel.GConstant = 100;
        ASADModel.mpc = 0.6;
        ASADModel.mpi = 0.1;
        ASADModel.mps = 1 - ASADModel.mpc - ASADModel.mpi;
        double savingsGrowth = ASADModel.mps + ASADModel.mpi;

        boolean isPlaying = true;
        while (isPlaying) {
            System.out.println("Press m for manual play, press a for ai play");
            String mode = scanner.nextLine();
            if (mode.equals("m")) {
                System.out.println("Cycle number " + (ASADModel.cyclesRun + 1));
                System.out.println("Press enter to run Solow Model cycle");
                scanner.nextLine();
                double populationGrowth = 0;

                if (ASADModel.cyclesRun != 0) {
                    double intrinsicGrowth = 1 / (SolowSwanGrowthModel.outputPerPerson / 100);
                    int carryingCapacity = (int) SolowSwanGrowthModel.outputPerPerson / 100;
                    populationGrowth = calculatePopulationGrowth(intrinsicGrowth, SolowSwanGrowthModel.Labour, carryingCapacity);
                }
                System.out.println("-*Solow Model Information*-");
                System.out.println("Population Growth rate: " + populationGrowth);
                SolowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

                System.out.println("Capital per person: " + SolowSwanGrowthModel.capitalPerPerson);
                System.out.println("Output/GDP per person: " + SolowSwanGrowthModel.outputPerPerson);
                System.out.println("Gain per person: " + SolowSwanGrowthModel.netGainPerPerson);
                System.out.println("Total Output: " + SolowSwanGrowthModel.output);
                //System.out.println("Steady state capital per person: " + SolowSwanGrowthModel.steadyStateCapitalPerPerson);
                //System.out.println("Steady state capital: " + SolowSwanGrowthModel.steadyStateCapital);
                //System.out.println("Steady state output per person: " + SolowSwanGrowthModel.steadyStateOutputPerPerson);
                //System.out.println("Steady state output: " + SolowSwanGrowthModel.steadyStateOutput);

                ASADModel.longRunAggregateSupply = SolowSwanGrowthModel.output;

                ASADModel.C = ASADModel.longRunAggregateSupply * ASADModel.mpc;
                ASADModel.IConstant = ASADModel.longRunAggregateSupply * ASADModel.mpi;
                //inflation = quantity * velocity;
                //money supply * velocity of money = price level * real gdp
                //price level * real gdp = nominal gdp
                System.out.println('\n' + "Press enter to run ASAD Model cycle");
                scanner.nextLine();
                ASADModel.runCycle();

                System.out.println("-*ASAD Model Information pre-adjustment*-");
                printData();

                System.out.println('\n' + "Select option for policy adjustment:" +
                        '\n' + "t for taxes" +
                        '\n' + "s for government spending" +
                        '\n' + "m for money supply" +
                        '\n' + "r for reserve requirement");
                String option = scanner.nextLine();
                switch (option) {
                    case "t":
                        // if we want to change taxes
                        ASADModel.changeTaxes();
                        System.out.println("Taxes: " + ASADModel.taxes);
                        break;
                    case "s":
                        // if we want to change spending
                        ASADModel.changeSpending();
                        System.out.println("Government Spending: " + ASADModel.G);
                        break;
                    case "m":
                        // if we want to change money supply
                        ASADModel.changeMoneySupply();
                        System.out.println("Money supply: " + ASADModel.moneySupply);
                        break;
                    case "r":
                        // if we want to change reserve requirement
                        ASADModel.changeReserveRequirements();
                        System.out.println("Reserve Requirement: " + ASADModel.reserveRequirement);
                }
                ASADModel.runCycle();

                System.out.println('\n' + "-*ASAD Model Information Post-adjustment*-");
                printData();

                technology += (ASADModel.I / 100);
                System.out.println('\n' + "-*Economic growth information*-");
                System.out.println("Technology Level: " + technology);
                System.out.println("Growth Rate for last cycle: " + ((ASADModel.growthRate - 1) * 100) + '%');
                System.out.println("Average growth Rate: " + ((ASADModel.averageGrowthRate - 1) * 100) + '%');
                System.out.println('\n' + "Type e and press enter to end program");
                if (scanner.nextLine().equals("e")) {
                    isPlaying = false;
                }
            } else if (mode.equals("a")) {
                AI ai = new AI();

            }
        }
    }

    private static void printData() {
        System.out.println("-*Output Gap Data*-");
        System.out.println("Long Run Aggregate Supply: " + ASADModel.longRunAggregateSupply);
        System.out.println("Aggregate Demand: " + ASADModel.aggregateDemandOutputCurve);
        System.out.println("Short Run aggregate supply: " + ASADModel.shortRunAggregateSupplyCurve);
        System.out.println("Equilibrium output: " + ASADModel.equilibriumOutput);
        System.out.println("Gap: " + ASADModel.outputGap);

        System.out.println('\n' + "-*Financial Data*-");
        System.out.println("Taxes: " + ASADModel.taxes);
        System.out.println("Government Spending: " + ASADModel.G);
        System.out.println("Consumption: " + ASADModel.C);
        System.out.println("Investment: " + ASADModel.I);
        System.out.println("Reserve Requirement: " + ASADModel.reserveRequirement);
        System.out.println("Price Level: " + ASADModel.priceLevel);

        System.out.println('\n' + "-*Debt and deficit Data*-");
        System.out.println("Government Balance: " + ASADModel.govtBalance);
        System.out.println("Public Balance: " + ASADModel.publicBalance);
        System.out.println("Total Government Debt: " + ASADModel.overallGovtBalanceWInterest);
        System.out.println("Total Public Debt: " + ASADModel.overallPublicBalanceWInterest);
    }

    private static double calculatePopulationGrowth(double intrinsicGrowthRate, int currentPopulation, int carryingCapacity) {
        return intrinsicGrowthRate * currentPopulation * (1 - (float) currentPopulation / (float) carryingCapacity);
    }
}
