package com.elevatorcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Elevator class.
 * Tests the core functionality of the elevator including:
 * - Adding calls and destinations
 * - Direction selection logic
 * - Floor management
 * - Movement
 */
@DisplayName("Elevator Component Tests")
class ElevatorTest {
    
    private Elevator elevator;
    
    @BeforeEach
    void setUp() {
        // Create elevator for 3-floor building (floors 0, 1, 2)
        elevator = new Elevator(0, 2);
    }
    
    @Test
    @DisplayName("Elevator initializes at lowest floor")
    void testInitialFloor() {
        assertEquals(0, elevator.getFloor(), "Elevator should start at lowest floor");
    }
    
    @Test
    @DisplayName("Elevator initializes with NONE direction")
    void testInitialDirection() {
        assertEquals(Elevator.Direction.NONE, elevator.getDirection(), 
                     "Elevator should start with NONE direction");
    }
    
    @Test
    @DisplayName("Adding a call is recorded")
    void testAddCall() {
        elevator.addCall(1);
        assertTrue(elevator.getCalls().contains(1), "Call for floor 1 should be recorded");
    }
    
    @Test
    @DisplayName("Adding multiple calls is recorded")
    void testAddMultipleCalls() {
        elevator.addCall(1);
        elevator.addCall(2);
        assertTrue(elevator.getCalls().contains(1), "Call for floor 1 should be recorded");
        assertTrue(elevator.getCalls().contains(2), "Call for floor 2 should be recorded");
    }
    
    @Test
    @DisplayName("Adding a destination is recorded")
    void testAddDestination() {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.addDestination(1);
        assertTrue(elevator.getDestinations().contains(1), "Destination floor 1 should be recorded");
    }
    
    @Test
    @DisplayName("Adding destination at current floor is ignored")
    void testAddDestinationAtCurrentFloor() {
        elevator.addDestination(0); // Current floor is 0
        assertFalse(elevator.getDestinations().contains(0), 
                    "Destination at current floor should not be added");
    }
    
    @Test
    @DisplayName("hasCallOrDestinationAtCurrentFloor detects calls")
    void testHasCallAtCurrentFloor() {
        elevator.addCall(0);
        assertTrue(elevator.hasCallOrDestinationAtCurrentFloor(), 
                   "Should detect call at current floor");
    }
    
    @Test
    @DisplayName("hasCallOrDestinationAtCurrentFloor detects destinations")
    void testHasDestinationAtCurrentFloor() throws InterruptedException {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.moveOneFloor(); // Move to floor 1
        elevator.addDestination(1);
        assertTrue(elevator.hasCallOrDestinationAtCurrentFloor(), 
                   "Should detect destination at current floor");
    }
    
    @Test
    @DisplayName("Clear calls and destinations at current floor")
    void testClearCallsAndDestinationsAtCurrentFloor() {
        elevator.addCall(0);
        elevator.setDirection(Elevator.Direction.UP);
        elevator.addDestination(0); // This should be added at floor 0 after movement
        assertTrue(elevator.hasCallOrDestinationAtCurrentFloor(), 
                   "Call at current floor should exist");
        
        elevator.clearCallsAndDestinationsAtCurrentFloor();
        assertFalse(elevator.hasCallOrDestinationAtCurrentFloor(), 
                    "All calls and destinations at current floor should be cleared");
    }
    
    @Test
    @DisplayName("Moving UP increases floor")
    void testMoveUp() {
        elevator.setDirection(Elevator.Direction.UP);
        int initialFloor = elevator.getFloor();
        elevator.moveOneFloor();
        assertEquals(initialFloor + 1, elevator.getFloor(), 
                     "Moving UP should increase floor by 1");
    }
    
    @Test
    @DisplayName("Moving DOWN decreases floor")
    void testMoveDown() throws InterruptedException {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.moveOneFloor(); // Move to floor 1
        elevator.moveOneFloor(); // Move to floor 2
        
        elevator.setDirection(Elevator.Direction.DOWN);
        int initialFloor = elevator.getFloor();
        elevator.moveOneFloor();
        assertEquals(initialFloor - 1, elevator.getFloor(), 
                     "Moving DOWN should decrease floor by 1");
    }
    
    @Test
    @DisplayName("Direction choice with no calls or destinations returns NONE")
    void testChooseDirectionNoCallsOrDestinations() {
        Elevator.Direction direction = elevator.chooseDirection();
        assertEquals(Elevator.Direction.NONE, direction, 
                     "Should choose NONE when no calls or destinations");
    }
    
    @Test
    @DisplayName("Direction choice with call at current floor returns NONE")
    void testChooseDirectionCallAtCurrentFloor() {
        elevator.addCall(0); // Current floor
        Elevator.Direction direction = elevator.chooseDirection();
        assertEquals(Elevator.Direction.NONE, direction, 
                     "Should choose NONE when call is at current floor");
    }
    
    @Test
    @DisplayName("Direction choice prefers current direction")
    void testChooseDirectionMaintainsCurrent() {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.addCall(2); // Call above
        Elevator.Direction direction = elevator.chooseDirection();
        assertEquals(Elevator.Direction.UP, direction, 
                     "Should maintain UP direction when call is above");
    }
    
    @Test
    @DisplayName("Direction reverses at highest floor")
    void testDirectionReverseAtHighestFloor() {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.moveOneFloor(); // Floor 1
        elevator.moveOneFloor(); // Floor 2 (highest)
        elevator.moveOneFloor(); // Try to move beyond
        
        assertEquals(2, elevator.getFloor(), 
                     "Elevator should not move beyond highest floor");
    }
    
    @Test
    @DisplayName("Can add invalid floors without error")
    void testAddInvalidFloors() {
        elevator.addCall(-1);
        elevator.addCall(3);
        assertFalse(elevator.getCalls().contains(-1), "Invalid floor -1 should not be added");
        assertFalse(elevator.getCalls().contains(3), "Invalid floor 3 should not be added");
    }
    
    @Test
    @DisplayName("Stop sets direction to NONE")
    void testStop() {
        elevator.setDirection(Elevator.Direction.UP);
        elevator.stop();
        assertEquals(Elevator.Direction.NONE, elevator.getDirection(), 
                     "Stop should set direction to NONE");
    }
}
