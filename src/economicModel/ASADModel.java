package economicModel;

public class ASADModel {
    public static float longRungAggregateSupply;
    public static float taxes;
    public static float mpc;
    public static float mps;
    public static float reserveRequirement;
    public static float moneySupply;
    public static float CConstant;
    public static float IConstant;
    public static float G;
    public static float gap;
    public static float C;
    public static float aggregateDemand;

    private static float investmentEquation(float interestRate) {
        return (float) (Math.pow((Math.sqrt(3) * Math.sqrt(27 * Math.pow(IConstant, 2) + 4 * Math.pow(interestRate, 3)) + 9 * IConstant), 1f / 3f) / (Math.pow(2, 1f / 3f) * Math.pow(3, 2f / 3f)) - (Math.pow(2f / 3f, 1f / 3f) * interestRate) / Math.pow((Math.sqrt(3) * Math.sqrt(27 * Math.pow(IConstant, 2) + 4 * Math.pow(interestRate, 3)) + 9 * IConstant), 1f / 3f));
    }

    private static float consumptionEquation(float priceLevel) {
        return CConstant / priceLevel;
    }

    public static void runCycle() {
        //float priceLevel;

        C = CConstant + taxes * (-mpc / mps);//consumptionEquation(priceLevel);
        //double I = -(interestRate - Math.sqrt(Math.pow(interestRate, 2) - 4 * IConstant)) / 2;
        //interestRate = Math.pow(I, 2) + 1/-I;
        float interestRate = longRungAggregateSupply/moneySupply;
        float I = investmentEquation(interestRate);

        aggregateDemand = C + I + G;

        //float SRAS = priceLevel;

        gap = longRungAggregateSupply - aggregateDemand;
    }

    public static void changeMoneySupply() {
        float interestRate;
        float investmentRequired = longRungAggregateSupply - C - G;
        interestRate = (float) (Math.pow(investmentRequired, 2) + 1/-investmentRequired);
        moneySupply = interestRate/longRungAggregateSupply;
    }

    public static void changeSpending() {
        float spendingMultiplier = 1 / (1 - mpc);
        float spendingChange = gap / spendingMultiplier;
        G += spendingChange;
    }

    public static void changeTaxes() {
        float taxMultiplier = -mpc / mps;
        float taxChange = gap / taxMultiplier;
        taxes += taxChange;
    }
}
