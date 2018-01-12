package sourcechecker;

public class App {

    public static void main(String[] args) {
        Contact contact = new Contact(
                "Wellington",
                "wellington@email.com",
                "85986846409"
        );

        System.out.println(contact);

        User user = new User();
        user.setName("Wellington");
        user.setUsername("wellington");
        user.setPassword("p@ssw0rd");

        System.out.println(user);

        printLoop();
    }

    private static void printLoop() {
        int i = 0;

        while(true) {
            if(i == 10) {
                break;
            }

            System.out.println("i: " + i);
            i ++;
        }
    }

}