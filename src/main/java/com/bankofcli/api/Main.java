package com.bankofcli.api;

import com.bankofcli.persistence.AccountDAO;
import com.bankofcli.persistence.AccountDAOImpl;
import com.bankofcli.persistence.ConnectionFactory;
import com.bankofcli.persistence.TransactionDAO;
import com.bankofcli.persistence.TransactionDAOImpl;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.AccountServiceImpl;
import com.bankofcli.service.TransactionService;
import com.bankofcli.service.TransactionServiceImpl;

public class Main {

    public static void main(String[] args) {
        AccountDAO accountDAO = new AccountDAOImpl();
        TransactionDAO transactionDAO = new TransactionDAOImpl();

        AccountService accountService = new AccountServiceImpl(accountDAO);
        TransactionService transactionService = new TransactionServiceImpl(
                accountDAO, transactionDAO, ConnectionFactory.getConnectionFactory());

        new BankREPL(accountService, transactionService).run();
    }
}
