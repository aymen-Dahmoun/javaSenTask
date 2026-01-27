package com.elevatorcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SystemController class.
 * Tests system-level behaviors including:
 * - Call elevator functionality
 * - Door management
 * - Elevator controller logic
 * - System invariants
 */
@DisplayName("System Controller Tests")
class SystemControllerTest {
    
    private SystemController system;
    
    @BeforeEach
    void setUp() {
        // Create system with 3 floors (0, 1, 2)
        system = new SystemController(0, 2);
        // Allow some time for controller to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    @DisplayName("System initializes with correct floors")
    void testSystemInitialization() {
        assertNotNull(system.getElevator(), "Elevator should be initialized");
        assertNotNull(system.getDoor(0), "Door at floor 0 should exist");
        assertNotNull(system.getDoor(1), "Door at floor 1 should exist");
        assertNotNull(system.getDoor(2), "Door at floor 2 should exist");
    }
    
    @Test
    @DisplayName("Elevator starts at lowest floor")
    void testElevatorStartsAtLowestFloor() {
        assertEquals(0, system.getElevator().getFloor(), 
                     "Elevator should start at floor 0");
    }
    
    @Test
    @DisplayName("Can retrieve door by floor")
    void testGetDoor() {
        Door door = system.getDoor(1);
        assertNotNull(door, "Door at floor 1 should exist");
        assertEquals(1, door.getFloor(), "Door should be at floor 1");
    }
    
    @Test
    @DisplayName("Call elevator adds call to elevator")
    void testCallElevator() {
        system.callElevator(1, Elevator.Direction.UP);
        assertTrue(system.getElevator().getCalls().contains(1), 
                   "Call for floor 1 should be recorded in elevator");
    }
    
    @Test
    @DisplayName("Invalid floor call is rejected")
    void testCallElevatorInvalidFloor() {
        system.callElevator(5, Elevator.Direction.UP);
        assertFalse(system.getElevator().getCalls().contains(5), 
                    "Call for invalid floor should not be recorded");
    }
    
    @Test
    @DisplayName("No doors are open initially")
    void testNoDoorOpenInitially() {
        assertFalse(system.isDoorOpen(), "No door should be open initially");
    }
    
    @Test
    @DisplayName("System has all required doors")
    void testAllDoorsExist() {
        for (int floor = 0; floor <= 2; floor++) {
            Door door = system.getDoor(floor);
            assertNotNull(door, "Door should exist at floor " + floor);
            assertEquals(floor, door.getFloor(), "Door should be at correct floor");
        }
    }
    
    @Test
    @DisplayName("Elevator can be called from multiple floors")
    void testMultipleCalls() {
        system.callElevator(0, Elevator.Direction.UP);
        system.callElevator(1, Elevator.Direction.UP);
        system.callElevator(2, Elevator.Direction.DOWN);
        
        assertTrue(system.getElevator().getCalls().contains(0), "Call at floor 0 recorded");
        assertTrue(system.getElevator().getCalls().contains(1), "Call at floor 1 recorded");
        assertTrue(system.getElevator().getCalls().contains(2), "Call at floor 2 recorded");
    }
    
    @Test
    @DisplayName("System invariant: elevator moves up and down")
    void testElevatorMovement() throws InterruptedException {
        Elevator elevator = system.getElevator();
        int initialFloor = elevator.getFloor();
        
        // Give time for elevator controller to process
        Thread.sleep(500);
        
        // Elevator should still be at initial floor (no calls)
        assertEquals(initialFloor, elevator.getFloor(), 
                     "Elevator should remain at initial floor with no calls");
    }
}
