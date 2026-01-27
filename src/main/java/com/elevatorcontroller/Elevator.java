package com.elevatorcontroller;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Elevator class representing the elevator component in the system.
 * Manages the elevator's current floor, direction, destinations, and calls.
 * 
 * Variables:
 * - floor: the current elevator floor
 * - direction: the current direction of the elevator (UP, DOWN, or NONE)
 * - destinations: vector of destinations entered by users
 * - calls: vector of calls made by users
 * 
 * Behavior:
 * - Move up or down one floor depending on the direction
 * - Reverse direction if reaching highest or lowest floor
 * - Signal to open the door when reaching a call or destination floor
 * - Clear calls and destinations when stopping at a floor
 */
public class Elevator {
    
    public enum Direction {
        UP, DOWN, NONE
    }
    
    private int floor;
    private Direction direction;
    private Set<Integer> destinations;
    private Set<Integer> calls;
    private final int lowestFloor;
    private final int highestFloor;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition floorChanged = lock.newCondition();
    private final Condition destinationAdded = lock.newCondition();
    private final Condition stopped = lock.newCondition();
    
    private volatile boolean isMoving = false;
    
    /**
     * Constructor for Elevator
     * @param lowestFloor the lowest floor the elevator can reach
     * @param highestFloor the highest floor the elevator can reach
     */
    public Elevator(int lowestFloor, int highestFloor) {
        this.floor = lowestFloor;
        this.lowestFloor = lowestFloor;
        this.highestFloor = highestFloor;
        this.direction = Direction.NONE;
        this.destinations = new TreeSet<>();
        this.calls = new TreeSet<>();
    }
    
    /**
     * Add a call to the elevator from a specific floor
     */
    public void addCall(int floor) {
        lock.lock();
        try {
            if (floor >= lowestFloor && floor <= highestFloor) {
                calls.add(floor);
                destinationAdded.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Add a destination for a user in the elevator
     */
    public void addDestination(int floor) {
        lock.lock();
        try {
            if (floor >= lowestFloor && floor <= highestFloor && floor != this.floor) {
                destinations.add(floor);
                destinationAdded.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if there is a destination or call on the current floor
     */
    public boolean hasCallOrDestinationAtCurrentFloor() {
        lock.lock();
        try {
            return calls.contains(floor) || destinations.contains(floor);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Clear calls and destinations for the current floor
     */
    public void clearCallsAndDestinationsAtCurrentFloor() {
        lock.lock();
        try {
            calls.remove(floor);
            destinations.remove(floor);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Choose the next direction based on the specification logic
     */
    public Direction chooseDirection() {
        lock.lock();
        try {
            // If there is a call on the current floor, indicate no current direction
            if (calls.contains(floor)) {
                direction = Direction.NONE;
                return direction;
            }
            
            // Look for calls or destinations in the current direction
            boolean hasInCurrentDirection = hasCallOrDestinationInDirection(direction);
            
            // If there is a call or destination in the current direction and not at boundary
            if (hasInCurrentDirection && !isAtBoundaryInDirection(direction)) {
                return direction;
            }
            
            // Search for call or destination in the opposite direction
            Direction opposite = getOppositeDirection(direction);
            boolean hasInOppositeDirection = hasCallOrDestinationInDirection(opposite);
            
            if (hasInOppositeDirection && !isAtBoundaryInDirection(opposite)) {
                direction = opposite;
                return direction;
            }
            
            // If there is no call or destination in any direction, change to NONE
            direction = Direction.NONE;
            return direction;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Check if there is a call or destination in the specified direction
     */
    private boolean hasCallOrDestinationInDirection(Direction dir) {
        if (dir == Direction.NONE) {
            // Look upwards first
            for (int f = floor; f <= highestFloor; f++) {
                if (calls.contains(f) || destinations.contains(f)) {
                    return true;
                }
            }
            // Then look downwards
            for (int f = floor; f >= lowestFloor; f--) {
                if (calls.contains(f) || destinations.contains(f)) {
                    return true;
                }
            }
            return false;
        }
        
        if (dir == Direction.UP) {
            for (int f = floor; f <= highestFloor; f++) {
                if (calls.contains(f) || destinations.contains(f)) {
                    return true;
                }
            }
        } else if (dir == Direction.DOWN) {
            for (int f = floor; f >= lowestFloor; f--) {
                if (calls.contains(f) || destinations.contains(f)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if elevator is at a boundary (highest or lowest floor) in the given direction
     */
    private boolean isAtBoundaryInDirection(Direction dir) {
        if (dir == Direction.UP) {
            return floor >= highestFloor;
        } else if (dir == Direction.DOWN) {
            return floor <= lowestFloor;
        }
        return false;
    }
    
    /**
     * Get the opposite direction
     */
    private Direction getOppositeDirection(Direction dir) {
        if (dir == Direction.UP) {
            return Direction.DOWN;
        } else if (dir == Direction.DOWN) {
            return Direction.UP;
        }
        return Direction.UP; // Default to UP when NONE
    }
    
    /**
     * Move the elevator one floor in the current direction
     */
    public void moveOneFloor() {
        lock.lock();
        try {
            if (direction == Direction.UP && floor < highestFloor) {
                floor++;
                floorChanged.signalAll();
            } else if (direction == Direction.DOWN && floor > lowestFloor) {
                floor--;
                floorChanged.signalAll();
            } else if (direction == Direction.UP && floor >= highestFloor) {
                // Reverse direction at the top
                direction = Direction.DOWN;
            } else if (direction == Direction.DOWN && floor <= lowestFloor) {
                // Reverse direction at the bottom
                direction = Direction.UP;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Stop the elevator
     */
    public void stop() {
        lock.lock();
        try {
            isMoving = false;
            direction = Direction.NONE;
            stopped.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Wait until the elevator reaches a specific floor
     */
    public void waitForFloor(int targetFloor) throws InterruptedException {
        lock.lock();
        try {
            while (floor != targetFloor) {
                floorChanged.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Wait until the elevator has no pending calls or destinations
     */
    public void waitUntilEmpty() throws InterruptedException {
        lock.lock();
        try {
            while (!calls.isEmpty() || !destinations.isEmpty()) {
                destinationAdded.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    // Getters
    public int getFloor() {
        lock.lock();
        try {
            return floor;
        } finally {
            lock.unlock();
        }
    }
    
    public Direction getDirection() {
        lock.lock();
        try {
            return direction;
        } finally {
            lock.unlock();
        }
    }
    
    public void setDirection(Direction direction) {
        lock.lock();
        try {
            this.direction = direction;
        } finally {
            lock.unlock();
        }
    }
    
    public Set<Integer> getDestinations() {
        lock.lock();
        try {
            return new TreeSet<>(destinations);
        } finally {
            lock.unlock();
        }
    }
    
    public Set<Integer> getCalls() {
        lock.lock();
        try {
            return new TreeSet<>(calls);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public void setMoving(boolean moving) {
        isMoving = moving;
    }
}
