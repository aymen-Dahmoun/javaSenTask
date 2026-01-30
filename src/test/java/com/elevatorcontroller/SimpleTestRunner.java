package com.elevatorcontroller;

/**
 * Simple test runner without JUnit dependency for basic testing.
 * Tests the core elevator simulator functionality.
 */
public class SimpleTestRunner {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("ELEVATOR SIMULATOR - TEST SUITE");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Run Elevator tests
        testElevatorComponent();
        
        // Run Door tests
        testDoorComponent();
        
        // Run SystemController tests
        testSystemController();
        
        // Run basic integration test
        testBasicIntegration();
        
        // Print summary
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("TEST SUMMARY");
        System.out.println("=".repeat(80));
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        System.out.println("Success rate: " + (testsRun == 0 ? "N/A" : String.format("%.1f%%", 100.0 * testsPassed / testsRun)));
        System.out.println();
    }
    
    private static void testElevatorComponent() {
        System.out.println("[UNIT TEST] Elevator Component Tests");
        System.out.println("-".repeat(80));
        
        // Test 1: Elevator initializes at lowest floor
        test("Elevator initializes at lowest floor", () -> {
            Elevator elevator = new Elevator(0, 2);
            assert elevator.getFloor() == 0 : "Elevator should start at floor 0";
        });
        
        // Test 2: Elevator initializes with NONE direction
        test("Elevator initializes with NONE direction", () -> {
            Elevator elevator = new Elevator(0, 2);
            assert elevator.getDirection() == Elevator.Direction.NONE : "Direction should be NONE";
        });
        
        // Test 3: Adding a call is recorded
        test("Adding a call is recorded", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.addCall(1);
            assert elevator.getCalls().contains(1) : "Call for floor 1 should be recorded";
        });
        
        // Test 4: Adding multiple calls is recorded
        test("Adding multiple calls is recorded", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.addCall(1);
            elevator.addCall(2);
            assert elevator.getCalls().contains(1) : "Call for floor 1 should be recorded";
            assert elevator.getCalls().contains(2) : "Call for floor 2 should be recorded";
        });
        
        // Test 5: Adding a destination is recorded
        test("Adding a destination is recorded", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.setDirection(Elevator.Direction.UP);
            elevator.addDestination(1);
            assert elevator.getDestinations().contains(1) : "Destination floor 1 should be recorded";
        });
        
        // Test 6: Moving UP increases floor
        test("Moving UP increases floor", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.setDirection(Elevator.Direction.UP);
            int initialFloor = elevator.getFloor();
            elevator.moveOneFloor();
            assert elevator.getFloor() == initialFloor + 1 : "Moving UP should increase floor";
        });
        
        // Test 7: Moving DOWN decreases floor
        test("Moving DOWN decreases floor", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.setDirection(Elevator.Direction.UP);
            elevator.moveOneFloor();
            elevator.moveOneFloor();
            elevator.setDirection(Elevator.Direction.DOWN);
            int initialFloor = elevator.getFloor();
            elevator.moveOneFloor();
            assert elevator.getFloor() == initialFloor - 1 : "Moving DOWN should decrease floor";
        });
        
        // Test 8: Direction choice with no calls or destinations returns NONE
        test("Direction choice with no calls/destinations returns NONE", () -> {
            Elevator elevator = new Elevator(0, 2);
            Elevator.Direction direction = elevator.chooseDirection();
            assert direction == Elevator.Direction.NONE : "Should choose NONE when no calls";
        });
        
        // Test 9: Stop sets direction to NONE
        test("Stop sets direction to NONE", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.setDirection(Elevator.Direction.UP);
            elevator.stop();
            assert elevator.getDirection() == Elevator.Direction.NONE : "Stop should set direction to NONE";
        });
        
        // Test 10: Clear calls and destinations
        test("Clear calls and destinations at current floor", () -> {
            Elevator elevator = new Elevator(0, 2);
            elevator.addCall(0);
            assert elevator.hasCallOrDestinationAtCurrentFloor() : "Should have call at current floor";
            elevator.clearCallsAndDestinationsAtCurrentFloor();
            assert !elevator.hasCallOrDestinationAtCurrentFloor() : "Should have no calls/destinations";
        });
        
        System.out.println();
    }
    
    private static void testDoorComponent() {
        System.out.println("[UNIT TEST] Door Component Tests");
        System.out.println("-".repeat(80));
        
        // Test 1: Door initializes closed
        test("Door initializes closed", () -> {
            Elevator elevator = new Elevator(0, 2);
            Door door = new Door(1, elevator);
            assert !door.isOpen() : "Door should initialize as closed";
        });
        
        // Test 2: Door floor is correctly set
        test("Door floor is correctly set", () -> {
            Elevator elevator = new Elevator(0, 2);
            Door door = new Door(1, elevator);
            assert door.getFloor() == 1 : "Door floor should be 1";
        });
        
        // Test 3: Door opens successfully
        test("Door opens successfully", () -> {
            Elevator elevator = new Elevator(0, 2);
            Door door = new Door(1, elevator);
            door.openDoor();
            assert door.isOpen() : "Door should be open after openDoor()";
        });
        
        // Test 4: Door closes successfully
        test("Door closes successfully", () -> {
            Elevator elevator = new Elevator(0, 2);
            Door door = new Door(1, elevator);
            door.openDoor();
            door.closeDoor();
            assert !door.isOpen() : "Door should be closed after closeDoor()";
        });
        
        // Test 5: Multiple opens and closes work correctly
        test("Multiple opens and closes work correctly", () -> {
            Elevator elevator = new Elevator(0, 2);
            Door door = new Door(1, elevator);
            door.openDoor();
            assert door.isOpen() : "Door should be open";
            door.closeDoor();
            assert !door.isOpen() : "Door should be closed";
            door.openDoor();
            assert door.isOpen() : "Door should be open again";
            door.closeDoor();
            assert !door.isOpen() : "Door should be closed again";
        });
        
        System.out.println();
    }
    
    private static void testSystemController() {
        System.out.println("[UNIT TEST] System Controller Tests");
        System.out.println("-".repeat(80));
        
        // Test 1: System initializes with correct floors
        test("System initializes with correct floors", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            assert system.getElevator() != null : "Elevator should be initialized";
            assert system.getDoor(0) != null : "Door at floor 0 should exist";
            assert system.getDoor(1) != null : "Door at floor 1 should exist";
            assert system.getDoor(2) != null : "Door at floor 2 should exist";
        });
        
        // Test 2: Elevator starts at lowest floor
        test("Elevator starts at lowest floor", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            assert system.getElevator().getFloor() == 0 : "Elevator should start at floor 0";
        });
        
        // Test 3: Can retrieve door by floor
        test("Can retrieve door by floor", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            Door door = system.getDoor(1);
            assert door != null : "Door at floor 1 should exist";
            assert door.getFloor() == 1 : "Door should be at floor 1";
        });
        
        // Test 4: Call elevator adds call to elevator
        test("Call elevator adds call to elevator", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            system.callElevator(1, Elevator.Direction.UP);
            assert system.getElevator().getCalls().contains(1) : "Call for floor 1 should be recorded";
        });
        
        // Test 5: Invalid floor call is rejected
        test("Invalid floor call is rejected", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            system.callElevator(5, Elevator.Direction.UP);
            assert !system.getElevator().getCalls().contains(5) : "Call for invalid floor should not be recorded";
        });
        
        // Test 6: System has all required doors
        test("System has all required doors", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(100);
            for (int floor = 0; floor <= 2; floor++) {
                Door door = system.getDoor(floor);
                assert door != null : "Door should exist at floor " + floor;
                assert door.getFloor() == floor : "Door should be at correct floor";
            }
        });
        
        System.out.println();
    }
    
    private static void testBasicIntegration() {
        System.out.println("[INTEGRATION TEST] Basic System Behavior");
        System.out.println("-".repeat(80));
        
        // Test 1: Single user scenario
        test("Single user completes journey", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(200);
            User user = new User(0, 0, 2, Elevator.Direction.UP, system);
            Thread userThread = new Thread(user);
            userThread.start();
            userThread.join(8000);
            assert !userThread.isAlive() : "User scenario should complete";
        });
        
        // Test 2: System responds to elevator calls
        test("System elevator moves when called", () -> {
            SystemController system = new SystemController(0, 2);
            Thread.sleep(200);
            Elevator elevator = system.getElevator();
            system.callElevator(2, Elevator.Direction.UP);
            Thread.sleep(2000);
            // Elevator should have moved towards floor 2
            int floor = elevator.getFloor();
            assert floor > 0 || elevator.getDirection() == Elevator.Direction.UP : 
                "Elevator should move towards called floor";
        });
        
        // Test 3: User distance invariant
        test("User distance equals source to destination", () -> {
            User user = new User(0, 0, 2, Elevator.Direction.UP, new SystemController(0, 2));
            int expectedDistance = Math.abs(2 - 0);
            assert user.getExpectedDistance() == 2 : "Expected distance should be 2 floors";
        });
        
        System.out.println();
    }
    
    private static void test(String testName, TestCase testCase) {
        testsRun++;
        try {
            testCase.run();
            testsPassed++;
            System.out.println("✓ PASS: " + testName);
        } catch (AssertionError e) {
            testsFailed++;
            System.out.println("✗ FAIL: " + testName);
            System.out.println("  Error: " + e.getMessage());
        } catch (Exception e) {
            testsFailed++;
            System.out.println("✗ ERROR: " + testName);
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    @FunctionalInterface
    interface TestCase {
        void run() throws Exception;
    }
}
