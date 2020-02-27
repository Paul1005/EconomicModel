package economicModel;

public class ASADModel {
    public static float longRungAggregateSupply = 400;
    public static float taxes;
    public static float spendiing;
    public static float taxMultiplier;
    public static float spendingMultiplier;
    public static float mpc;
    public static float mps;
    public static float reserveRequirement;
    public static float moneySupply;

    public static void runCycle() {
        float ADm = -1;
        float ADb = 2000;

        float SRASm = 1;
        float SRASb = 0;

        float C = 100;
        float I = 100;
        float G = 100;
        float output = C + I + G;
        float output = (ADb * SRASm - SRASb * ADm) / (SRASm - ADm);
        //float priceLevel = (SRASb - ADb) / (ADm - SRASm);

        float gap = longRungAggregateSupply - output;

        spendingMultiplier = 1 / (1 - mpc);
        taxMultiplier = -mpc/mps;

        float taxChange = gap/taxMultiplier;

        float spendingChange = gap/spendingMultiplier;



        float MDm = -1;
        float MDb = 2000;
        moneySupply = 1000;
        float interestRate = MDm * moneySupply + MDb;
        reserveRequirement = 0.125f;

        float LMm = ;
        float LMb = ;
        float ISm = ;
        float ISb = ;

        float targetInterestRate = ;

        float supplyChange = gap;
    }
}
