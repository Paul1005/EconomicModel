package economicModel;

public class ASADModel {
    public static float longRungAggregateSupply;
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
    public static float gap;
    public static float C;
    public static float aggregateDemand;

    private static float investmentEquation(float interestRate) {
        return (float) (Math.sqrt(Math.pow(IConstant * mpi, 2) * (Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate)) / Math.sqrt(2));
    }

    /*private static float consumptionEquation(float priceLevel) {
        return CConstant / priceLevel;
    }*/

    public static void runCycle() {
        //float priceLevel;
        C = CConstant + taxes * (-mpc / mps);//consumptionEquation(priceLevel);
        //double I = -(interestRate - Math.sqrt(Math.pow(interestRate, 2) - 4 * IConstant)) / 2;
        //interestRate = Math.pow(I, 2) + 1/-I;
        moneySupply = ownedBonds / reserveRequirement;
        System.out.println("Money supply: " + ASADModel.moneySupply);
        float interestRate = (longRungAggregateSupply - moneySupply) / longRungAggregateSupply;
        System.out.println("Current Interest Rate: " + interestRate);
        float I = investmentEquation(interestRate);
        System.out.println("Current Investment: " + I);
        G = GConstant * (1 / (mps));
        aggregateDemand = C + I + G;

        //float SRAS = priceLevel;

        gap = longRungAggregateSupply - aggregateDemand;
    }

    public static void changeReserveRequirements() {
        float investmentRequired = longRungAggregateSupply - C - G;
        float interestRate = (float) ((Math.pow(IConstant, 4) * Math.pow(mpi, 4) - Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(mpi, 2) * Math.pow(investmentRequired, 2)));
        float newMoneySupply = -interestRate * longRungAggregateSupply + longRungAggregateSupply;
        reserveRequirement *= (moneySupply / newMoneySupply);
    }

    public static void changeMoneySupply() {
        float investmentRequired = longRungAggregateSupply - C - G;
        System.out.println("Investment required: " + investmentRequired);
        float interestRate = (float) ((Math.pow(IConstant, 4) * Math.pow(mpi, 4) - Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(mpi, 2) * Math.pow(investmentRequired, 2)));
        System.out.println("New Interest Rate: " + interestRate);
        float newMoneySupply = -interestRate * longRungAggregateSupply + longRungAggregateSupply;
        System.out.println("New Money supply: " + newMoneySupply);
        float gap = newMoneySupply - moneySupply;
        float bondChange = gap * reserveRequirement;
        ownedBonds += bondChange;
        System.out.println("New owned bonds: " + ownedBonds);
    }

    public static void changeSpending() {
        float spendingMultiplier = 1 / (mps);
        float spendingChange = gap / spendingMultiplier;
        G += spendingChange;
    }

    public static void changeTaxes() {
        float taxMultiplier = -mpc / mps;
        float taxChange = gap / taxMultiplier;
        taxes += taxChange;
    }
}
