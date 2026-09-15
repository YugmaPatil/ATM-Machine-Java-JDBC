package com.bank.atm.main;
 class BankAccount {
	private String accountNumber;
	private String holderName;
	private double balance;
	
	public void setaccountNumber(String accountNumber) {
		this.accountNumber=accountNumber;
		
	}
	
	public String getaccountNumber() {
		return accountNumber;
	}
	
	public void setholderName(String holderName) {
		this.holderName = holderName;
		
	}
	
	public String getholderName() {
		return holderName;
	}
	
	public void setbalance(double balance) {
		this.balance = balance;
	}
	
	public double getbalance() {
		return balance;
	}
	void deposit(double amount){
		if(amount >0) {
			this.balance +=amount;
			System.out.println("Successfully Deposited : "+amount);
		}else {
			System.out.println("Invalid Deposit Amount");
		}
		
	}
	void displayBalance() {
		System.out.println("Account Number : "+accountNumber);
		System.out.println("Account Holder Name : "+holderName);
		System.out.println("Total Balance : "+balance);
		
	}

}

public class SavingsAccount extends BankAccount {
	private double interestRate;
	
	public void setinterestRate(double interestRate) {
		this.interestRate = interestRate;
	}
	
	public double getinterestRate() {
		return interestRate;
	}
	
	void calculateInterest() {
		double currentBalance = getbalance();
		double interestEarned = (currentBalance * interestRate)/100;
		System.out.println("Interest Earned At : "+ interestRate+"% rate :$"+interestEarned);
		deposit(interestEarned);
	}
	
	
	public static void main(String[] args) {
		SavingsAccount bank = new SavingsAccount();
		bank.setaccountNumber("67885424");
		bank.setholderName("Yugma Hari Patil");
		bank.setinterestRate(5.0);
		
		System.out.println(" --------------------  ");
		bank.deposit(6574.0);
		bank.displayBalance();
		System.out.println(" --------------------  ");
		bank.calculateInterest();
		
		
		
		
		
		
	}

	
}
