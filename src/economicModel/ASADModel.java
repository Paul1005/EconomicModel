package economicModel;

//TODO: incorporate inflation somehow
public class ASADModel {
    public static float longRunAggregateSupply;
    public static float shortRunAggregateSupply;
    public static float taxes;
    public static float mpc;
    public static float mpi;
    public static float mps;
    public static float reserveRequirement;
    public static float ownedBonds;
    public static float moneySupply;
    public static float CConstant;
    public static float GConstant;
    public static float IConstant;
    public static float G;
    public static float govtSpendingGap;
    public static float C;
    public static float aggregateDemand;
//    public static float priceLevel;

    private static float taxMultiplier;
    private static float spendingMultiplier;

    public static float govtBalance;
    public static float overallGovtBalance;
    public static float govtDebtInterest;
    public static float publicBalance;
    public static float publicDebtInterest;
    public static float overallPublicBalance;

    /**
     * Find investment based on interest rate, IConstant, and mpi. Is the inverse(swap x and y) of the equation below.
     *
     * @param interestRate
     * @return
     */
    private static float investmentEquation(float interestRate) {
        return (float) (Math.sqrt(Math.pow(IConstant * mpi, 2) * (Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate)) / Math.sqrt(2));
    }

    /**
     * Find interest rate based on investment, IConstant, and mpi. Is the inverse(swap x and y) of the equation above.
     *
     * @param investmentRequired
     * @return
     */
    private static float interestRateEquation(float investmentRequired) {
        return (float) ((Math.pow(IConstant, 4) * Math.pow(mpi, 4) - Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(mpi, 2) * Math.pow(investmentRequired, 2)));
    }

    /**
     * Find money supply based on interest rate and long run aggregate supply.
     *
     * @param interestRate
     * @return
     */
    private static float moneySupplyEquation(float interestRate) {
        return -interestRate * longRunAggregateSupply + longRunAggregateSupply;
    }

    private static float debtInterestMultiplierEquation(float total, float balance) {
        return (float) (1 / (2 * total) * (Math.sqrt(Math.pow(balance, 2) + 4) - balance)); // TODO: incorporate overall growth
    }

    static void runCycle() {
        taxMultiplier = -mpc / mps;
        spendingMultiplier = 1 / mps;

        C = CConstant + taxes * taxMultiplier; // overall consumption

        moneySupply = ownedBonds / reserveRequirement; // find money supply based on bonds and reserve requirement
        float interestRate = (longRunAggregateSupply - moneySupply) / longRunAggregateSupply; // find interest rate based on current money supply
        float I = investmentEquation(interestRate); // overall investment

        publicBalance = IConstant - I;
        if (publicBalance < 0) {
            publicDebtInterest = debtInterestMultiplierEquation(IConstant, publicBalance) * interestRate;
            publicBalance *= interestRate;
        }
        overallPublicBalance += publicBalance;
        G = GConstant * spendingMultiplier; // overall government spending

        aggregateDemand = (C + I + G);// / priceLevel; // total gdp
        shortRunAggregateSupply = longRunAggregateSupply;// * priceLevel;
        govtSpendingGap = longRunAggregateSupply - aggregateDemand; // find the output govtSpendingGap

        govtBalance = taxes - GConstant;
        if (govtBalance < 0) {
            govtDebtInterest = debtInterestMultiplierEquation(longRunAggregateSupply, govtBalance) * interestRate;
            govtBalance *= govtDebtInterest;
        }
        overallGovtBalance += govtBalance;
    }

    static void changeReserveRequirements() {
        float investmentRequired = longRunAggregateSupply - C - G; // find how much investment we need
        float interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        float newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        reserveRequirement *= (moneySupply / newMoneySupply); // determine the new reserve requirement based on the new and old money supply
    }

    static void changeMoneySupply() {
        float investmentRequired = longRunAggregateSupply - C - G; // find how much investment we need
        float interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        float newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        float gap = newMoneySupply - moneySupply; // determine how much more money we need
        float bondChange = gap * reserveRequirement; // determine how many more bonds we need to buy or sell
        ownedBonds += bondChange; // add the change in bonds
    }

    static void changeSpending() {
        float spendingChange = govtSpendingGap / spendingMultiplier; // find the change in spending required
        G += spendingChange; // add spending change to government spending
    }

    static void changeTaxes() {
        float taxChange = govtSpendingGap / taxMultiplier; // find the change in taxes required
        if (taxes - taxChange <= 0) {
            System.out.println("can't cut taxes enough");
        } else {
            taxes += taxChange; // add tax change to total taxes
        }
    }
}
