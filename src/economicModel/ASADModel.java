package economicModel;

import java.util.ArrayList;

//TODO: incorporate inflation somehow, right now incentive is to keep price level at 1
public class ASADModel {
    public static double longRunAggregateSupply;
    public static double shortRunAggregateSupplyCurve;
    public static double taxes;
    public static double mpc;
    public static double mpi;
    public static double mps;
    public static double reserveRequirement;
    public static double ownedBonds;
    public static double moneySupply;
    public static double GConstant;
    public static double IConstant;
    public static double G;
    public static double outputGap;
    public static double C;
    public static double aggregateDemandOutputCurve;
    public static double equilibriumOutput;
    public static double priceLevel;
    public static double I;

    private static double taxMultiplier;
    private static double spendingMultiplier;

    //TODO: these should somehow be affected by inflation
    public static double govtBalance;
    public static double overallGovtBalance;
    public static double overallGovtBalanceWInterest;
    public static double publicBalance;
    public static double overallPublicBalance;
    public static double overallPublicBalanceWInterest;

    public static double publicDebtInterest;
    public static double govtDebtInterest;

    //public static int debtCycles; // number of cycles we use to pay of debt
    public static int debtRepaymentAmount; // min debt repayment required
    private static ArrayList<Double> govtDebts = new ArrayList<>();
    private static ArrayList<Double> publicDebts = new ArrayList<>();

    static double growthRate = 0;
    static double averageGrowthRate = 0;
    static int cyclesRun;
    private static double originalOutput = 0;
    private static double previousOutput = 0;
    /**
     * Find investment based on interest rate, IConstant, and mpi. Is the inverse(swap x and y) of the equation below.
     *
     * @param interestRate
     * @return
     */
    private static double investmentEquation(double interestRate) {
        return (Math.sqrt(Math.pow(IConstant * mpi, 2) * (Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate)) / Math.sqrt(2));
    }

    /**
     * Find interest rate based on investment, IConstant, and mpi. Is the inverse(swap x and y) of the equation above.
     *
     * @param investmentRequired
     * @return
     */
    private static double interestRateEquation(double investmentRequired) {
        return ((Math.pow(IConstant, 4) * Math.pow(mpi, 4) - Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(mpi, 2) * Math.pow(investmentRequired, 2)));
    }

    /**
     * Find money supply based on interest rate and long run aggregate supply.
     *
     * @param interestRate
     * @return
     */
    private static double moneySupplyEquation(double interestRate) {
        return -interestRate * longRunAggregateSupply + longRunAggregateSupply;
    }

    private static double baseDebtInterestEquation(double totalAssets, double currentBalance) {
        return 1 / (2 * totalAssets + totalAssets * averageGrowthRate) * (Math.sqrt(Math.pow(currentBalance, 2) + 4) - currentBalance); // may not work in cases of negative assets or very negative growth
    }

    static void runCycle() {
        taxMultiplier = -mpc / mps;
        spendingMultiplier = 1 / mps;

        moneySupply = ownedBonds / reserveRequirement; // find money supply based on bonds and reserve requirement
        double interestRate = (longRunAggregateSupply - moneySupply) / longRunAggregateSupply; // find interest rate based on current money supply
        I = investmentEquation(interestRate); // overall investment

        publicBalance = IConstant - I;
        if (publicBalance < 0) {
            publicDebtInterest = (baseDebtInterestEquation(IConstant, publicBalance) + interestRate) / 2; // might need a better equation for this
            takeOutLoan(publicDebts, publicBalance);
            overallPublicBalance += publicBalance;
            overallPublicBalanceWInterest = overallPublicBalance + overallPublicBalance * publicDebtInterest;
            servicePublicDebt();
        } else if (govtBalance > 0) {
            repayPublicLoan(publicBalance);
        }

        G = GConstant * spendingMultiplier; // overall government spending

        equilibriumOutput = C + taxes * taxMultiplier + G + I; // should equal LRAS when price is set to one

        priceLevel = (Math.sqrt(4 * C * longRunAggregateSupply + Math.pow(G, 2) + 2 * G * taxes * taxMultiplier + 4 * longRunAggregateSupply * I + Math.pow(taxes, 2) * Math.pow(taxMultiplier, 2)) + G + taxes * taxMultiplier) / (2 * longRunAggregateSupply); // find our equilibrium price level
        aggregateDemandOutputCurve = (C + I) / priceLevel + G + taxes * taxMultiplier; // this is the aggregate demand curve
        shortRunAggregateSupplyCurve = longRunAggregateSupply * priceLevel; // this is the short run aggregate supply curve

        outputGap = longRunAggregateSupply - equilibriumOutput; // find the output gap so that our price will be one

        govtBalance = taxes - GConstant;

        if (govtBalance < 0) {
            govtDebtInterest = (baseDebtInterestEquation(longRunAggregateSupply, govtBalance) + interestRate) / 2; // might need a better equation for this
            takeOutLoan(govtDebts, govtBalance);
            overallGovtBalance += govtBalance;
            overallGovtBalanceWInterest = overallGovtBalance + overallGovtBalance * govtDebtInterest;
            serviceGovtDebt();
        } else if (govtBalance > 0) {
            repayGovtLoan(govtBalance);
        }

        if (cyclesRun == 0) {
            originalOutput = equilibriumOutput;
        } else {
            growthRate = equilibriumOutput / previousOutput;
            averageGrowthRate = equilibriumOutput / originalOutput;
            previousOutput = equilibriumOutput;
        }
        cyclesRun++;
    }

    private static void takeOutLoan(ArrayList<Double> debts, double balance) {
        debts.add(Math.abs(balance));
    }

    static void changeReserveRequirements() {
        double investmentRequired = longRunAggregateSupply - C - G - taxes * taxMultiplier; // find how much investment we need
        double interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        reserveRequirement *= (moneySupply / newMoneySupply); // determine the new reserve requirement based on the new and old money supply
    }

    static void changeMoneySupply() {
        double investmentRequired = longRunAggregateSupply - C - G - taxes * taxMultiplier; // find how much investment we need
        double interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        double gap = newMoneySupply - moneySupply; // determine how much more money we need
        double bondChange = gap * reserveRequirement; // determine how many more bonds we need to buy or sell
        ownedBonds += bondChange; // add the change in bonds
    }

    static void changeSpending() {
        double spendingChange = outputGap / spendingMultiplier; // find the change in spending required
        GConstant += spendingChange; // add spending change to government spending
    }

    static void changeTaxes() {
        double taxChange = outputGap / taxMultiplier; // find the change in taxes required
        if (taxes + taxChange <= 0) {
            System.out.println("can't cut taxes enough");
        } else {
            taxes += taxChange; // add tax change to total taxes
        }
    }

    private static void repayGovtLoan(double govtBalance) {
        overallGovtBalanceWInterest -= govtBalance;
    }

    private static void repayPublicLoan(double publicBalance) {
        overallPublicBalanceWInterest -= publicBalance;
    }

    private static void serviceGovtDebt() {
        overallGovtBalanceWInterest = overallGovtBalance + overallGovtBalance * govtDebtInterest;
        GConstant -= (debtRepaymentAmount * govtDebtInterest);
        overallGovtBalanceWInterest -= (debtRepaymentAmount + debtRepaymentAmount * govtDebtInterest);
    }

    private static void servicePublicDebt() {
        overallPublicBalanceWInterest = overallPublicBalance + overallPublicBalance * publicDebtInterest;
        C -= (debtRepaymentAmount * publicDebtInterest);
        overallPublicBalanceWInterest -= (debtRepaymentAmount + debtRepaymentAmount * publicDebtInterest);
    }
}
