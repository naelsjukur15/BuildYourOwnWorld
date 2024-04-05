package core;

public class Main {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.out.println("Can only have two arguments - the flag and input string");
            System.exit(0);
        } else if (args.length == 2 && args[0].equals("-s")) {
            World world = new World();
            world.interactWithInputString(args[1]);
            System.out.println(world.toString());
        } else {
            World world = new World();
            world.keyboardInteraction();
        }
    }
}
