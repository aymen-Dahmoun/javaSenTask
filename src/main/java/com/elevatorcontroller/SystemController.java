package com.elevatorcontroller;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * SystemController class managing the overall elevator system.
 * 
 * Overall behavior specifications:
 * - When the elevator is in motion, no doors are open
 * - A user who requests the elevator will inevitably enter it
 * - There is never more than one door open at a time
 * - The distance traveled by a user is always equal to (source to destination)
 */
public class SystemController {
    
    private final Elevator elevator;
    private final Map<Integer, Door> doors;
    private final int lowestFloor;
    private final int highestFloor;
    
    private volatile Door openDoor = null;
    private final ReentrantLock doorLock = new ReentrantLock();
    private final Condition doorStateChanged = doorLock.newCondition();
    
    // Time constants (in milliseconds)
    private static final long MOVEMENT_TIME = 200; // Time between floor movements
    private static final long STOP_TIME = 300;     // Time for elevator to stop and operate doors
    
    /**
     * Constructor for SystemController
     * @param lowestFloor the lowest floor in the building
     * @param highestFloor the highest floor in the building
     */
    public SystemController(int lowestFloor, int highestFloor) {
        this.lowestFloor = lowestFloor;
        this.highestFloor = highestFloor;
        this.elevator = new Elevator(lowestFloor, highestFloor);
        this.doors = new HashMap<>();
        
        // Create doors for each floor
        for (int floor = lowestFloor; floor <= highestFloor; floor++) {
            doors.put(floor, new Door(floor, elevator));
        }
        
        // Start elevator controller thread
        startElevatorController();
    }
    
    /**
     * Call the elevator from a specific floor in a specific direction
     */
    public void callElevator(int floor, Elevator.Direction direction) {
        if (floor < lowestFloor || floor > highestFloor) {
            return;
        }
        
        elevator.addCall(floor);
    }
    
    /**
     * Get the door at a specific floor
     */
    public Door getDoor(int floor) {
        return doors.get(floor);
    }
    
    /**
     * Get the elevator
     */
    public Elevator getElevator() {
        return elevator;
    }
    
    /**
     * Check if a door is currently open
     */
    public boolean isDoorOpen() {
        doorLock.lock();
        try {
            return openDoor != null && openDoor.isOpen();
        } finally {
            doorLock.unlock();
        }
    }
    
    /**
     * Get the currently open door (if any)
     */
    public Door getOpenDoor() {
        doorLock.lock();
        try {
            return openDoor;
        } finally {
            doorLock.unlock();
        }
    }
    
    /**
     * Wait until a specific door is open
     */
    public void waitForDoorOpen(int floor) throws InterruptedException {
        doorLock.lock();
        try {
            while (openDoor == null || openDoor.getFloor() != floor || !openDoor.isOpen()) {
                doorStateChanged.await();
            }
        } finally {
            doorLock.unlock();
        }
    }
    
    /**
     * Start the elevator controller thread that manages elevator movement and door operations
     */
    private void startElevatorController() {
        Thread controllerThread = new Thread(() -> {
            try {
                elevatorControllerBehavior();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ElevatorController");
        controllerThread.setDaemon(false);
        controllerThread.start();
    }
    
    /**
     * Main elevator controller behavior
     */
    private void elevatorControllerBehavior() throws InterruptedException {
        while (true) {
            // Choose direction based on calls and destinations
            Elevator.Direction nextDirection = elevator.chooseDirection();
            
            if (nextDirection != Elevator.Direction.NONE) {
                elevator.setDirection(nextDirection);
                elevator.setMoving(true);
                System.out.println("+ Ascenseur: + direction: " + nextDirection);
                
                // Move the elevator one floor
                Thread.sleep(MOVEMENT_TIME);
                elevator.moveOneFloor();
                System.out.println("+ Ascenseur: + Etage: " + elevator.getFloor());
                
                // Check if we need to stop at this floor
                if (elevator.hasCallOrDestinationAtCurrentFloor()) {
                    elevator.setMoving(false);
                    System.out.println("+ Ascenseur: + arrêt à l'étage " + elevator.getFloor());
                    
                    // Operate the door
                    operateDoor(elevator.getFloor());
                    
                    // Clear calls and destinations for this floor
                    elevator.clearCallsAndDestinationsAtCurrentFloor();
                    
                    System.out.println("+ Ascenseur: + fin de l'arrêt");
                    Thread.sleep(STOP_TIME);
                }
            } else {
                // No calls or destinations, wait briefly before checking again
                elevator.setMoving(false);
                Thread.sleep(100);
            }
        }
    }
    
    /**
     * Operate the door at the specified floor
     */
    private void operateDoor(int floor) throws InterruptedException {
        doorLock.lock();
        try {
            // Set this door as the open door
            openDoor = doors.get(floor);
            doorStateChanged.signalAll();
        } finally {
            doorLock.unlock();
        }
        
        Door door = doors.get(floor);
        try {
            // Open door
            door.openDoor();
            
            // Keep door open for a while
            Thread.sleep(300);
            
            // Close door
            door.closeDoor();
        } finally {
            doorLock.lock();
            try {
                openDoor = null;
                doorStateChanged.signalAll();
            } finally {
                doorLock.unlock();
            }
        }
    }
    
    /**
     * Shutdown the system
     */
    public void shutdown() {
    }
}
