package com.elevatorcontroller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple JUnit5-compatible test runner for executing tests.
 * Discovers and runs all test methods annotated with @Test.
 */
public class TestRunner {
    
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private List<String> failures = new ArrayList<>();
    
    public static void main(String[] args) throws Exception {
        TestRunner runner = new TestRunner();
        
        System.out.println("\n========================================");
        System.out.println("  Elevator Controller System Test Suite");
        System.out.println("========================================\n");
        
        // Run all test classes
        runner.runTestClass(ElevatorTest.class);
        runner.runTestClass(DoorTest.class);
        runner.runTestClass(SystemControllerTest.class);
        runner.runTestClass(IntegrationTest.class);
        
        // Print summary
        System.out.println("\n========================================");
        System.out.println("           Test Summary Report");
        System.out.println("========================================");
        System.out.println("Total Tests:   " + totalTests);
        System.out.println("Passed:        " + passedTests + " ✓");
        System.out.println("Failed:        " + failedTests + " ✗");
        System.out.println("Success Rate:  " + (totalTests > 0 ? (passedTests * 100 / totalTests) : 0) + "%");
        
        if (!runner.failures.isEmpty()) {
            System.out.println("\n========================================");
            System.out.println("           Failed Tests Details");
            System.out.println("========================================");
            for (String failure : runner.failures) {
                System.out.println(failure);
            }
        }
        
        System.out.println("\n========================================\n");
        System.exit(failedTests == 0 ? 0 : 1);
    }
    
    private void runTestClass(Class<?> testClass) throws Exception {
        System.out.println("[TEST CLASS] " + testClass.getSimpleName());
        System.out.println("-----------------------------------------");
        
        Object testInstance = testClass.getDeclaredConstructor().newInstance();
        int classPassCount = 0;
        int classFailCount = 0;
        
        for (Method method : testClass.getDeclaredMethods()) {
            // Check if method has @Test annotation
            if (method.getAnnotation(org.junit.jupiter.api.Test.class) != null) {
                try {
                    // Call setUp if exists
                    try {
                        Method setUp = testClass.getDeclaredMethod("setUp");
                        setUp.invoke(testInstance);
                    } catch (NoSuchMethodException e) {
                        // setUp not required
                    }
                    
                    totalTests++;
                    method.invoke(testInstance);
                    System.out.println("  ✓ " + method.getName());
                    passedTests++;
                    classPassCount++;
                } catch (Exception e) {
                    failedTests++;
                    classFailCount++;
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String failMsg = "  ✗ " + method.getName() + ": " + cause.getMessage();
                    System.out.println(failMsg);
                    failures.add(testClass.getSimpleName() + "." + method.getName() + 
                               ": " + cause.getMessage());
                }
            }
        }
        
        System.out.println("-----------------------------------------");
        System.out.println("  Passed: " + classPassCount + ", Failed: " + classFailCount);
    }
}
