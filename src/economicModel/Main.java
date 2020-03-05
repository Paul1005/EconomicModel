package economicModel;

import java.util.Scanner;

public class Main {
    public static void main(String args[]){
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
        ASADModel.longRungAggregateSupply = 200;
        ASADModel.CConstant = 100;
        ASADModel.G = 100;
        ASADModel.IConstant = 100;
        ASADModel.moneySupply = 1000;
        ASADModel.reserveRequirement = 0.125f;
        ASADModel.taxes = 10;
        ASADModel.mpc = 0.7f;
        ASADModel.mps = 0.3f;
        while(true) {
            System.out.println("Press enter to run cycle");
            scanner.nextLine();
            ASADModel.runCycle();
            System.out.println("Current output: " + ASADModel.aggregateDemand);
            System.out.println("Gap: " + ASADModel.gap);
            System.out.println("Select option for policy adjustment");
            String option = scanner.nextLine();
            switch (option) {
                case "t":
                    //if we want to change taxes
                    ASADModel.changeTaxes();
                    System.out.println("Taxes: " +  ASADModel.taxes);
                    break;
                case "s":
                    //if we want to change spending
                    ASADModel.changeSpending();
                    System.out.println("Government Spending: " +  ASADModel.G);
                    break;
                case "m":
                    //if we want to change money supply
                    ASADModel.changeMoneySupply();
                    System.out.println("Money supply: " +  ASADModel.moneySupply);
                    break;
            }
            ASADModel.runCycle();
            System.out.println("Current output: " + ASADModel.aggregateDemand);
        }
    }
}
