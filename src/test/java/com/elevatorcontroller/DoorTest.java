package com.elevatorcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Door class.
 * Tests door behavior including:
 * - Door opening and closing
 * - Synchronization with elevator
 * - Waiting for door state changes
 */
@DisplayName("Door Component Tests")
class DoorTest {
    
    private Elevator elevator;
    private Door door;
    
    @BeforeEach
    void setUp() {
        elevator = new Elevator(0, 2);
        door = new Door(1, elevator); // Door at floor 1
    }
    
    @Test
    @DisplayName("Door initializes closed")
    void testInitialState() {
        assertFalse(door.isOpen(), "Door should initialize as closed");
        assertFalse(door.isClosing(), "Door should not be closing initially");
    }
    
    @Test
    @DisplayName("Door floor is correctly set")
    void testDoorFloor() {
        assertEquals(1, door.getFloor(), "Door floor should be 1");
    }
    
    @Test
    @DisplayName("Door opens successfully")
    void testOpenDoor() throws InterruptedException {
        door.openDoor();
        assertTrue(door.isOpen(), "Door should be open after openDoor()");
    }
    
    @Test
    @DisplayName("Door closes successfully")
    void testCloseDoor() throws InterruptedException {
        door.openDoor();
        door.closeDoor();
        assertFalse(door.isOpen(), "Door should be closed after closeDoor()");
    }
    
    @Test
    @DisplayName("Multiple opens and closes work correctly")
    void testMultipleOperations() throws InterruptedException {
        door.openDoor();
        assertTrue(door.isOpen(), "Door should be open");
        door.closeDoor();
        assertFalse(door.isOpen(), "Door should be closed");
        
        door.openDoor();
        assertTrue(door.isOpen(), "Door should be open again");
        door.closeDoor();
        assertFalse(door.isOpen(), "Door should be closed again");
    }
    
    @Test
    @DisplayName("Can wait for door to open")
    void testWaitForDoorOpen() throws InterruptedException {
        Thread opener = new Thread(() -> {
            try {
                Thread.sleep(100);
                door.openDoor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        opener.start();
        door.waitForDoorOpen();
        assertTrue(door.isOpen(), "Door should be open after waiting");
        opener.join();
    }
    
    @Test
    @DisplayName("Can wait for door to close")
    void testWaitForDoorClosed() throws InterruptedException {
        door.openDoor();
        assertTrue(door.isOpen(), "Door should be open");
        
        Thread closer = new Thread(() -> {
            try {
                Thread.sleep(100);
                door.closeDoor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        closer.start();
        door.waitForDoorClosed();
        assertFalse(door.isOpen(), "Door should be closed after waiting");
        closer.join();
    }
    
    @Test
    @DisplayName("operateDoor waits for elevator at correct floor")
    void testOperateDoorWaitsForFloor() throws InterruptedException {
        Thread doorOperator = new Thread(() -> {
            try {
                door.operateDoor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        doorOperator.start();
        
        // Simulate elevator reaching the floor
        Thread.sleep(100);
        elevator.setDirection(Elevator.Direction.UP);
        elevator.moveOneFloor(); // Move from floor 0 to floor 1
        
        // Wait for door operation to complete
        doorOperator.join(3000);
        assertFalse(doorOperator.isAlive(), "Door operation should complete");
    }
}
