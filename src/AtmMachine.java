import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class AtmMachine {
    private static final String URL = "jdbc:mysql://localhost:3306/atm_db";
    private static final String USER = "root";
    private static final String PASSWORD = "pass123";
    
    public static void main(String[] args) {
        initializeDatabase();
        Scanner sc = new Scanner(System.in);
        System.out.println("---- Account Setup ----");

        System.out.println("Set Password : ");
        String password = sc.next();

        System.out.println("Set Pin : ");
        String pin = sc.next();

        double balance = 60000;
        int accountId = -1;
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO atm_users (password, pin, balance) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, password);
            ps.setString(2, pin);
            ps.setDouble(3, balance);
            ps.executeUpdate();
                
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    accountId = generatedKeys.getInt(1);
                    System.out.println("Account created successfully! Your Account ID is: " + accountId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
            return;
        }

        System.out.println("\nWelcome to ATM Login Page ");
        System.out.println("Enter Password : ");
        String enteredPassword = sc.next();

        System.out.println("Enter Pin : ");
        String enteredPin = sc.next();

        if (enteredPin.equals(pin) && enteredPassword.equals(password)) {
            System.out.println("Login Successful");

            int choice = 0;

            do {
                System.out.println("\n-----ATM MENU-----");
                System.out.println("1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Password Check");
                System.out.println("5. PIN Change");
                System.out.println("6. Exit");
                System.out.println("7.View Mini-statement");

                System.out.println("Enter your choice : ");
                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        // Fetch the latest balance directly from the DB
                        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement ps = con.prepareStatement("SELECT balance FROM atm_users WHERE account_id = ?")) {
                            ps.setInt(1, accountId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    System.out.println("Current Balance : " + rs.getDouble("balance"));
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error fetching balance.");
                        }
                        break;

                    case 2:
                        System.out.println("Deposit Amount");
                        double deposit = sc.nextDouble();

                        if (deposit > 0) {
                            String updateQuery = "UPDATE atm_users SET balance = balance + ? WHERE account_id = ?";
                            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                                 PreparedStatement ps = con.prepareStatement(updateQuery)) {
                                
                                ps.setDouble(1, deposit);
                                ps.setInt(2, accountId);
                                ps.executeUpdate();
                                
                                System.out.println("Amount deposited successfully!!");
                                
                                logTransaction(accountId, "DEPOSIT",deposit);
                                
                            } catch (Exception e) {
                                System.out.println("Transaction failed: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Invalid Amount");
                        }
                        break;

                    case 3:
                        System.out.println("Withdraw Amount");
                        double withdraw = sc.nextDouble();

                        double currentBalance = 0;
                        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement ps = con.prepareStatement("SELECT balance FROM atm_users WHERE account_id = ?")) {
                            ps.setInt(1, accountId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    currentBalance = rs.getDouble("balance");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error reading balance.");
                        }
                        
                        if (withdraw > currentBalance) {
                            System.out.println("Insufficient Amount");
                        } else if (withdraw <= 0) {
                            System.out.println("Invalid Amount");
                        } else {
                            String withdrawQuery = "UPDATE atm_users SET balance = balance - ? WHERE account_id = ?";
                            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                                 PreparedStatement ps = con.prepareStatement(withdrawQuery)) {
                                ps.setDouble(1, withdraw);
                                ps.setInt(2, accountId);
                                ps.executeUpdate();
                                System.out.println("Please collect Your Cash.");
                                
                                logTransaction(accountId, "WITHDRAW",withdraw);
                                
                            } catch (Exception e) {
                                System.out.println("Transaction failed");
                            }
                        }
                        break;

                    case 4:
                        System.out.println("Check Password");
                        String checkPassword = sc.next();

                        // Fetch the actual current password from the DB
                        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement ps = con.prepareStatement("SELECT password FROM atm_users WHERE account_id = ?")) {
                            ps.setInt(1, accountId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && checkPassword.equals(rs.getString("password"))) {
                                    System.out.println("Password IS Correct");
                                } else {
                                    System.out.println("Wrong Password");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error validating password.");
                        }
                        break;

                    case 5:
                        System.out.println("Enter current PIN");
                        String currentPin = sc.next();

                        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement ps = con.prepareStatement("SELECT pin FROM atm_users WHERE account_id = ?")) {
                            ps.setInt(1, accountId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && rs.getString("pin").equals(currentPin)){
                                    System.out.println("Enter new PIN :");
                                    String newPin = sc.next();
                                    
                                    // Fixed the missing SET statement and parameters here
                                    try (PreparedStatement updatePs = con.prepareStatement("UPDATE atm_users SET pin = ? WHERE account_id = ?")) {
                                        updatePs.setString(1, newPin);
                                        updatePs.setInt(2, accountId);
                                        updatePs.executeUpdate();
                                        System.out.println("PIN Changed Successfully");
                                        
                                        pin =newPin;
                                    }
                                } else {
                                    System.out.println("Incorrect PIN");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error processing PIN change: " + e.getMessage());
                        }
                        break;

                    case 6:
                        System.out.println("Thanks For Using The ATM");
                        break;
                        
                    case 7:
                        System.out.println("\n---- MINI-STATEMENT ----");
                        String selectLogs = "SELECT transaction_type, amount, transaction_time FROM atm_transactions WHERE account_id = ? ORDER BY transaction_time DESC LIMIT 5";
                        
                        // Fixed: Properly passing selectLogs into con.prepareStatement()
                        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement ps = con.prepareStatement(selectLogs)) {
                            
                            ps.setInt(1, accountId);
                            try (ResultSet rs = ps.executeQuery()) {
                                boolean hasRecords = false;
                                while (rs.next()) {
                                    hasRecords = true;
                                    String type = rs.getString("transaction_type");
                                    double amt = rs.getDouble("amount");
                                    java.sql.Timestamp time = rs.getTimestamp("transaction_time");
                                    System.out.println("[" + time + "] " + type + ": Rs. " + amt);
                                }
                                if (!hasRecords) {
                                    System.out.println("No recent transactions found.");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error fetching mini-statement: " + e.getMessage());
                        }
                        break;
                       
                    default:
                        System.out.println("Invalid Choice");
                }

            } while (choice !=6 );

        } else {
            System.out.println("Invalid Credentials");
        }

        sc.close();
   
    }

	
	
	private static void logTransaction(int accountId, String type, double amount) {
		String sql ="INSERT INTO atm_transactions(account_id, transaction_type,amount)VALUES(?,?,?)";
		
		try(Connection con = DriverManager.getConnection(URL,USER,PASSWORD);
				PreparedStatement ps = con.prepareStatement(sql)){
			
			ps.setInt(1, accountId);
			ps.setString(2, type);
			ps.setDouble(3, amount);
			ps.executeUpdate();
			
			
		}catch(Exception e) {
			
			System.out.println(" Failed to log transaction history : "+e.getMessage());
			
		}
	}
	private static void initializeDatabase() {
	    // 1. Users Table
	    String userTableQuery = "CREATE TABLE IF NOT EXISTS atm_users (" +
	                            "account_id INT AUTO_INCREMENT PRIMARY KEY, " +
	                            "password VARCHAR(255) NOT NULL, " +
	                            "pin VARCHAR(10) NOT NULL, " +
	                            "balance DOUBLE NOT NULL)";
	    
	    // 2. Transactions Table - Structured neatly to satisfy strict SQL parser rules
	    String transactionTableQuery = "CREATE TABLE IF NOT EXISTS atm_transactions (" +
	                                   "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
	                                   "account_id INT NOT NULL, " +
	                                   "transaction_type VARCHAR(20) NOT NULL, " + 
	                                   "amount DOUBLE NOT NULL, " +
	                                   "transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + 
	                                   "CONSTRAINT fk_user_transaction FOREIGN KEY (account_id) REFERENCES atm_users(account_id))";
	    
	    try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
	         Statement stmt = con.createStatement()) {
	        
	        // Execute both table creations sequentially
	        stmt.execute(userTableQuery);
	        stmt.execute(transactionTableQuery);
	        
	    } catch (Exception e) {
	        System.out.println("Database initialization failed: " + e.getMessage());
	    }
	}
}