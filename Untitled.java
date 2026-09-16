  public void run() {
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();

            if (command.equals("exit")) {
                return;
            }

            try {
                handle(command);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }