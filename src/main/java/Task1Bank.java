import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class Account {
    private final int id;
    private int balance;

    public Account(int id, int initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }

    public int getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void withdraw(int amount) {
        balance -= amount;
    }

    public void deposit(int amount) {
        balance += amount;
    }
}

class Bank {
    public void transfer(Account from, Account to, int amount) {
        if (from == to || amount <= 0) {
            return;
        }

        Account firstLock = from.getId() < to.getId() ? from : to;
        Account secondLock = from.getId() < to.getId() ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (from.getBalance() >= amount) {
                    from.withdraw(amount);
                    to.deposit(amount);
                }
            }
        }
    }
}

public class Task1Bank {
    public static void main(String[] args) throws InterruptedException {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int accountsCount = 100;
        int threadsCount = 2000;
        Bank bank = new Bank();
        List<Account> accounts = new ArrayList<>();

        long totalBefore = 0;
        for (int i = 0; i < accountsCount; i++) {
            int initialBalance = ThreadLocalRandom.current().nextInt(1000, 10001);
            accounts.add(new Account(i, initialBalance));
            totalBefore += initialBalance;
        }

        System.out.println("Кількість рахунків: " + accountsCount);
        System.out.println("Кількість потоків переказів: " + threadsCount);
        System.out.println("Загальна сума в банку ДО переказів: " + totalBefore);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            Thread t = new Thread(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                Account from = accounts.get(random.nextInt(accountsCount));
                Account to = accounts.get(random.nextInt(accountsCount));
                int amount = random.nextInt(1, 2000);
                bank.transfer(from, to, amount);
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long totalAfter = 0;
        for (Account acc : accounts) {
            totalAfter += acc.getBalance();
        }

        System.out.println("Загальна сума в банку ПІСЛЯ переказів: " + totalAfter);
        System.out.println("Суми збігаються: " + (totalBefore == totalAfter));
    }
}