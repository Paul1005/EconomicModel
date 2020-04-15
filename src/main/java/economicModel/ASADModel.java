package economicModel;

//Note: right now incentive is to keep price level at 1
//TODO: have some kind of unemployment indicator (phillips curve?), right now labour and population are synonymous
//TODO: incorporate crowding out
//TODO: go over how C and I are calculated
public class ASADModel {
    private double longRunAggregateSupply;
    private double shortRunAggregateSupplyCurve;
    private double taxes;
    private double mpc;
    private double mpi;
    private double mps;
    private double reserveRequirement;
    private double ownedBonds;
    private double moneySupply;
    private double GConstant;
    private double IConstant;
    private double G;
    private double outputGap;
    private double C; // Should maybe be affected by inflation
    private double aggregateDemandOutputCurve;
    private double equilibriumOutput;
    private double I;

    private double taxMultiplier;
    private double spendingMultiplier;

    private double govtBalance;
    private double publicBalance;
    private double overallGovtBalance;
    private double overallPublicBalance;

    //public  int debtCycles; // number of cycles we use to pay of debt
    private double debtRepaymentAmount; // min debt repayment required

    private double growth;
    private double overallGrowth;
    private int cyclesRun;
    private double originalOutput = 0;
    private double previousOutput = 0;

    private double priceLevel;
    private double previousPriceLevel;
    private double originalPriceLevel;
    private double overallInflation;
    private double inflation;

    //default constructor
    public ASADModel() {

    }

    //copy constructor
    public ASADModel(ASADModel asadModel) {
        this.longRunAggregateSupply = asadModel.longRunAggregateSupply;
        this.shortRunAggregateSupplyCurve = asadModel.shortRunAggregateSupplyCurve;
        this.taxes = asadModel.taxes;
        this.mpc = asadModel.mpc;
        this.mpi = asadModel.mpi;
        this.mps = asadModel.mps;
        this.reserveRequirement = asadModel.reserveRequirement;
        this.ownedBonds = asadModel.ownedBonds;
        this.moneySupply = asadModel.moneySupply;
        this.GConstant = asadModel.GConstant;
        this.IConstant = asadModel.IConstant;
        this.G = asadModel.G;
        this.outputGap = asadModel.outputGap;
        this.C = asadModel.C;
        this.aggregateDemandOutputCurve = asadModel.aggregateDemandOutputCurve;
        this.equilibriumOutput = asadModel.equilibriumOutput;
        this.I = asadModel.I;
        this.taxMultiplier = asadModel.taxMultiplier;
        this.spendingMultiplier = asadModel.spendingMultiplier;
        this.govtBalance = asadModel.govtBalance;
        this.publicBalance = asadModel.publicBalance;
        this.overallGovtBalance = asadModel.overallGovtBalance;
        this.overallPublicBalance = asadModel.overallPublicBalance;
        this.debtRepaymentAmount = asadModel.debtRepaymentAmount;
        this.growth = asadModel.growth;
        this.overallGrowth = asadModel.overallGrowth;
        this.cyclesRun = asadModel.cyclesRun;
        this.originalOutput = asadModel.originalOutput;
        this.previousOutput = asadModel.previousOutput;
        this.priceLevel = asadModel.priceLevel;
        this.previousPriceLevel = asadModel.previousPriceLevel;
        this.originalPriceLevel = asadModel.originalPriceLevel;
        this.overallInflation = asadModel.overallInflation;
        this.inflation = asadModel.inflation;
    }

    /**
     * Find investment based on interest rate, IConstant, and mpi. Is the inverse(swap x and y) of the equation below. \frac{a\sqrt{\sqrt{x^{2}+4}-x}}{\sqrt{2}\cdot b}
     *
     * @param interestRate
     * @return
     */
    private double investmentEquation(double interestRate) {
        return IConstant * Math.sqrt(Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate) / (Math.sqrt(2) * overallInflation);
    }

    /**
     * Find interest rate based on investment, IConstant, and mpi. Is the inverse(swap x and y) of the equation above. \frac{a^{4}-b^{4}\cdot x^{4}}{a^{2}\cdot b^{2}\cdot x^{2}}
     *
     * @param investmentRequired
     * @return
     */
    private double interestRateEquation(double investmentRequired) {
        return (Math.pow(IConstant, 4) - Math.pow(overallInflation, 4) * Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(overallInflation, 2) * Math.pow(investmentRequired, 2));
    }

    /**
     * Find money supply based on interest rate and long run aggregate supply.
     *
     * @param interestRate
     * @return
     */
    private double moneySupplyEquation(double interestRate) {
        return -interestRate * longRunAggregateSupply + longRunAggregateSupply;
    }

    /**
     * Find the interest rate multiplier based on how fast your economy is growing, how large your debt is, and how large your economy is. \frac{-x}{a+a\cdot b}
     *
     * @param currentBalance
     * @return
     */
    private double calculateDebtInterestModifier(double currentBalance) {
        return currentBalance / (longRunAggregateSupply + longRunAggregateSupply * overallGrowth);
    }

    void runCycle() {
        moneySupply = getMoneySupply(); // find money supply based on bonds and reserve requirement

        double interestRate = getInterestRate(); // find interest rate based on current money supply
        govtBalance = taxes - GConstant; // find the government balance for this cycle

        overallGovtBalance = calculateBalance(govtBalance, interestRate, overallGovtBalance); // add the current government balance to our overall government balance
        GConstant = calculateSpendingAfterDebt(govtBalance, interestRate, GConstant); // subtract any debt servicing from our government spending if we have to

        G = getGovernmentSpending(); // overall government spending

        I = investmentEquation(interestRate); // overall investment

        equilibriumOutput = calculateEquilibriumOutput(); // should equal LRAS when price is set to one

        priceLevel = getPriceLevel(); // find our equilibrium price level
        aggregateDemandOutputCurve = getAggregateDemandOutput(); // this is the aggregate demand curve
        shortRunAggregateSupplyCurve = getShortRunAggregateSupply(); // this is the short run aggregate supply curve

        outputGap = calculateOutputGap(); // find the output gap so that our price will be one

        publicBalance = IConstant - I; // find the public balance for this cycle

        overallPublicBalance = calculateBalance(publicBalance, interestRate, overallPublicBalance); // add the current public balance to our overall public balance
        I = calculateSpendingAfterDebt(publicBalance, interestRate, I); // subtract any debt servicing from our public investing if we have to

        calculateGrowthAndInflation(); // calculate our growth and inflation for this cycle and for the overall session
        previousOutput = equilibriumOutput;
        previousPriceLevel = priceLevel;

        cyclesRun++;
    }

    public void calculateGrowthAndInflation() {
        if (cyclesRun == 0) { // if this is the first cycle, set the variables
            originalOutput = equilibriumOutput;
            originalPriceLevel = priceLevel;
        } else {
            growth = equilibriumOutput / previousOutput; // equilibrium output growth over previous cycle
            overallGrowth = equilibriumOutput / originalOutput; // average equilibrium output growth over all cycles
            inflation = priceLevel / previousPriceLevel; // inflation for this cycle
            overallInflation = priceLevel / originalPriceLevel; // average inflation for all previous cycles
        }
    }

    public double getShortRunAggregateSupply() {
        return longRunAggregateSupply * priceLevel;
    }

    public double getAggregateDemandOutput() {
        return (C + I) / priceLevel + G + taxes * taxMultiplier;
    }

    public double calculateOutputGap() {
        return equilibriumOutput - longRunAggregateSupply;
    }

    public double getInterestRate() {
        return (longRunAggregateSupply - moneySupply) / longRunAggregateSupply;
    }

    private double calculateBalance(double balance, double interestRate, double overallBalance) {
        if (balance < 0) { // if we have a deficit
            double debtInterestModifier = calculateDebtInterestModifier(balance); // calculate the debt interest modifier based on current output and size of current balance
            double debtInterest = (debtInterestModifier + interestRate) / 2; // calculate the debt interest based on the overall interest rate added to modifier, divided by 2 (or the average if you will)
            overallBalance += (balance + balance * debtInterest) * priceLevel; // add our current balance to the overall balance, taking into account debt interest and price level

            overallBalance += ((debtRepaymentAmount + debtRepaymentAmount * debtInterest) * priceLevel); // add the debt repayment to the overall balance
        } else if (balance > 0) { // if we have a surplus
            overallBalance += (balance * priceLevel); // add balance to overall balance to the overall balance, taking into account price level
        }
        return overallBalance;
    }

    private double calculateSpendingAfterDebt(double balance, double interestRate, double spending) {
        if (balance < 0) { // if we have a deficit, else do nothing
            double debtInterestModifier = calculateDebtInterestModifier(balance);// calculate the debt interest modifier based on current output and size of current balance
            double debtInterest = (debtInterestModifier + interestRate) / 2; // calculate the debt interest based on the overall interest rate added to modifier, divided by 2 (or the average if you will)
            spending -= (debtRepaymentAmount + debtRepaymentAmount * debtInterest); // remove the debt repayment amount from the spending
        }
        return spending;
    }

    public double getGovernmentSpending() {
        return GConstant * spendingMultiplier;
    }

    public double calculateEquilibriumOutput() {
        return C + taxes * taxMultiplier + G + I;
    }

    public double getPriceLevel() {
        return (Math.sqrt(4 * C * longRunAggregateSupply + Math.pow(G, 2) + 2 * G * taxes * taxMultiplier + 4 * longRunAggregateSupply * I + Math.pow(taxes, 2) * Math.pow(taxMultiplier, 2)) + G + taxes * taxMultiplier) / (2 * longRunAggregateSupply);
    }

    public double getMoneySupply() {
        return ownedBonds / reserveRequirement;
    }


    double calculateReserveMultiplier(double investmentRequired) {
        double interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        return moneySupply / newMoneySupply;
    }

    void changeReserveRequirements(double reserveMultiplier) {
        System.out.println("Reserve Requirement changed by " + reserveMultiplier + '\n');
        reserveRequirement *= reserveMultiplier; // determine the new reserve requirement based on the new and old money supply
    }

    double calculateBondChange(double investmentRequired) {
        double interestRate = interestRateEquation(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = moneySupplyEquation(interestRate); // find the money supply we need based on the new interest rate
        double gap = newMoneySupply - moneySupply; // determine how much more money we need
        return gap * reserveRequirement; // determine how many more bonds we need to buy or sell
    }

    public double getInvestmentRequired() {
        return longRunAggregateSupply - C - G - taxes * taxMultiplier;
    }

    void changeMoneySupply(double bondChange) {
        System.out.println("Bonds owned changed by " + bondChange + '\n');
        ownedBonds += bondChange; // add the change in bonds
    }

    double calculateSpendingChange() {
        return outputGap / -spendingMultiplier; // find the change in spending required
    }

    void changeSpending(double spendingChange) {
        System.out.println("Spending changed by " + spendingChange + '\n');
        GConstant += spendingChange; // add spending change to government spending
    }

    double calculateTaxChange() {
        return outputGap / -taxMultiplier; // find the change in taxes required
    }

    void changeTaxes(double taxChange) {
        if (taxes + taxChange <= 0) {
            System.out.println("can't cut taxes enough");
        } else {
            System.out.println("Taxes changed by " + taxChange + '\n');
            taxes += taxChange; // add tax change to total taxes
        }
    }

    public void setDebtRepaymentAmount(double i) {
        debtRepaymentAmount = i;
    }

    public void setCyclesRun(int i) {
        cyclesRun = i;
    }

    public void setOverallGrowth(double i) {
        overallGrowth = i;
    }

    public void setOverallInflation(double i) {
        overallInflation = i;
    }

    public void setOwnedBonds(double i) {
        ownedBonds = i;
    }

    public void setReserveRequirement(double v) {
        reserveRequirement = v;
    }

    public void setTaxes(double i) {
        taxes = i;
    }

    public void setGConstant(double i) {
        GConstant = i;
    }

    public void setmpc(double v) {
        mpc = v;
    }

    public void setmpi(double v) {
        mpi = v;
    }

    public void setmps(double v) {
        mps = v;
    }

    public void setTaxMultiplier(double v) {
        taxMultiplier = v;
    }

    public void setSpendingMultiplier(double v) {
        spendingMultiplier = v;
    }

    public int getCyclesRun() {
        return cyclesRun;
    }

    public void setLongRunAggregateSupply(double output) {
        longRunAggregateSupply = output;
    }

    public double getmpc() {
        return mpc;
    }

    public double getmpi() {
        return mpi;
    }

    public double getmps() {
        return mps;
    }

    public double getLongRunAggregateSupply() {
        return longRunAggregateSupply;
    }

    public void setC(double v) {
        C = v;
    }

    public void setIConstant(double v) {
        IConstant = v;
    }

    public double getTaxes() {
        return taxes;
    }

    public double getG() {
        return G;
    }

    public double getReserveRequirement() {
        return reserveRequirement;
    }

    public double getI() {
        return I;
    }

    public double getC() {
        return C;
    }

    public double getAggregateDemandOutputCurve() {
        return aggregateDemandOutputCurve;
    }

    public double getShortRunAggregateSupplyCurve() {
        return shortRunAggregateSupplyCurve;
    }

    public double getInflation() {
        return inflation;
    }

    public double getOverallInflation() {
        return overallInflation;
    }

    public double getGovtBalance() {
        return govtBalance;
    }

    public double getPublicBalance() {
        return publicBalance;
    }

    public double getOverallGovtBalance() {
        return overallGovtBalance;
    }

    public double getOverallPublicBalance() {
        return overallPublicBalance;
    }

    public double getGrowth() {
        return growth;
    }

    public double getOverallGrowth() {
        return overallGrowth;
    }

    public double getTaxMultiplier() {
        return taxMultiplier;
    }

    public double getSpendingMultiplier() {
        return spendingMultiplier;
    }

    public double getOwnedBonds() {
        return ownedBonds;
    }

    public double getOutputGap() {
        return outputGap;
    }

    public double getEquilibriumOutput() {
        return equilibriumOutput;
    }
}
