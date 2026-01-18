# Unit Tests for Calendar Scheduler

This directory contains JUnit 5 tests for the Calendar Scheduler module.

## Test Classes

### Core Model Tests
- **GreatCalendarTest** - Tests for the GreatCalendar data model including working hours calculations
- **GreatCalendarEntryTest** - Tests for the GreatEntry nested class with various entry fields
- **PersonalProfileTest** - Tests for PersonalProfile with profile data validation
- **ConflictRuleTest** - Tests for ConflictRule with different field types and operators

### Utility Tests
- **ConflictResolutionCalculatorTest** - Tests for the conflict resolution logic (core functionality only)
- **PersonalProfileMapperTest** - Tests for the PersonalProfile mapper constants
- **ConflictRuleProviderTest** - Tests for ConflictRuleProvider instantiation

### Integration Tests
- **SchedulerIntegrationTest** - High-level integration tests validating data model interactions

## Running Tests

### From Maven for Java Extension (VS Code)
1. Open Maven Explorer sidebar
2. Navigate to `scheduler` → `Lifecycle`
3. Double-click `test` to run all tests
4. Or right-click on individual test classes to run specific tests

### From Command Line
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=GreatCalendarTest

# Run a specific test method
mvn test -Dtest=GreatCalendarTest#testCalculateWorkingHoursSingleEntry
```

## Test Configuration

- **junit-platform.properties** - Disables parallel test execution for stability
- **HeadlessTestExtension** - Configures JavaFX for headless testing environments

## Known Limitations

- Tests that require FormsFX GUI components are kept to basic instantiation checks only
- CalendarFX GUI-dependent tests are simplified to avoid initialization issues in CI/CD environments
- The test suite focuses on core business logic rather than UI components

## Adding New Tests

When adding new tests:
1. Create a `*Test.java` class in this directory
2. Use `@BeforeEach` for setup and `@AfterEach` for cleanup
3. Follow the naming convention: `test[MethodName][Condition]`
4. Avoid direct CalendarFX GUI instantiation unless necessary
