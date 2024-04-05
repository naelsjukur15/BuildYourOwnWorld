package core;

import tileengine.TETile;
import tileengine.Tileset;
import tileengine.TERenderer;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


import java.io.FileWriter;
import java.io.IOException;


public class World {
    TERenderer ter = new TERenderer();
    public static final int WORLD_WIDTH = 82;
    public static final int WORLD_HEIGHT = 33;
    private TETile[][] tileWorld = new TETile[WORLD_WIDTH][WORLD_HEIGHT];
    private Random randomObject;
    private Map<Integer, Room> roomMap = new HashMap<>();
    private Map<Integer, ArrayList<Room>> minDistRooms = new TreeMap<>();
    private int numOfRooms = 0;
    private Map<Integer, Room> secMinDisRooms = new TreeMap<>();
    public static final int SEED_MULT = 10;
    public static final int MIN_ROOM_COUNT = 50;
    public static final int MAX_ROOM_COUNT = 80;
    private int avatarXCoordinate;
    private int avatarYCoordinate;
    private double mouseXCoordinate;
    private double mouseYCoordinate;
    private boolean isLightsOn = true;
    private boolean enableRendering = true;
    private boolean waitingForSaveCommand = false;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    public void keyboardInteraction() {
        ter.initialize(WORLD_WIDTH, WORLD_HEIGHT);

        char[] menuKeys = new char[]{'n', 'N', 'q', 'Q', 'l', 'L'};
        ArrayList<Character> validMenuKeys = new ArrayList<Character>();
        for (char key : menuKeys) {
            validMenuKeys.add(key);
        }

        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                displayMainMenu();
            } else {
                handleGamePlay();
            }
        }
    }

    public void setRenderingEnabled(boolean enable) {
        this.enableRendering = enable;
    }
    public void handleGamePlay() {
        char currentKey;
        char previousKey = 0;

        while (true) {
            while (StdDraw.hasNextKeyTyped()) {
                currentKey = StdDraw.nextKeyTyped();
                switch (currentKey) {
                    case 'n':
                    case 'N':
                        requestInput();
                        break;
                    case 'l':
                    case 'L':
                        restorePreviousGame("output.txt");
                        break;
                    case 'Q':
                    case 'q':
                        if (previousKey == ':') {
                            handleQuitAndSave();
                        } else {
                            System.exit(0);
                        }
                        break;
                    case 'H':
                    case 'h':
                        isLightsOn = !isLightsOn;
                        renderWorldState();
                        break;
                    default:
                        break;
                }
                previousKey = currentKey;
                char[] movementKeys = new char[]{'w', 'a', 's', 'd', 'W', 'A', 'S', 'D'};
                ArrayList<Character> validMovementKeys = new ArrayList<Character>();
                for (char move : movementKeys) {
                    validMovementKeys.add(move);
                }
                if (validMovementKeys.contains(currentKey)) {
                    controlAvatar(currentKey);
                    if (isLightsOn) {
                        this.ter.renderFrame(tileWorld);
                    } else {
                        this.ter.renderFrame(tileWorld, isLightsOn, avatarXCoordinate, avatarYCoordinate);
                    }
                }
            }
            visualizeHUD();
        }
    }

    private void renderWorldState() {
        if (!enableRendering) {
            return;
        }
        if (isLightsOn) {
            this.ter.renderFrame(tileWorld);
        } else {
            this.ter.renderFrame(tileWorld, isLightsOn, avatarXCoordinate, avatarYCoordinate);
        }
    }

    public void displayMainMenu() {
        StdDraw.setXscale(0, WORLD_WIDTH);
        StdDraw.setYscale(0, WORLD_HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font titleFont = new Font("Monaco", Font.BOLD, 25);
        StdDraw.setFont(titleFont);
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT * 2 / 3, "MAIN MENU");
        Font optionsFont = new Font("Monaco", Font.PLAIN, 25);
        StdDraw.setFont(optionsFont);
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, "NEW WORLD (N)");
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT / 3, "LOAD WORLD (L)");
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT / 4, "QUIT (Q)");
        StdDraw.show();
    }

    public void seedDisplay(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font seedFont = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(seedFont);
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, "Seed is:");
        StdDraw.text(WORLD_WIDTH / 2, WORLD_HEIGHT / 3, s);
        StdDraw.show();
    }

    public void requestInput() {
        String inputString = "";
        displaySeedInput(inputString);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char inputChar = StdDraw.nextKeyTyped();
                if (Character.isDigit(inputChar)) {
                    inputString += inputChar;
                    displaySeedInput(inputString);
                } else if (inputChar == 's' || inputChar == 'S') {
                    TETile[][] generatedTiles = interactWithInputString(inputString);
                    if (isLightsOn) {
                        this.ter.renderFrame(generatedTiles);
                    } else {
                        this.ter.renderFrame(generatedTiles, isLightsOn, avatarXCoordinate, avatarYCoordinate);
                    }
                    break;
                }
            }
        }
    }

    public void processGameCommand(char command) {
        command = Character.toLowerCase(command);

        if (waitingForSaveCommand) {
            if (command == 'q') {
                handleQuitAndSave();
                waitingForSaveCommand = false;
                return;
            } else {
                waitingForSaveCommand = false;
            }
        }

        switch (command) {
            case 'w':
                controlAvatar('w');
                break;
            case 'a':
                controlAvatar('a');
                break;
            case 's':
                controlAvatar('s');
                break;
            case 'd':
                controlAvatar('d');
                break;
            case ':':
                waitingForSaveCommand = true;
                break;
            default:
                break;
        }
    }

    public TETile[][] getWorldState() {
        return tileWorld;
    }
    public void handleQuitAndSave() {
        try {
            File outputFile = new File("output.txt");
            FileWriter fileSaver = new FileWriter(outputFile);
            String worldDescription = convertTilesToString(tileWorld);
            fileSaver.write(worldDescription);
            fileSaver.close();
            System.out.println("Data has been saved succesfully " + outputFile);
            System.exit(0);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void handleSaveOnly() {
        try {
            File outputFile = new File("output.txt");
            FileWriter fileSaver = new FileWriter(outputFile);
            fileSaver.write(convertTilesToString(tileWorld));
            fileSaver.close();
            System.out.println("Data has been saved succesfully " + outputFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void displaySeedInput(String seed) {
        seedDisplay(seed);
    }
    private String convertTilesToString(TETile[][] tiles) {
        return tileStringConverter(tiles);
    }

    private TETile[][] processInput(String input) {
        return interactWithInputString(input);
    }
    public String tileStringConverter(TETile[][] tiles) {
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int x = 0; x < WORLD_WIDTH; x++) {
            List<String> descriptionsList = new ArrayList<>();
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                descriptionsList.add(tiles[x][y].description());
            }
            descriptionBuilder.append(descriptionsList).append("\n");
        }
        return descriptionBuilder.toString();
    }

    public TETile mapDescriptionToTile(String desc) {
        TETile selectedTile = Tileset.NOTHING;
        switch (desc) {
            case "water":
                selectedTile = Tileset.WATER;
                break;
            case "wall":
                selectedTile = Tileset.WALL;
                break;
            case "floor":
                selectedTile = Tileset.FLOOR;
                break;
            case "you":
                selectedTile = Tileset.AVATAR;
                break;
            default:
                break;
        }
        return selectedTile;
    }

    public String[] convertLineToArray(String line) {
        return line.replace("[", "").replace("]", "").split(", ");
    }

    public void restorePreviousGame(String fileOutput) {
        In fileReader = new In(fileOutput);
        while (fileReader.hasNextLine()) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                String line = fileReader.readLine();
                String[] tileDescriptions = convertLineToArray(line);

                for (int y = 0; y < WORLD_HEIGHT; y++) {
                    tileWorld[x][y] = mapDescriptionToTile(tileDescriptions[y]);
                    if ("you".equals(tileDescriptions[y])) {
                        avatarXCoordinate = x;
                        avatarYCoordinate = y;
                    }
                }
            }
        }
        if (enableRendering) {
            visualizeGameWorld(tileWorld);
        }
    }

    public void visualizeHUD() {
        int currentMouseX = (int) Math.floor(StdDraw.mouseX());
        int currentMouseY = (int) Math.floor(StdDraw.mouseY());

        if (currentMouseX < WORLD_WIDTH && currentMouseY < WORLD_HEIGHT) {
            if (mousePositionChanged(StdDraw.mouseX(), StdDraw.mouseY())) {
                currentMouseX = (int) Math.floor(StdDraw.mouseX());
                currentMouseY = (int) Math.floor(StdDraw.mouseY());
                visualizeGameWorld(tileWorld);
            }
            StdDraw.setPenColor(Color.white);
            StdDraw.textLeft(1, WORLD_HEIGHT - 1, tileWorld[currentMouseX][currentMouseY].description());
            showDateTime();
            StdDraw.show();
        }
    }

    private void visualizeGameWorld(TETile[][] world) {
        if (!enableRendering) {
            return;
        }
        if (!isLightsOn) {
            this.ter.renderFrame(world);
        } else {
            this.ter.renderFrame(world, isLightsOn, avatarXCoordinate, avatarYCoordinate);
        }
    }

    private boolean mousePositionChanged(double x, double y) {
        if (x != mouseXCoordinate || y != mouseYCoordinate) {
            mouseXCoordinate = x;
            mouseYCoordinate = y;
            return true;
        }
        return false;
    }

    private void showDateTime() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StdDraw.textRight(WORLD_WIDTH - 1, WORLD_HEIGHT - 1, dateFormat.format(now));
    }
    public void generateAvatar() {
        List<List<Integer>> validPoints = secMinDisRooms.get(1).getValidSpawnPoints();
        if (!validPoints.isEmpty()) {
            int index = randomObject.nextInt(validPoints.size());
            List<Integer> coordinates = validPoints.get(index);
            avatarXCoordinate = coordinates.get(0);
            avatarYCoordinate = coordinates.get(1);
            tileWorld[avatarXCoordinate][avatarYCoordinate] = Tileset.AVATAR;
        }
    }

    public void controlAvatar(char input) {
        switch (Character.toLowerCase(input)) {
            case 'w':
                shiftUp();
                break;
            case 'a':
                shiftLeft();
                break;
            case 's':
                shiftDown();
                break;
            case 'd':
                shiftRight();
                break;
            default:
                break;
        }
    }


    public void shiftRight() {
        if (!tileWorld[avatarXCoordinate + 1][avatarYCoordinate].equals(Tileset.WALL)) {
            updatePosition(avatarXCoordinate + 1, avatarYCoordinate);
        }
        updateAvatarLocation();
    }


    public void shiftLeft() {
        if (!tileWorld[avatarXCoordinate - 1][avatarYCoordinate].equals(Tileset.WALL)) {
            updatePosition(avatarXCoordinate - 1, avatarYCoordinate);
        }
        updateAvatarLocation();
    }


    public void shiftUp() {
        if (!tileWorld[avatarXCoordinate][avatarYCoordinate + 1].equals(Tileset.WALL)) {
            updatePosition(avatarXCoordinate, avatarYCoordinate + 1);
        }
        updateAvatarLocation();
    }


    public void shiftDown() {
        if (!tileWorld[avatarXCoordinate][avatarYCoordinate - 1].equals(Tileset.WALL)) {
            updatePosition(avatarXCoordinate, avatarYCoordinate - 1);
        }
        updateAvatarLocation();
    }


    private void updatePosition(int newX, int newY) {
        tileWorld[newX][newY] = Tileset.AVATAR;
        tileWorld[avatarXCoordinate][avatarYCoordinate] = Tileset.FLOOR;
        avatarXCoordinate = newX;
        avatarYCoordinate = newY;
    }


    private void updateAvatarLocation() {
        tileWorld[avatarXCoordinate][avatarYCoordinate] = Tileset.AVATAR;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */

    public TETile[][] interactWithInputString(String input) {
        int seed = parseInput(input);
        randomObject = new Random(seed);

        fillBackground();
        populateTilesWithRooms();
        generateAvatar();
        if (input.charAt(0) == 'l' || input.charAt(0) == 'L') {
            restorePreviousGame("output.txt");
            for (int i = 1; i < input.length(); i++) {
                controlAvatar(input.charAt(i));
            }
        } else if (input.charAt(0) == 'n' || input.charAt(0) == 'N') {
            int sPos = findStartPosition(input);
            if (sPos < input.length()) {
                String remaining = parseRemaining(input);
                executeGameCommands(remaining);
            }
        }
        return tileWorld;
    }

    private void executeGameCommands(String commandSequence) {
        char prevKey = 0;
        for (char c : commandSequence.toCharArray()) {
            char currKey = c;

            if (isQuitCommand(currKey, prevKey)) {
                handleSaveOnly();
            }

            prevKey = currKey;
            if (isMovementKey(currKey)) {
                controlAvatar(currKey);
            }
        }
    }

    private boolean isQuitCommand(char currentKey, char previousKey) {
        return (currentKey == 'Q' || currentKey == 'q') && (previousKey == ':' || previousKey == 0);
    }

    private boolean isMovementKey(char key) {
        char[] possibleMoves = {'w', 'a', 's', 'd', 'W', 'A', 'S', 'D'};
        for (char move : possibleMoves) {
            if (move == key) {
                return true;
            }
        }
        return false;
    }

    private int findStartPosition(String input) {
        int i = 0;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            if (currentChar == 'S' || currentChar == 's') {
                return i + 1;
            }
            i++;
        }
        return 0;
    }

    public int parseInput(String input) {
        long seed = 0;
        int i = 0;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            if (currentChar == 'n' || currentChar == 'N') {
                i++;
                continue;
            } else if (currentChar == 's' || currentChar == 'S') {
                break;
            } else {
                seed = seed * SEED_MULT + currentChar;
            }
            i++;
        }
        return (int) seed;
    }

    public String parseRemaining(String input) {
        int currentTracker = 0;
        StringBuilder toReturn = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            if (currentChar == 'S' || currentChar == 's') {
                currentTracker = i + 1;
                break;
            }
            i++;
        }
        for (int j = currentTracker; j < input.length(); j++) {
            toReturn.append(input.charAt(j));
        }

        return toReturn.toString();
    }


    public class Room {
        private int width;
        private int height;
        private int xCoord;
        private int yCoord;
        private int roomNumber;
        private int randXCoord;
        private int randYCoord;

        public Room(int roomWidth, int roomHeight, int xCoordinate, int yCoordinate) {
            width = roomWidth;
            height = roomHeight;
            xCoord = xCoordinate;
            yCoord = yCoordinate;
            this.roomNumber = numOfRooms + 1;

            randXCoord = randomInRangeHelper(xCoord + 1, width + xCoord - 2);
            randYCoord = randomInRangeHelper(yCoord + 1, height + yCoord - 2);
        }

        public List<List<Integer>> getValidSpawnPoints() {
            List<List<Integer>> validPoints = new ArrayList<>();
            for (int x = xCoord + 1; x < width + xCoord - 1; x++) {
                for (int y = yCoord + 1; y < height + yCoord - 1; y++) {
                    if (!tileWorld[x][y].equals(Tileset.WALL)) {
                        validPoints.add(Arrays.asList(x, y));
                    }
                }
            }
            return validPoints;
        }
    }

    public int randomInRangeHelper(int minimumValue, int maximumValue) {
        return randomObject.nextInt((maximumValue - minimumValue) + 1) + minimumValue;
    }

    public void fillBackground() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                tileWorld[x][y] = Tileset.WATER;
            }
        }
    }

    public void instantiateRoom(int width, int height, int xStart, int yStart) {
        Room createRoom = new Room(width, height, xStart, yStart);
        roomMap.put(createRoom.roomNumber, createRoom);

        setFloors(width, height, xStart, yStart);
        fillWalls(width, height, xStart, yStart);

        tileWorld[createRoom.randXCoord][createRoom.randYCoord] = Tileset.FLOOR;
        numOfRooms += 1;
    }

    public void setFloors(int width, int height, int xStart, int yStart) {
        for (int x = xStart; x < xStart + width; x++) {
            for (int y = yStart; y < yStart + height; y++) {
                tileWorld[x][y] = Tileset.FLOOR;
            }
        }
    }

    public void fillWalls(int width, int height, int xStart, int yStart) {
        for (int y = yStart; y < yStart + height; y++) {
            tileWorld[xStart][y] = Tileset.WALL;
            tileWorld[width + xStart - 1][y] = Tileset.WALL;
        }

        for (int x = xStart; x < xStart + width; x++) {
            tileWorld[x][yStart] = Tileset.WALL;
            tileWorld[x][height + yStart - 1] = Tileset.WALL;
        }
    }

    public void populateTilesWithRooms() {
        int totalRooms = generateRandomRoomCount();
        createAndPlaceRooms(totalRooms);
        organizeAndConnectRooms();
    }

    private int generateRandomRoomCount() {
        return randomInRangeHelper(MIN_ROOM_COUNT, MAX_ROOM_COUNT);
    }

    private void createAndPlaceRooms(int roomCount) {
        for (int i = 0; i < roomCount; i++) {
            int width = randomInRangeHelper(4, 10);
            int height = randomInRangeHelper(4, 10);
            int startX = randomInRangeHelper(1, WORLD_WIDTH - 10);
            int startY = randomInRangeHelper(1, WORLD_HEIGHT - 10);

            if (isAreaEmpty(startX, startY, width, height)) {
                instantiateRoom(width, height, startX, startY);
            }
        }
    }

    private boolean isAreaEmpty(int startX, int startY, int width, int height) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (tileWorld[x][y].equals(Tileset.WALL) || tileWorld[x][y].equals(Tileset.FLOOR)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void organizeAndConnectRooms() {
        reorderMapOfRooms();
        connectAdjacentRooms();
    }

    private void connectAdjacentRooms() {
        int counter = 1;
        for (ArrayList<Room> roomList : minDistRooms.values()) {
            for (Room currentRoom : roomList) {
                secMinDisRooms.put(counter++, currentRoom);
            }
        }

        for (int i = 1; i < secMinDisRooms.size(); i++) {
            connectRoomWithHallways(secMinDisRooms.get(i), secMinDisRooms.get(i + 1));
        }
    }

    public int distBetweenRooms(Room firstRoom, Room secondRoom) {
        double returnDistance = secondRoom.randXCoord - firstRoom.randXCoord;
        return (int) returnDistance;
    }

    public void reorderMapOfRooms() {
        Room furhtestRoom = findFurthestRoom();
        organizeRoomsByDistance(furhtestRoom);
    }

    private Room findFurthestRoom() {
        Room furthest = roomMap.get(1);
        int minXStart = furthest.xCoord;

        for (Room room : roomMap.values()) {
            if (room.xCoord < minXStart) {
                minXStart = room.xCoord;
                furthest = room;
            }
        }
        return furthest;
    }

    private void organizeRoomsByDistance(Room furthestRoom) {
        for (Room room : roomMap.values()) {
            int distance = distBetweenRooms(furthestRoom, room);
            minDistRooms.computeIfAbsent(distance, k -> new ArrayList<>()).add(room);
        }
    }

    public void connectRoomWithHallways(Room firstRoom, Room secondRoom) {
        int pathRandomizer = randomInRangeHelper(0, 1);

        if (pathRandomizer == 0) {
            verticalToHorizontalShift(firstRoom, secondRoom);
        } else {
            horizontalToVerticalShift(firstRoom, secondRoom);
        }
    }

    public void verticalToHorizontalShift(Room firstRoom, Room secondRoom) {
        if (firstRoom.randYCoord > secondRoom.randYCoord) {
            verticalToHorizontalHelper(firstRoom, secondRoom);
        } else if (firstRoom.randYCoord < secondRoom.randYCoord) {
            for (int yCoordinate = firstRoom.randYCoord; yCoordinate <= secondRoom.randYCoord; yCoordinate++) {
                waterWallChecker(yCoordinate, firstRoom);
            }
        }
        for (int xCoordinate = firstRoom.randXCoord; xCoordinate <= secondRoom.randXCoord; xCoordinate++) {
            floorWallChecker(xCoordinate, secondRoom);
        }
    }

    public void floorWallChecker(int xCoordinate, Room secondRoom) {
        boolean tChecker = !tileWorld[xCoordinate - 1][secondRoom.randYCoord + 1].equals(Tileset.FLOOR);
        if (tileWorld[xCoordinate][secondRoom.randYCoord].equals(Tileset.WALL)) {
            tileWorld[xCoordinate][secondRoom.randYCoord] = Tileset.FLOOR;
            if (tileWorld[xCoordinate][secondRoom.randYCoord - 1].equals(Tileset.WATER)) {
                tileWorld[xCoordinate][secondRoom.randYCoord - 1] = Tileset.WALL;
            }
            if (tileWorld[xCoordinate][secondRoom.randYCoord + 1].equals(Tileset.WATER)) {
                tileWorld[xCoordinate][secondRoom.randYCoord + 1] = Tileset.WALL;
            }
        } else if (tileWorld[xCoordinate][secondRoom.randYCoord].equals(Tileset.WATER)) {
            tileWorld[xCoordinate][secondRoom.randYCoord] = Tileset.FLOOR;
            tileWorld[xCoordinate][secondRoom.randYCoord + 1] = Tileset.WALL;
            tileWorld[xCoordinate][secondRoom.randYCoord - 1] = Tileset.WALL;
        } else if (tileWorld[xCoordinate][secondRoom.randYCoord].equals(Tileset.FLOOR)) {
            if (!tileWorld[xCoordinate][secondRoom.randYCoord - 1].equals(Tileset.FLOOR)) {
                if (!tileWorld[xCoordinate - 1][secondRoom.randYCoord - 1].equals(Tileset.FLOOR)) {
                    tileWorld[xCoordinate - 1][secondRoom.randYCoord - 1] = Tileset.WALL;
                }
                tileWorld[xCoordinate][secondRoom.randYCoord - 1] = Tileset.WALL;
            }
            if (!tileWorld[xCoordinate][secondRoom.randYCoord + 1].equals(Tileset.FLOOR)) {
                if (tChecker) {
                    tileWorld[xCoordinate - 1][secondRoom.randYCoord + 1] = Tileset.WALL;
                }
                tileWorld[xCoordinate][secondRoom.randYCoord + 1] = Tileset.WALL;
            }
            if (tileWorld[xCoordinate - 1][secondRoom.randYCoord].equals(Tileset.WALL) && tChecker) {
                tileWorld[xCoordinate - 1][secondRoom.randYCoord + 1] = Tileset.WALL;
            }
        }
    }

    public void waterWallChecker(int y, Room firstRoom) {
        if (tileWorld[firstRoom.randXCoord][y].equals(Tileset.WALL)) {
            if (tileWorld[firstRoom.randXCoord - 1][y].equals(Tileset.WATER)) {
                tileWorld[firstRoom.randXCoord - 1][y] = Tileset.WALL;
            }
            if (tileWorld[firstRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
                tileWorld[firstRoom.randXCoord + 1][y] = Tileset.WALL;
            }
            tileWorld[firstRoom.randXCoord][y] = Tileset.FLOOR;
        } else if (tileWorld[firstRoom.randXCoord][y].equals(Tileset.WATER)) {
            tileWorld[firstRoom.randXCoord][y] = Tileset.FLOOR;
            tileWorld[firstRoom.randXCoord + 1][y] = Tileset.WALL;
            tileWorld[firstRoom.randXCoord - 1][y] = Tileset.WALL;
        }
        if (tileWorld[firstRoom.randXCoord][y].equals(Tileset.FLOOR)) {
            if (tileWorld[firstRoom.randXCoord][y + 1].equals(Tileset.WALL)
                    && tileWorld[firstRoom.randXCoord - 1][y + 1].equals(Tileset.WALL)) {
                tileWorld[firstRoom.randXCoord - 1][y + 1] = Tileset.WALL;
                if (!tileWorld[firstRoom.randXCoord - 1][y].equals(Tileset.FLOOR)) {
                    tileWorld[firstRoom.randXCoord - 1][y] = Tileset.WALL;
                }
            }
            if (tileWorld[firstRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
                tileWorld[firstRoom.randXCoord + 1][y] = Tileset.WALL;
            }
            if (tileWorld[firstRoom.randXCoord - 1][y + 1].equals(Tileset.WATER)) {
                tileWorld[firstRoom.randXCoord + 1][y + 1] = Tileset.WALL;
            }
        }
    }
    public void verticalToHorizontalHelper(Room room1, Room room2) {
        for (int y = room1.randYCoord; y >= room2.randYCoord; y--) {
            if (tileWorld[room1.randXCoord][y].equals(Tileset.WALL)) {
                if (tileWorld[room1.randXCoord - 1][y].equals(Tileset.WATER)) {
                    tileWorld[room1.randXCoord - 1][y] = Tileset.WALL;
                }
                if (tileWorld[room1.randXCoord + 1][y].equals(Tileset.WATER)) {
                    tileWorld[room1.randXCoord + 1][y] = Tileset.WALL;
                }
                tileWorld[room1.randXCoord][y] = Tileset.FLOOR;
            } else if (tileWorld[room1.randXCoord][y].equals(Tileset.WATER)) {
                tileWorld[room1.randXCoord][y] = Tileset.FLOOR;
                tileWorld[room1.randXCoord + 1][y] = Tileset.WALL;
                tileWorld[room1.randXCoord - 1][y] = Tileset.WALL;
            }
        }
    }
    public void horizontalToVerticalShift(Room firstRoom, Room secondRoom) {
        for (int x = firstRoom.randXCoord; x <= secondRoom.randXCoord; x++) {
            helperHorizontalToVerticalShift(x, firstRoom);
        }

        if (firstRoom.randYCoord > secondRoom.randYCoord) {
            for (int y = firstRoom.randYCoord; y >= secondRoom.randYCoord; y--) {
                createVertRooms1(y, secondRoom);
            }
        } else {
            for (int y = firstRoom.randYCoord; y < secondRoom.randYCoord; y++) {
                createVertRooms2(y, secondRoom);
            }
        }
    }

    public void helperHorizontalToVerticalShift(int x, Room firstRoom) {
        if (tileWorld[x][firstRoom.randYCoord].equals(Tileset.WALL)) {
            if (tileWorld[x][firstRoom.randYCoord - 1].equals(Tileset.WATER)) {
                tileWorld[x][firstRoom.randYCoord - 1] = Tileset.WALL;
            }
            if (tileWorld[x][firstRoom.randYCoord + 1].equals(Tileset.WATER)) {
                tileWorld[x][firstRoom.randYCoord + 1] = Tileset.WALL;
            }
            tileWorld[x][firstRoom.randYCoord] = Tileset.FLOOR;
        } else if (tileWorld[x][firstRoom.randYCoord].equals(Tileset.WATER)) {
            tileWorld[x][firstRoom.randYCoord] = Tileset.FLOOR;
            tileWorld[x][firstRoom.randYCoord + 1] = Tileset.WALL;
            tileWorld[x][firstRoom.randYCoord - 1] = Tileset.WALL;
        }
        if (tileWorld[x][firstRoom.randYCoord].equals(Tileset.FLOOR)
                && tileWorld[x][firstRoom.randYCoord - 1].equals(Tileset.WATER)) {
            tileWorld[x][firstRoom.randYCoord - 1] = Tileset.WALL;
        }
        if (tileWorld[x][firstRoom.randYCoord].equals(Tileset.FLOOR)
                && tileWorld[x + 1][firstRoom.randYCoord + 1].equals(Tileset.WATER)) {
            tileWorld[x + 1][firstRoom.randYCoord + 1] = Tileset.WALL;
        }
    }
    public void createVertRooms1(int y, Room secondRoom) {
        if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.WALL)) {
            tileWorld[secondRoom.randXCoord][y] = Tileset.FLOOR;
            if (tileWorld[secondRoom.randXCoord - 1][y].equals(Tileset.WATER)) {
                tileWorld[secondRoom.randXCoord - 1][y] = Tileset.WALL;
            }
            if (tileWorld[secondRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
                tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            }
        } else if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.WATER)) {
            tileWorld[secondRoom.randXCoord][y] = Tileset.FLOOR;
            tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            tileWorld[secondRoom.randXCoord - 1][y] = Tileset.WALL;
        }
        if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.FLOOR)
                && tileWorld[secondRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
            tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            tileWorld[secondRoom.randXCoord + 1][y + 1] = Tileset.WALL;
        }
    }

    public void createVertRooms2(int y, Room secondRoom) {
        if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.FLOOR)
                && tileWorld[secondRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
            tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            tileWorld[secondRoom.randXCoord + 1][y - 1] = Tileset.WALL;
        }
        if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.WALL)) {
            tileWorld[secondRoom.randXCoord][y] = Tileset.FLOOR;
            if (tileWorld[secondRoom.randXCoord - 1][y].equals(Tileset.WATER)) {
                tileWorld[secondRoom.randXCoord - 1][y] = Tileset.WALL;
            }
            if (tileWorld[secondRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
                tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            }
        } else if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.WATER)) {
            tileWorld[secondRoom.randXCoord][y] = Tileset.FLOOR;
            tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
            tileWorld[secondRoom.randXCoord - 1][y] = Tileset.WALL;
        }
        if (tileWorld[secondRoom.randXCoord][y].equals(Tileset.FLOOR)
                && tileWorld[secondRoom.randXCoord + 1][y].equals(Tileset.WATER)) {
            tileWorld[secondRoom.randXCoord + 1][y] = Tileset.WALL;
        }
    }
}

