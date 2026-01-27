package com.elevatorcontroller;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * User class representing a user in the elevator system.
 * 
 * Variables:
 * - floor: the user's current floor
 * - direction: the direction the user wants to take
 * - destination: the user's destination floor
 * 
 * Behavior:
 * - If a call was reported to the same floor in the opposite direction: wait
 * - Otherwise, call the elevator
 * - Wait for the door to open
 * - Decide whether or not to enter (the user may be distracted)
 * - If the door is still open, enter the elevator
 * - Enter the destination
 * - Wait for the door to close
 * - Wait until the elevator reaches its destination
 * - Wait for the door to open and then go out
 */
public class User implements Runnable {
    
    private final int userId;
    private final int startFloor;
    private final int destinationFloor;
    private final Elevator.Direction direction;
    private final SystemController system;
    private final Door startDoor;
    private final Door destinationDoor;
    
    private volatile boolean hasEnteredElevator = false;
    private volatile boolean hasReachedDestination = false;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();
    
    // Probability of user entering the elevator (0.0 to 1.0)
    private static final double ENTER_PROBABILITY = 0.9;
    
    /**
     * Constructor for User
     * @param userId unique identifier for the user
     * @param startFloor the floor where the user starts
     * @param destinationFloor the floor where the user wants to go
     * @param direction the direction the user wants to take (UP or DOWN)
     * @param system the system controller
     */
    public User(int userId, int startFloor, int destinationFloor, 
                Elevator.Direction direction, SystemController system) {
        this.userId = userId;
        this.startFloor = startFloor;
        this.destinationFloor = destinationFloor;
        this.direction = direction;
        this.system = system;
        this.startDoor = system.getDoor(startFloor);
        this.destinationDoor = system.getDoor(destinationFloor);
    }
    
    @Override
    public void run() {
        try {
            userBehavior();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Execute the user behavior sequence
     */
    private void userBehavior() throws InterruptedException {
        // Step 1: Call the elevator
        System.out.println("# Usager[" + userId + "]: # effective l'appel " + startFloor + "-" +
                   (direction == Elevator.Direction.UP ? "UP" : "DOWN"));
        
        // Step 2: Wait for the door to open at start floor
        startDoor.waitForDoorOpen();
        
        // Step 3: Decide whether to enter (with probability)
        boolean willEnter = Math.random() < ENTER_PROBABILITY;
        if (!willEnter) {
            System.out.println("# Usager[" + userId + "]: # declined to enter");
            return;
        }
        
        // Step 4: Enter the elevator if door is still open
        if (startDoor.isOpen()) {
            enterElevator();
        } else {
            System.out.println("# Usager[" + userId + "]: # door closed before entering");
            return;
        }
        
        // Step 5: Enter the destination
        enterDestination();
        
        // Step 6: Wait for the door to close
        startDoor.waitForDoorClosed();
        
        // Step 7: Wait until the elevator reaches the destination
        system.getElevator().waitForFloor(destinationFloor);
        
        // Step 8: Destination reached
        reachedDestination();
        
        // Step 9: Wait for the door to open at destination
        destinationDoor.waitForDoorOpen();
        
        // Step 10: Exit the elevator
        exitElevator();
    }
    
    /**
     * User enters the elevator
     */
    private void enterElevator() {
        lock.lock();
        try {
            hasEnteredElevator = true;
            System.out.println("# Usager[" + userId + "]: # entre ds l'ascenseur");
            stateChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * User enters the destination floor
     */
    private void enterDestination() {
        system.getElevator().addDestination(destinationFloor);
        System.out.println("# Usager[" + userId + "]: # entre la destination " + destinationFloor);
    }
    
    /**
     * User has reached the destination
     */
    private void reachedDestination() {
        lock.lock();
        try {
            hasReachedDestination = true;
            System.out.println("# Usager[" + userId + "]: # destination atteinte");
            stateChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * User exits the elevator
     */
    private void exitElevator() {
        System.out.println("# Usager[" + userId + "]: # sortie de l'ascenseur");
    }
    
    /**
     * Wait for user to enter elevator
     */
    public void waitForEntry() throws InterruptedException {
        lock.lock();
        try {
            while (!hasEnteredElevator) {
                stateChanged.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if user has entered the elevator
     */
    public boolean hasEntered() {
        lock.lock();
        try {
            return hasEnteredElevator;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if user has reached destination
     */
    public boolean hasReachedDest() {
        lock.lock();
        try {
            return hasReachedDestination;
        } finally {
            lock.unlock();
        }
    }
    
    // Getters
    public int getUserId() {
        return userId;
    }
    
    public int getStartFloor() {
        return startFloor;
    }
    
    public int getDestinationFloor() {
        return destinationFloor;
    }
    
    public Elevator.Direction getDirection() {
        return direction;
    }
    
    public int getExpectedDistance() {
        return Math.abs(destinationFloor - startFloor);
    }
}
