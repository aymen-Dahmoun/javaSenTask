package com.elevatorcontroller;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Door class representing a door at a specific floor.
 * 
 * Variables:
 * - floor: the door floor
 * 
 * Behavior:
 * - Wait until the elevator has stopped at the floor indicated by the door
 * - Open the door
 * - Wait a certain amount of time
 * - Close the door
 * - Signal to the elevator that it can restart
 */
public class Door {
    
    private final int floor;
    private final Elevator elevator;
    private volatile boolean isOpen = false;
    private volatile boolean isClosing = false;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition doorStateChanged = lock.newCondition();
    
    // Time constants (in milliseconds)
    private static final long DOOR_OPEN_TIME = 100; // Time door stays open
    private static final long DOOR_OPENING_TIME = 50;   // Time to open door
    private static final long DOOR_CLOSING_TIME = 50;   // Time to close door
    
    /**
     * Constructor for Door
     * @param floor the floor where this door is located
     * @param elevator the elevator instance
     */
    public Door(int floor, Elevator elevator) {
        this.floor = floor;
        this.elevator = elevator;
    }
    
    /**
     * Execute the door behavior:
     * 1. Wait until the elevator has stopped at the floor
     * 2. Open the door
     * 3. Wait a certain amount of time
     * 4. Close the door
     * 5. Signal that the elevator can restart
     */
    public void operateDoor() throws InterruptedException {
        try {
            // Wait until elevator reaches this floor
            elevator.waitForFloor(floor);
            
            // Open the door
            openDoor();
            
            // Wait a certain amount of time
            Thread.sleep(DOOR_OPEN_TIME);
            
            // Close the door
            closeDoor();
            
            // Signal that elevator can restart (clearing destinations/calls already done)
        } catch (InterruptedException e) {
            throw e;
        }
    }
    
    /**
     * Open the door at this floor
     */
    public synchronized void openDoor() throws InterruptedException {
        lock.lock();
        try {
            // Simulate door opening
            Thread.sleep(DOOR_OPENING_TIME);
            isOpen = true;
            isClosing = false;
            doorStateChanged.signalAll();
            System.out.println("* Porte[" + floor + "]: * ouverture");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Close the door at this floor
     */
    public synchronized void closeDoor() throws InterruptedException {
        lock.lock();
        try {
            // Simulate door closing
            Thread.sleep(DOOR_CLOSING_TIME);
            isOpen = false;
            isClosing = true;
            doorStateChanged.signalAll();
            System.out.println("* Porte[" + floor + "]: * fermeture");
            // Wait briefly for users to exit
            Thread.sleep(50);
            isClosing = false;
            doorStateChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Wait until the door is open
     */
    public void waitForDoorOpen() throws InterruptedException {
        lock.lock();
        try {
            while (!isOpen) {
                doorStateChanged.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Wait until the door is closed
     */
    public void waitForDoorClosed() throws InterruptedException {
        lock.lock();
        try {
            while (isOpen || isClosing) {
                doorStateChanged.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if door is open
     */
    public boolean isOpen() {
        lock.lock();
        try {
            return isOpen;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if door is closing
     */
    public boolean isClosing() {
        lock.lock();
        try {
            return isClosing;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get floor number
     */
    public int getFloor() {
        return floor;
    }
}
