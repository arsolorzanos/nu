# Stock Tax Calculator

A Java CLI application that calculates taxes for stock market operations using hexagonal architecture.

## Features

- Calculates 20% tax on stock market profits
- Implements weighted average price calculation for multiple purchases
- Handles accumulated losses deduction from future profits
- $20,000 threshold for tax exemption
- Comprehensive logging with SLF4J
- Hexagonal architecture for maintainability

## Architecture

The application follows hexagonal architecture principles, providing flexibility, maintainability, and testability. The architecture allows users to easily change input/output sources - some want JSON files, others CSV, and production would need database integration.

By creating `InputPort` and `OutputPort` interfaces in the domain, the business logic is completely independent. It's easy to build multiple adapters - for example, `JsonInputAdapter` for development, or a `DatabaseInputAdapter` for production. To switch adapters, you only need to change one line in `ApplicationConfig`, and everything will work. The domain and application layers don't need to change, only the adapter.

### Three Layers

The application has three main parts:

```
┌─────────────────────────────────────────────┐
│         INFRASTRUCTURE LAYER                │
│  (HOW data enters/exits: JSON, DB, API)     │
└─────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────┐
│         APPLICATION LAYER                   │
│  (WHAT to do: Orchestrate use cases)        │
└─────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────┐
│         DOMAIN LAYER                        │
│  (WHY: Business rules & logic)              │
└─────────────────────────────────────────────┘
```

#### 1. Domain Layer

**Location:** `src/main/java/com/stocktax/domain/`

**What it contains:**
- **Business Logic**: `TaxCalculator.java` - The actual tax calculation rules
- **Domain Models**: `Operation.java`, `StockPosition.java`, `TaxCalculation.java`
- **Ports (Interfaces)**: `InputPort.java`, `OutputPort.java`

#### 2. Application Layer (Orchestrator)

**Location:** `src/main/java/com/stocktax/application/`

**What it does:**
- Orchestrates the business logic
- Coordinates between input, domain, and output
- Adds application-level concerns (logging, error handling)
- Contains `TaxCalculationService` and `Application`

#### 3. Infrastructure Layer (The Adapters)

**Location:** `src/main/java/com/stocktax/infrastructure/adapters/`

**What it does:**
- Implements the port interfaces
- Handles all the "how" details (JSON parsing, file reading, database queries)
- Converts between external formats and domain objects
- Contains `JsonInputAdapter` and `JsonOutputAdapter`

### Data Flow

```
User types JSON into terminal
        ↓
[JsonInputAdapter] reads JSON from stdin
        ↓
[JsonInputAdapter] parses JSON → creates Operation objects
        ↓
[Application] receives List<Operation>
        ↓
[Application] calls TaxCalculator.calculateTaxes()
        ↓
[TaxCalculator] applies business rules
        ↓
[TaxCalculator] returns List<TaxCalculation>
        ↓
[Application] receives List<TaxCalculation>
        ↓
[JsonOutputAdapter] converts to JSON string
        ↓
[JsonOutputAdapter] writes to stdout
        ↓
User sees JSON output
```

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Build
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn clean package
```

### Run Application
```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.stocktax.application.Application"

# Using JAR
java -jar target/stock-tax-calculator-1.0.0.jar

# With input file
java -jar target/stock-tax-calculator-1.0.0.jar < test-input.txt
```

### Docker

The application can be containerized using Docker for easy deployment and distribution.

#### Build Docker Image
```bash
docker build -t stock-tax-calculator:1.0.0 .
```

#### Run Container

**Using stdin (pipe input):**
```bash
echo '[{"operation":"buy", "unit-cost":10.00, "quantity": 10000}]' | docker run -i stock-tax-calculator:1.0.0
```

**Using input file:**
```bash
docker run -i stock-tax-calculator:1.0.0 < input.txt
```

**Interactive mode:**
```bash
docker run -it stock-tax-calculator:1.0.0
```

**Multiple input lines:**
```bash
cat input.txt | docker run -i stock-tax-calculator:1.0.0
```

> **Note:** The `-i` flag keeps stdin open for input, which is required for the application to read JSON from stdin.

## Input Format

The application reads JSON arrays from stdin, one per line:

```json
[{"operation":"buy", "unit-cost":10.00, "quantity": 10000},{"operation":"sell", "unit-cost":20.00, "quantity": 5000}]
[{"operation":"buy", "unit-cost":20.00, "quantity": 10000},{"operation":"sell", "unit-cost":10.00, "quantity": 5000}]
```

## Output Format

For each input line, the application outputs a JSON array with tax calculations:

```json
[{"tax":0.00},{"tax":10000.00}]
[{"tax":0.00},{"tax":0.00}]
```

## Tax Calculation Rules

1. **No tax on buy operations**
2. **20% tax rate** on profits from sell operations
3. **$20,000 threshold**: Operations below this amount don't pay taxes
4. **Weighted average price**: Calculated for multiple purchases
5. **Loss deduction**: Losses are deducted from future profits
6. **No tax on losses**: Only profits are taxed

## Logging

The application uses SLF4J with Logback for logging:
- Console output for INFO level and above
- File logging to `logs/stock-tax-calculator.log`
- DEBUG level for application-specific loggers (`com.stocktax` package)

## Example

Input:
```json
[{"operation":"buy", "unit-cost":10.00, "quantity": 10000},{"operation":"sell", "unit-cost":20.00, "quantity": 5000}]
```

Output:
```json
[{"tax":0.00},{"tax":10000.00}]
```

Explanation:
- Buy operation: No tax (0.00)
- Sell operation: 5000 shares × $20 = $100,000 total
- Profit: $100,000 - (5000 × $10) = $50,000
- Tax: 20% × $50,000 = $10,000
