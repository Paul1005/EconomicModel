package economicModel;

import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object

        //starting variables
        ASADModel.debtRepaymentAmount = 1;
        ASADModel.cyclesRun = 1;
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
                System.out.println("Press enter to run Solow Model cycle");
                scanner.nextLine();

                double intrinsicGrowth = 1 / (SolowSwanGrowthModel.outputPerPerson / 100);
                int carryingCapacity = (int) SolowSwanGrowthModel.outputPerPerson / 100;
                double populationGrowth = calculatePopulationGrowth(intrinsicGrowth, SolowSwanGrowthModel.Labour, carryingCapacity);
                SolowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);

                System.out.println("Capital per person: " + SolowSwanGrowthModel.capitalPerPerson);
                System.out.println("Output/GDP per person: " + SolowSwanGrowthModel.outputPerPerson);
                System.out.println("Gain per person: " + SolowSwanGrowthModel.netGainPerPerson);
                System.out.println("Steady state capital per person: " + SolowSwanGrowthModel.steadyStateCapitalPerPerson);
                System.out.println("Steady state capital: " + SolowSwanGrowthModel.steadyStateCapital);
                System.out.println("Steady state output per person: " + SolowSwanGrowthModel.steadyStateOutputPerPerson);
                System.out.println("Steady state output: " + SolowSwanGrowthModel.steadyStateOutput);

                ASADModel.longRunAggregateSupply = SolowSwanGrowthModel.steadyStateOutput;

                ASADModel.C = ASADModel.longRunAggregateSupply * ASADModel.mpc;
                ASADModel.IConstant = ASADModel.longRunAggregateSupply * ASADModel.mpi;
                //inflation = quantity * velocity;
                //money supply * velocity of money = price level * real gdp
                //price level * real gdp = nominal gdp
                System.out.println("Press enter to run ASAD cycle");
                scanner.nextLine();
                ASADModel.runCycle();
                System.out.println("Aggregate Demand: " + ASADModel.aggregateDemandOutputCurve);
                System.out.println("Short Run aggregate supply: " + ASADModel.shortRunAggregateSupplyCurve);
                System.out.println("Equilibrium output: " + ASADModel.equilibriumOutput);
                System.out.println("Gap: " + ASADModel.outputGap);
                System.out.println("Taxes: " + ASADModel.taxes);
                System.out.println("Government Spending: " + ASADModel.G);
                System.out.println("Consumption: " + ASADModel.C);
                System.out.println("Reserve Requirement: " + ASADModel.reserveRequirement);
                System.out.println("Price Level: " + ASADModel.priceLevel);
                System.out.println("Long Run aggregate supply: " + ASADModel.longRunAggregateSupply);
                System.out.println("Select option for policy adjustment:" +
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

                System.out.println("Price Level: " + ASADModel.priceLevel);
                System.out.println("Aggregate Demand: " + ASADModel.aggregateDemandOutputCurve);
                System.out.println("Short Run aggregate supply: " + ASADModel.shortRunAggregateSupplyCurve);
                System.out.println("Equilibrium output: " + ASADModel.equilibriumOutput);
                System.out.println("Total Government Debt: " + ASADModel.overallGovtBalanceWInterest);
                System.out.println("Total Public Debt: " + ASADModel.overallPublicBalanceWInterest);
                System.out.println("Long Run aggregate supply: " + ASADModel.longRunAggregateSupply);

                technology += (ASADModel.I / 100);
                System.out.println("Technology Level: " + technology);

                System.out.println("Growth Rate for last cycle: " + ((ASADModel.growthRate - 1) * 100) + '%');
                System.out.println("Average growth Rate: " + ((ASADModel.averageGrowthRate - 1) * 100) + '%');
                System.out.println("Type e and press enter to end program");
                if (scanner.nextLine().equals("e")) {
                    isPlaying = false;
                }
            } else if (mode.equals("a")) {
                AI ai = new AI();

            }
        }
    }

    private static double calculatePopulationGrowth(double intrinsicGrowthRate, int currentPopulation, int carryingCapacity) {
        return intrinsicGrowthRate * currentPopulation * (1 - (float) currentPopulation / (float) carryingCapacity);
    }
}
