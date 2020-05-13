package economicModels;

//Note: right now incentive is to keep price level at 1
//TODO: have some kind of unemployment indicator (phillips curve?), right now labour and population are synonymous
//TODO: incorporate crowding out
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
    private double CConstant;
    private double C;
    private double aggregateDemandOutputCurve;
    private double equilibriumOutput;
    private double I; // Should we have public and government investment?

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
    private double averageInflation;
    private double inflation;

    //default constructor
    public ASADModel() {
        cyclesRun = 0;
        averageInflation = inflation = 1;
        growth = overallGrowth = 1;
        priceLevel = 1;
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
        this.CConstant = asadModel.CConstant;
        this.aggregateDemandOutputCurve = asadModel.aggregateDemandOutputCurve;
        this.equilibriumOutput = asadModel.equilibriumOutput;
        this.I = asadModel.I;
        this.C = asadModel.C;
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
        this.averageInflation = asadModel.averageInflation;
    }

    public void runCycle() {
        moneySupply = calculateMoneySupply(); // find money supply based on bonds and reserve requirement

        double interestRate = calculateInterestRateGivenMoneySupply(); // find interest rate based on current money supply
        govtBalance = taxes - GConstant; // find the government balance for this cycle

        overallGovtBalance = calculateBalance(govtBalance, interestRate, overallGovtBalance); // add the current government balance to our overall government balance
        GConstant = calculateSpendingAfterDebt(govtBalance, overallGovtBalance, interestRate, GConstant, 1); // subtract any debt servicing from our government spending if we have to
        G = calculateGovernmentSpending(); // overall government spending

        C = calculateConsumption();
        I = calculateInvestmentGivenInterestRate(interestRate); // overall investment

        //publicBalance = longRunAggregateSupply - I - C - taxes; // find the public balance for this cycle, might still need some adjusting
        publicBalance = IConstant - I; // find the public balance for this cycle, might still need some adjusting

        overallPublicBalance = calculateBalance(publicBalance, interestRate, overallPublicBalance); // add the current public balance to our overall public balance
        I = calculateSpendingAfterDebt(publicBalance, overallPublicBalance, interestRate, I, mpi); // subtract any debt servicing from our public investing if we have to
        C = calculateSpendingAfterDebt(publicBalance, overallPublicBalance,interestRate, C, mpc); // subtract any debt servicing from our public consumption if we have to

        equilibriumOutput = calculateEquilibriumOutput(); // should equal LRAS when price is set to one

        priceLevel = calculatePriceLevel(); // find our equilibrium price level
        aggregateDemandOutputCurve = calculateAggregateDemandOutput(); // this is the aggregate demand curve
        shortRunAggregateSupplyCurve = calculateShortRunAggregateSupply(); // this is the short run aggregate supply curve

        outputGap = calculateOutputGap(); // find the output gap so that our price will be one

        calculateGrowthAndInflation(); // calculate our growth and inflation for this cycle and for the overall session
        previousOutput = equilibriumOutput;
        previousPriceLevel = priceLevel;

        cyclesRun++;
    }

    /**
     * Find investment based on interest rate, IConstant, and average inflation. Is the inverse(swap x and y) of the equation below. \frac{a\sqrt{\sqrt{x^{2}+4}-x}}{\sqrt{2}\cdot b}
     * @param interestRate
     * @return
     */
    private double calculateInvestmentGivenInterestRate(double interestRate) {
        return IConstant * Math.sqrt(Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate) / (Math.sqrt(2) * averageInflation); // might need to modify this
    }

    /**
     * Find interest rate based on investment, IConstant, and average inflation. Is the inverse(swap x and y) of the equation above. \frac{a^{4}-b^{4}\cdot x^{4}}{a^{2}\cdot b^{2}\cdot x^{2}}
     * @param investmentRequired
     * @return
     */
    private double calculateInterestRateGivenInvestment(double investmentRequired) {
        return (Math.pow(IConstant, 4) - Math.pow(averageInflation, 4) * Math.pow(investmentRequired, 4)) / (Math.pow(IConstant, 2) * Math.pow(averageInflation, 2) * Math.pow(investmentRequired, 2));
    }

    /**
     * Find money supply based on interest rate and long run aggregate supply. \frac{1}{2}a\left(\sqrt{x^{2}+4}-x\right)
     * @param interestRate
     * @return
     */
    private double calculateMoneySupplyGivenInterestRate(double interestRate) {
        return 0.5 * longRunAggregateSupply * (Math.sqrt(Math.pow(interestRate, 2) + 4) - interestRate);
    }

    /**
     * Find interest rate based on money supply and long run aggregate supply (is the inverse of the above). \frac{a}{x}-\frac{x}{a}
     * @return
     */
    private double calculateInterestRateGivenMoneySupply() {
        return longRunAggregateSupply / moneySupply - moneySupply / longRunAggregateSupply;
    }

    /**
     * Find the interest rate multiplier based on how fast your economy is growing, how large your debt is, and how large your economy is. \frac{x^{2}}{a^{2}+a^{2}\cdot b}
     * @param currentBalance
     * @return
     */
    private double calculateInterestRateModifier(double currentBalance) {
        return Math.pow(currentBalance, 2) / (Math.pow(longRunAggregateSupply, 2) + Math.pow(longRunAggregateSupply, 2) * overallGrowth);
    }

    /**
     * Calculate C based on the CConstant taxes and taxMultiplier.
     * @return
     */
    private double calculateConsumption() {
        return CConstant + taxes * taxMultiplier;
    }

    /**
     * Calculate various growth and inflation variables for this cycle.
     */
    private void calculateGrowthAndInflation() {
        if (cyclesRun == 0) { // if this is the first cycle, set the variables
            originalOutput = equilibriumOutput;
            originalPriceLevel = priceLevel;
        } else {
            growth = equilibriumOutput / previousOutput; // equilibrium output growth over previous cycle
            overallGrowth = equilibriumOutput / originalOutput; // average equilibrium output growth over all cycles
            inflation = priceLevel / previousPriceLevel; // inflation for this cycle
            overallInflation += inflation; // average inflation for all previous cycles
            averageInflation = overallInflation / cyclesRun;
        }
    }

    /**
     * Calculate sras based on lras and price level
     * @return
     */
    private double calculateShortRunAggregateSupply() {
        return longRunAggregateSupply * priceLevel;
    }

    /**
     * Calculate aggregate demand based on C, I, G, and price level
     * @return
     */
    private double calculateAggregateDemandOutput() {
        //return (CConstant + I) / priceLevel + G + taxes * taxMultiplier; // not sure what should be affected by inflation
        return (C + I + G) / priceLevel;
    }

    /**
     * Calculate the output gap base on equilibrium output and lras.
     * @return
     */
    private double calculateOutputGap() {
        return equilibriumOutput - longRunAggregateSupply;
    }

    /**
     * calculate the overall balance based on current balance and interest rate.
     * @param balance
     * @param interestRate
     * @param overallBalance
     * @return
     */
    private double calculateBalance(double balance, double interestRate, double overallBalance) {
        if (balance < 0 && overallBalance + balance < 0) { // if we have a deficit and have to take on debt
            double debtInterestModifier = calculateInterestRateModifier(overallBalance + balance); // calculate the debt interest modifier based on current output and size of current balance
            double debtInterest = getFinalDebtInterest(interestRate / 100, debtInterestModifier); // calculate the debt interest based on the overall interest rate added to modifier, divided by 2 (or the average if you will)
            overallBalance += ((balance + balance * debtInterest) * priceLevel); // add our current balance to the overall balance, taking into account debt interest and price level
            overallBalance += ((debtRepaymentAmount + debtRepaymentAmount * debtInterest) * priceLevel); // add the debt repayment to the overall balance
        } else { // if we have a surplus or reserves
            overallBalance += (balance * priceLevel); // add balance to overall balance to the overall balance, taking into account price level
        }
        return overallBalance;
    }

    /**
     * Find the final interest rate based on the overall interest rate and the interest rate of this organization.
     * @param interestRate
     * @param debtInterestModifier
     * @return
     */
    private double getFinalDebtInterest(double interestRate, double debtInterestModifier) {
        return (debtInterestModifier + interestRate) / 2;
    }

    /**
     * Calculate the spending by subtracting debt if necessary.
     * @param balance
     * @param interestRate
     * @param spending
     * @param modifier
     * @return
     */
    private double calculateSpendingAfterDebt(double balance, double overallBalance, double interestRate, double spending, double modifier) {
        if (balance < 0 && overallBalance + balance < 0) { // if we have a deficit and have to take on debt, else do nothing
            double debtInterestModifier = calculateInterestRateModifier(balance + overallBalance);// calculate the debt interest modifier based on current output and size of current balance
            double debtInterest = getFinalDebtInterest(interestRate / 100, debtInterestModifier); // calculate the debt interest based on the overall interest rate added to modifier, divided by 2 (or the average if you will)
            double debtRepayment = ((debtRepaymentAmount + debtRepaymentAmount * debtInterest) * modifier);
            System.out.println("Debt Repayment: " + debtRepayment);
            spending -= debtRepayment; // remove the debt repayment amount from the spending
        }
        return spending;
    }

    /**
     * Calculate G from GConstant and spendingMultiplier.
     * @return
     */
    private double calculateGovernmentSpending() {
        return GConstant * spendingMultiplier;
    }

    /**
     * Calculate output by adding C, G, and I.
     * @return
     */
    private double calculateEquilibriumOutput() {
        return C + G + I;
    }

    /**
     * Calculate Price level based on C, G, I, and lras.
     * @return
     */
    private double calculatePriceLevel() {
        //return (Math.sqrt(4 * CConstant * longRunAggregateSupply + Math.pow(G, 2) + 2 * G * taxes * taxMultiplier + 4 * longRunAggregateSupply * I + Math.pow(taxes, 2) * Math.pow(taxMultiplier, 2)) + G + taxes * taxMultiplier) / (2 * longRunAggregateSupply);
        return Math.sqrt(C + G + I) / Math.sqrt(longRunAggregateSupply);
    }

    /**
     * Calculate the current money supply based on bonds and reserveRequirements.
     * @return
     */
    private double calculateMoneySupply() {
        return ownedBonds / reserveRequirement;
    }

    /**
     * Calculate the spending change required to close the output gap.
     * @return
     */
    public double calculateSpendingChange() {
        return outputGap / -spendingMultiplier; // find the change in spending required
    }

    /**
     * Calculate the tax change required to close the output gap.
     * @return
     */
    public double calculateTaxChange() {
        return outputGap / -taxMultiplier; // find the change in taxes required
    }

    /**
     * Calculate the reserve multiplier required based on investment required.
     * @param investmentRequired
     * @return
     */
    public double calculateReserveMultiplier(double investmentRequired) {
        double interestRate = calculateInterestRateGivenInvestment(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = calculateMoneySupplyGivenInterestRate(interestRate); // find the money supply we need based on the new interest rate
        return moneySupply / newMoneySupply;
    }

    /**
     * Calculate the bond change required based on investment required.
     * @param investmentRequired
     * @return
     */
    public double calculateBondChange(double investmentRequired) {
        double interestRate = calculateInterestRateGivenInvestment(investmentRequired); // find the new interest rate based on the investment we need.
        double newMoneySupply = calculateMoneySupplyGivenInterestRate(interestRate); // find the money supply we need based on the new interest rate
        double gap = newMoneySupply - moneySupply; // determine how much more money we need
        return gap * reserveRequirement; // determine how many more bonds we need to buy or sell
    }

    /**
     * Find the investment required by subtracting C and G from lras.
     * @return
     */
    public double calculateInvestmentRequired() {
        return longRunAggregateSupply - C - G;
    }

    /**
     * Multiply current reserve requirement by the reserve multiplier.
     * @param reserveMultiplier
     */
    public void changeReserveRequirements(double reserveMultiplier) {
        if (reserveMultiplier == 0) {
            System.out.println("reserve multiplier can't be zero");
        } else {
            System.out.println("Reserve Requirement changed by " + reserveMultiplier + '\n');
            reserveRequirement *= reserveMultiplier; // determine the new reserve requirement based on the new and old money supply
        }
    }

    /**
     * Add the bond change to the current owned bonds.
     * @param bondChange
     */
    public void changeMoneySupply(double bondChange) {
        if (ownedBonds + bondChange <= 0) {
            System.out.println("can't sell enough bonds");
        } else {
            System.out.println("Bonds owned changed by " + bondChange + '\n');
            ownedBonds += bondChange; // add the change in bonds
        }
    }

    /**
     * Add the spending change to the current spending.
     * @param spendingChange
     */
    public void changeSpending(double spendingChange) {
        if (GConstant + spendingChange <= 0) {
            System.out.println("can't cut spending enough");
        } else {
            System.out.println("Spending changed by " + spendingChange + '\n');
            GConstant += spendingChange; // add spending change to government spending
        }
    }

    /**
     * Add the tax change to the current taxes.
     * @param taxChange
     */
    public void changeTaxes(double taxChange) {
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

    public void setLongRunAggregateSupply(double output) {
        longRunAggregateSupply = output;
    }

    public void setCConstant(double v) {
        CConstant = v;
    }

    public void setIConstant(double v) {
        IConstant = v;
    }

    public int getCyclesRun() {
        return cyclesRun;
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

    public double getPriceLevel() {
        return priceLevel;
    }

    public double getMoneySupply() {
        return moneySupply;
    }

    public double getAverageInflation() {
        return averageInflation;
    }
}
