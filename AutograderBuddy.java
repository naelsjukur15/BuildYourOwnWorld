package core;

import tileengine.TETile;
import tileengine.Tileset;

public class AutograderBuddy {

    /**
     * Simulates a game, but doesn't render anything or call any StdDraw
     * methods. Instead, returns the world that would result if the input string
     * had been typed on the keyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quit and
     * save. To "quit" in this method, save the game to a file, then just return
     * the TETile[][]. Do not call System.exit(0) in this method.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] getWorldFromInput(String input) {
        World world = new World();
        world.setRenderingEnabled(false);


        if (input.startsWith("l")) {
            world.restorePreviousGame("output.txt");
            input = input.substring(1);
        } else {
            int sIndex = input.toLowerCase().indexOf('s');
            if (sIndex != -1) {
                world.interactWithInputString(input.substring(0, sIndex + 1));
                input = input.substring(sIndex + 1);
            } else {
                world.interactWithInputString(input);
                return world.getWorldState();
            }
        }

        boolean waitingForSaveCommand = false;
        for (int i = 0; i < input.length(); i++) {
            char command = input.charAt(i);

            if (waitingForSaveCommand && command == 'q') {
                world.handleSaveOnly();
                break;
            }

            waitingForSaveCommand = (command == ':');

            if (!waitingForSaveCommand) {
                world.processGameCommand(command);
            }
        }

        return world.getWorldState();
    }


    /**
     * Used to tell the autograder which tiles are the floor/ground (including
     * any lights/items resting on the ground). Change this
     * method if you add additional tiles.
     */
    public static boolean isGroundTile(TETile t) {
        return t.character() == Tileset.FLOOR.character()
                || t.character() == Tileset.AVATAR.character()
                || t.character() == Tileset.FLOWER.character();
    }

    /**
     * Used to tell the autograder while tiles are the walls/boundaries. Change
     * this method if you add additional tiles.
     */
    public static boolean isBoundaryTile(TETile t) {
        return t.character() == Tileset.WALL.character()
                || t.character() == Tileset.LOCKED_DOOR.character()
                || t.character() == Tileset.UNLOCKED_DOOR.character();
    }
}
