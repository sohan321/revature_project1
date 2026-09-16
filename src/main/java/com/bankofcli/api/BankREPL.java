package com.bankofcli.api;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.exception.BankingException;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class BankREPL {

    private static final Logger logger = LoggerFactory.getLogger(BankREPL.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final Scanner scanner = new Scanner(System.in);

    private Account currentAccount;

    public BankREPL(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public void run() {
        System.out.println("Welcome to Bank of CLI");

        while (true) {
            printMenu();
            System.out.print("> ");
            String command = scanner.nextLine().trim();
            System.out.println();

            if (command.toLowerCase().equals("exit")) {
                System.out.println("Goodbye!");
                return;
            }

            try {
                handle(command);
            } catch (BankingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Unexpected error handling command '{}'", command, e);
                System.out.println("Service unavailable. Please try again later.");
            }
        }
    }

    private void printMenu() {
        if (currentAccount == null) {
            System.out.println("""

                    1) Register
                    2) Login
                    Exit""");
            System.out.println();
        } else {
            System.out.println("""

                    1) Check balance
                    2) Deposit
                    3) Withdraw
                    4) Transfer
                    5) Transaction history
                    6) Logout
                    Exit""");
            System.out.println();
        }
    }

    private void handle(String command) {
        if (currentAccount == null) {
            switch (command) {
                case "1" -> register();
                case "2" -> login();
                default -> System.out.println("Invalid option.");
            }
        } else {
            switch (command) {
                case "1" -> checkBalance();
                case "2" -> deposit();
                case "3" -> withdraw();
                case "4" -> transfer();
                case "5" -> viewHistory();
                case "6" -> logout();
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void register() {
        System.out.print("Choose a 4-digit PIN: ");
        String pin = scanner.nextLine().trim();

        Account account = accountService.register(pin);
        System.out.println("Account created! Your account ID is " + account.getAccountId());
    }

    private void login() {
        Long accountId = readAccountId("Account ID: ");
        if (accountId == null) {
            return;
        }
        System.out.print("PIN: ");
        String pin = scanner.nextLine().trim();

        currentAccount = accountService.login(accountId, pin);
        System.out.println("Login successful. Welcome, account " + currentAccount.getAccountId() + "!");
    }

    private void logout() {
        System.out.println("Logged out account " + currentAccount.getAccountId() + ".");
        currentAccount = null;
    }

    private void checkBalance() {
        BigDecimal balance = accountService.getBalance(currentAccount.getAccountId());
        System.out.println("Current balance: " + balance);
    }

    private void deposit() {
        BigDecimal amount = readAmount("Amount to deposit: ");
        if (amount == null) {
            return;
        }

        transactionService.deposit(currentAccount.getAccountId(), amount);
        System.out.println("Deposit successful.");
    }

    private void withdraw() {
        BigDecimal amount = readAmount("Amount to withdraw: ");
        if (amount == null) {
            return;
        }

        transactionService.withdraw(currentAccount.getAccountId(), amount);
        System.out.println("Withdrawal successful.");
    }

    private void transfer() {
        Long toAccountId = readAccountId("Recipient account ID: ");
        if (toAccountId == null) {
            return;
        }
        BigDecimal amount = readAmount("Amount to transfer: ");
        if (amount == null) {
            return;
        }

        transactionService.transfer(currentAccount.getAccountId(), toAccountId, amount);
        System.out.println("Transfer successful.");
    }

    private void viewHistory() {
        List<Transaction> history = transactionService.getHistory(currentAccount.getAccountId());
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction transaction : history) {
            System.out.printf("[%s] %s %s (related account: %s)%n",
                    TIMESTAMP_FORMAT.format(transaction.getTimestamp()),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getRelatedAccountId());
        }
    }

    private Long readAccountId(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid account ID.");
            return null;
        }
    }

    private BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return null;
        }
    }
}
