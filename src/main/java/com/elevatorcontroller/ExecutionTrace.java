package com.elevatorcontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ExecutionTrace class for capturing and managing execution trace of the system.
 * Traces events in the format specified in the specification:
 * - # for user events
 * - * for door events
 * - + for elevator events
 */
public class ExecutionTrace {
    
    private final List<TraceEvent> events = Collections.synchronizedList(new ArrayList<>());
    private final ReentrantLock lock = new ReentrantLock();
    private int eventCounter = 0;
    
    /**
     * Inner class representing a single trace event
     */
    public static class TraceEvent {
        public final int eventNumber;
        public final long timestamp;
        public final char eventType; // '#' for user, '*' for door, '+' for elevator
        public final String actor;   // "Usager[id]", "Porte[floor]", "Ascenseur"
        public final String action;  // The action taken
        public final String description;
        
        public TraceEvent(int eventNumber, long timestamp, char eventType, 
                         String actor, String action, String description) {
            this.eventNumber = eventNumber;
            this.timestamp = timestamp;
            this.eventType = eventType;
            this.actor = actor;
            this.action = action;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return String.format("%d.%c %s: %c %s", eventNumber, eventType, actor, eventType, description);
        }
    }
    
    /**
     * Record a user event
     */
    public void recordUserEvent(int userId, String action) {
        recordEvent('#', String.format("Usager[%d]", userId), action);
    }
    
    /**
     * Record a door event
     */
    public void recordDoorEvent(int floor, String action) {
        recordEvent('*', String.format("Porte[%d]", floor), action);
    }
    
    /**
     * Record an elevator event
     */
    public void recordElevatorEvent(String action) {
        recordEvent('+', "Ascenseur", action);
    }
    
    /**
     * Record a trace event
     */
    private void recordEvent(char eventType, String actor, String action) {
        lock.lock();
        try {
            TraceEvent event = new TraceEvent(
                eventCounter++,
                System.currentTimeMillis(),
                eventType,
                actor,
                action,
                action
            );
            events.add(event);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get all recorded events
     */
    public List<TraceEvent> getEvents() {
        lock.lock();
        try {
            return new ArrayList<>(events);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get events as formatted string
     */
    public String getFormattedTrace() {
        StringBuilder sb = new StringBuilder();
        List<TraceEvent> eventList = getEvents();
        for (TraceEvent event : eventList) {
            sb.append(event).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Print all events to console
     */
    public void printTrace() {
        System.out.println("=== Execution Trace ===");
        List<TraceEvent> eventList = getEvents();
        for (TraceEvent event : eventList) {
            System.out.println(event);
        }
        System.out.println("======================");
    }
    
    /**
     * Clear all recorded events
     */
    public void clear() {
        lock.lock();
        try {
            events.clear();
            eventCounter = 0;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get the number of recorded events
     */
    public int getEventCount() {
        lock.lock();
        try {
            return events.size();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get events of a specific type
     */
    public List<TraceEvent> getEventsByType(char eventType) {
        List<TraceEvent> result = new ArrayList<>();
        for (TraceEvent event : getEvents()) {
            if (event.eventType == eventType) {
                result.add(event);
            }
        }
        return result;
    }
    
    /**
     * Get events for a specific actor
     */
    public List<TraceEvent> getEventsByActor(String actor) {
        List<TraceEvent> result = new ArrayList<>();
        for (TraceEvent event : getEvents()) {
            if (event.actor.equals(actor)) {
                result.add(event);
            }
        }
        return result;
    }
}
