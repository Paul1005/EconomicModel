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
    public static double C; // Should maybe be affected by inflation
    public static double aggregateDemandOutputCurve;
    public static double equilibriumOutput;
    public static double I;

    private static double taxMultiplier;
    private static double spendingMultiplier;

    //TODO: these should somehow be affected by inflation
    public static double govtBalance;
    private static double overallGovtBalance;
    private static double overallGovtBalanceWInterest;
    public static double publicBalance;
    private static double overallPublicBalance;
    private static double overallPublicBalanceWInterest;
    public static double overallGovtBalanceInflationAdjusted;
    public static double overallPublicBalanceInflationAdjusted;

    public static double publicDebtInterest;
    public static double govtDebtInterest;

    //public static int debtCycles; // number of cycles we use to pay of debt
    public static int debtRepaymentAmount; // min debt repayment required
    private static ArrayList<Double> govtDebts = new ArrayList<>();
    private static ArrayList<Double> publicDebts = new ArrayList<>();

    static double growth;
    static double overallGrowth;
    static int cyclesRun;
    private static double originalOutput = 0;
    private static double previousOutput = 0;

    public static double priceLevel;
    private static double previousPriceLevel;
    private static double originalPriceLevel;
    public static double overallInflation;
    public static double inflation;


    /**
     * Find investment based on interest rate, IConstant, and mpi. Is the inverse(swap x and y) of the equation below. \frac{a\sqrt{\sqrt{x^{2}+4}-x}}{\sqrt{2}\cdot b}
     *
     * @param interestRate
     * @return
     */
    private static double investmentEquation(double interestRate) {
        return IConstant * Math.sqrt(Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate) / (Math.sqrt(2) * overallInflation);
    }

    /**
     * Find interest rate based on investment, IConstant, and mpi. Is the inverse(swap x and y) of the equation above. \frac{a^{4}-b^{4}\cdot x^{4}}{a^{2}\cdot b^{2}\cdot x^{2}}
     *
     * @param investmentRequired
     * @return
     */
    private static double interestRateEquation(double investmentRequired) {
        return (Math.pow(IConstant, 4) - Math.pow(overallInflation, 4) * Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2)* Math.pow(overallInflation, 2) * Math.pow(investmentRequired, 2));
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

    /**
     * Find the interest rate multiplier based on how fast your economy is growing, how large your debt is, and how large your economy is. \frac{\sqrt{x^{2}+4}-x}{\left(2\cdot a+a\cdot b\right)}
     * @param totalAssets
     * @param currentBalance
     * @return
     */
    private static double baseDebtInterestEquation(double totalAssets, double currentBalance) {
        return 1 / (2 * totalAssets + totalAssets * overallGrowth) * (Math.sqrt(Math.pow(currentBalance, 2) + 4) - currentBalance); // may not work in cases of negative assets or very negative growth
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
            originalPriceLevel = priceLevel;
        } else {
            growth = equilibriumOutput / previousOutput;
            overallGrowth = equilibriumOutput / originalOutput;
            inflation = priceLevel / previousPriceLevel;
            overallInflation = priceLevel / originalPriceLevel;
        }
        previousOutput = equilibriumOutput;
        previousPriceLevel = priceLevel;

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

    // repay loans using surplus
    private static void repayGovtLoan(double govtBalance) {
        overallGovtBalance -= govtBalance;
    }

    private static void repayPublicLoan(double publicBalance) {
        overallPublicBalance -= publicBalance;
    }

    // overall debt servicing, might need to make these harsher
    private static void serviceGovtDebt() {
        overallGovtBalanceWInterest = overallGovtBalance + overallGovtBalance * govtDebtInterest;
        GConstant -= (debtRepaymentAmount * govtDebtInterest);
        overallGovtBalanceWInterest -= (debtRepaymentAmount + debtRepaymentAmount * govtDebtInterest);
        overallGovtBalanceInflationAdjusted = overallGovtBalanceWInterest / priceLevel;
    }

    private static void servicePublicDebt() {
        overallPublicBalanceWInterest = overallPublicBalance + overallPublicBalance * publicDebtInterest;
        C -= (debtRepaymentAmount * publicDebtInterest);
        overallPublicBalanceWInterest -= (debtRepaymentAmount + debtRepaymentAmount * publicDebtInterest);
        overallPublicBalanceInflationAdjusted = overallPublicBalanceWInterest / priceLevel;
    }
}
