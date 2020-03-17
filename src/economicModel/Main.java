package economicModel;

import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        // preset gdp values
   /*     float consumption = 100;
        float investment = 100;
        float govtSpending = 100;
        float exports = 100;
        float imports = 100;

        EconomicModel economicModel = new EconomicModel(consumption, investment, govtSpending, exports, imports);

        economicModel.debtOrReserves = -1000f;

        int turnNum = 1;
        while(true) {
            System.out.println("Turn number: " + turnNum);
            System.out.println("Total GDP: " + economicModel.gdp);
            System.out.println("Please enter tax rate");
            String taxationPercentage = scanner.nextLine();
            System.out.println("Available money " + Float.parseFloat(taxationPercentage)/100  * economicModel.gdp);
            System.out.println("Please enter Spending");
            String spending = scanner.nextLine();
            System.out.println("Please enter money supply change");
            String money = scanner.nextLine();
            economicModel.runCycle(Float.parseFloat(taxationPercentage), Float.parseFloat(spending), Float.parseFloat(money));

            System.out.println("Interest Rate" + economicModel.interestRate * 100);
            System.out.println("Deficit or surplus" + economicModel.deficitOrSurplus);
            System.out.println("Debt or Reserves" + economicModel.debtOrReserves + "\n");
            turnNum++;
        }
    */

        //Testing
   /*     float savingsGrowth = 0.5f;
        float populationGrowth = 0.0f;
        float technology = 1.5f;
        float deprecation = 0.005f;
        int startingPopulation = 100;
        int startingCapital = 183712;

        SolowSwanGrowthModel.capital = startingCapital;
        SolowSwanGrowthModel.Labour = startingPopulation;
        while(true) {
            System.out.println("Press enter to run cycle");
            scanner.nextLine();
            SolowSwanGrowthModel.runCycle(savingsGrowth, populationGrowth, technology, deprecation);
            System.out.println("Capital per person: " + SolowSwanGrowthModel.capitalPerPerson);
            System.out.println("Output/GDP per person: " + SolowSwanGrowthModel.outputPerPerson);
            System.out.println("Gain per person: " + SolowSwanGrowthModel.netGainPerPerson);
            System.out.println("Steady state capital per person: " + SolowSwanGrowthModel.steadyStateCapitalPerPerson);
            //System.out.println("Steady state capital: " + SolowSwanGrowthModel.steadyStateCapital);
            System.out.println("Steady state output per person: " + SolowSwanGrowthModel.steadyStateOutputPerPerson);
            //System.out.println("Steady state output: " + SolowSwanGrowthModel.steadyStateOutput);
        }*/
        ASADModel.longRunAggregateSupply = 500;
        ASADModel.C = 500;
        ASADModel.GConstant = 50;
        ASADModel.IConstant = 50;
        ASADModel.ownedBonds = 10;
        ASADModel.reserveRequirement = 0.125;
        ASADModel.taxes = 100;
        ASADModel.mpc = 0.6;
        ASADModel.mpi = 0.1;
        ASADModel.mps = 1 - ASADModel.mpc - ASADModel.mpi;
        ASADModel.debtRepaymentAmount = 1;
        //inflation = quantity * velocity;
        //money supply * velocity of money = price level * real gdp
        //price level * real gdp = nominal gdp
        while (true) {
            System.out.println("Press enter to run cycle");
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
            System.out.println("Select option for policy adjustment");
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
                    break;
                case "m":
                    // if we want to change money supply
                    ASADModel.changeMoneySupply();
                    break;
                case "r":
                    // if we want to change reserve requirement
                    ASADModel.changeReserveRequirements();
                    System.out.println("Reserve Requirement: " + ASADModel.reserveRequirement);
            }
            ASADModel.runCycle();
            System.out.println("Price Level: " + ASADModel.priceLevel);
            System.out.println("Money supply: " + ASADModel.moneySupply);
            System.out.println("Aggregate Demand: " + ASADModel.aggregateDemandOutputCurve);
            System.out.println("Short Run aggregate supply: " + ASADModel.shortRunAggregateSupplyCurve);
            System.out.println("Equilibrium output: " + ASADModel.equilibriumOutput);
            System.out.println("Government Spending: " + ASADModel.G);
            System.out.println("Total Government Debt: " + ASADModel.overallGovtBalanceWInterest);
            System.out.println("Total Public Debt: " + ASADModel.overallPublicBalanceWInterest);
        }
    }
}
