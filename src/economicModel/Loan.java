package economicModel;

public class Loan {
    Float amount;
    Float repaiments;

    public Loan (Float amount, Float repaiments){
        this.amount = amount;
        this.repaiments = repaiments;
    }

    public void repayLoan(Float payment){
        amount-= payment;
        repaiments--;
    }
}
