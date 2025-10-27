# Advanced Fraud Detection Algorithm - Tasks 1, 2, 3

This project extends the basic Flink DataStream API fraud detection tutorial by adding location-based fraud detection using zip code information.

## Project Overview

The advanced fraud detection algorithm detects fraudulent transactions based on the pattern: **small transaction followed by large transaction in the same zip code**. This is more sophisticated than the basic algorithm which only considers transaction amounts.

## Project Structure

```
frauddetection/
├── src/main/java/spendreport/
│   ├── DetailedTransaction.java          # Task 1 - Transaction with zip code
│   ├── DetailedAlert.java               # Task 2 - Alert with detailed info
│   ├── DetailedAlertSink.java           # Task 2 - Sink for detailed alerts
│   ├── DetailedTransactionSource.java    # Task 3 - Random transaction source
│   ├── DetailedTransactionSourceTest.java # Task 3 - Hand-crafted test source
│   ├── DetailedTransactionSourceTestJob.java # Task 3 - Test job for hand-crafted
│   ├── DetailedTransactionSourceRandomTestJob.java # Task 3 - Test job for random
│   ├── DetailedFraudDetector.java       # Task 4 - Enhanced fraud detector
│   ├── DetailedFraudDetectorTestJob.java # Task 4 - Test job for fraud detector
│   ├── DetailedFraudDetectionJob.java   # Task 5 - Complete integrated system
│   ├── FraudDetectionJob.java           # Original job
│   └── FraudDetector.java               # Original detector
├── src/main/resources/
│   └── log4j2.properties                # Updated with DetailedAlertSink config
└── pom.xml                              # Project configuration
```

## Prerequisites

- **Java 25** (OpenJDK)
- **Maven 3.x**
- **Apache Flink 1.17.2**

## Installation & Setup

### 1. Install Java 25
```bash
sudo apt update
sudo apt install -y openjdk-25-jdk
java -version  # Verify installation
```

### 2. Compile Project
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
mvn clean compile
```

### 3. Run with VM Options (to suppress warnings)
```bash
java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" <MainClass>
```

---

## Task 1: DetailedTransaction Class

### Objective
Replace the basic Transaction class with a DetailedTransaction class that includes zip code information for enhanced fraud detection capabilities.

### Implementation
- **File**: `src/main/java/spendreport/DetailedTransaction.java`
- **Fields**: accountId (Long), amount (double), timestamp (long), zipCode (String)
- **Features**: Complete POJO with constructors, getters, setters, toString, equals, hashCode

### Key Features
- **Zip Code Support**: Added String field for zip code information
- **Compatibility**: Maintains same field types as original Transaction class
- **Best Practices**: Follows Java POJO design patterns with proper null safety

### Usage Example
```java
DetailedTransaction transaction = new DetailedTransaction(
    1L,           // accountId
    100.50,       // amount
    System.currentTimeMillis(), // timestamp
    "01003"       // zipCode
);
```

---

## Task 2: DetailedAlert Class

### Objective
Create a DetailedAlert class that extends the basic Alert with additional fraud information including account ID, timestamp, zip code, and amount for comprehensive fraud reporting.

### Implementation
- **File**: `src/main/java/spendreport/DetailedAlert.java`
- **Fields**: accountId (Long), timestamp (long), zipCode (String), amount (double)
- **Features**: Complete POJO with all standard methods

### DetailedAlertSink Class
- **File**: `src/main/java/spendreport/DetailedAlertSink.java`
- **Purpose**: Logs DetailedAlert instances using slf4j
- **Logging Format**: `FRAUD DETECTED - Account: {accountId}, Timestamp: {timestamp}, Zip: {zipCode}, Amount: ${amount}`

### Log4j2 Configuration
Updated `src/main/resources/log4j2.properties`:
```properties
logger.detailedSink.name = spendreport.DetailedAlertSink
logger.detailedSink.level = INFO
```

### Alert Flow Analysis
**Classes that Alert instances pass through:**
1. **FraudDetector** → Creates Alert instances
2. **FraudDetectionJob** → Receives DataStream<Alert>
3. **AlertSink** → Uses slf4j logging internally
4. **Log4j2** → Outputs to console via ConsoleAppender

---

## Task 3: DetailedTransactionSource Class

### Objective
Create a DetailedTransactionSource that randomly generates DetailedTransaction instances at runtime with specific properties for fraud detection testing.

### Implementation
- **File**: `src/main/java/spendreport/DetailedTransactionSource.java`
- **Interface**: Extends `RichSourceFunction<DetailedTransaction>`
- **Generation**: Random generation at runtime (not static)

### Random Generation Properties
- **Account ID**: Uniformly random from {1, 2, 3, 4, 5}
- **Timestamp**: Increments by 1 second for each transaction
- **Zip Code**: Uniformly random from {"01003", "02115", "78712"}
- **Amount**: Uniformly random from (0, 1000] (exclusive 0, inclusive 1000)

### Test Sources
1. **DetailedTransactionSourceTest.java**: Hand-crafted static data for debugging
2. **DetailedTransactionSourceTestJob.java**: Test job using hand-crafted data
3. **DetailedTransactionSourceRandomTestJob.java**: Test job using random data

---

## Task 4: DetailedFraudDetector Class

### Objective
Create a DetailedFraudDetector class that implements enhanced fraud detection logic using zip code information. The detector identifies fraudulent transactions based on the pattern: small transaction (< $10) followed by large transaction (≥ $500) from the same account in the same zip code within 1 minute.

### Implementation Details
- **File**: `src/main/java/spendreport/DetailedFraudDetector.java`
- **Interface**: Extends `KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert>`
- **State Management**: Tracks small transactions with account, zip code, amount, and timer

### Enhanced Fraud Detection Logic
- **Small Transaction**: < $10 (updated from < $1)
- **Large Transaction**: ≥ $500
- **Time Window**: 1 minute
- **Zip Code Matching**: Both transactions must be in the same zip code
- **Account Matching**: Both transactions must be from the same account

### Key Features
1. **State Variables**: flagState, zipCodeState, amountState, timerState
2. **Zip Code Validation**: Only triggers alerts when small and large transactions are in the same zip
3. **Timer Management**: 1-minute timeout for small transactions
4. **Debug Logging**: Detailed console output for monitoring fraud detection
5. **State Cleanup**: Proper cleanup when fraud is detected or timer expires

### Test Job
- **DetailedFraudDetectorTestJob.java**: Test job using hand-crafted data to verify fraud detection logic

---

## Task 5: DetailedFraudDetectionJob Class

### Objective
Create a DetailedFraudDetectionJob class that integrates all the new classes (DetailedTransaction, DetailedAlert, DetailedTransactionSource, DetailedFraudDetector) to provide a complete location-based fraud detection system.

### Implementation Details
- **File**: `src/main/java/spendreport/DetailedFraudDetectionJob.java`
- **Integration**: Uses all Task 1-4 classes together
- **Data Flow**: DetailedTransactionSource → DetailedFraudDetector → DetailedAlertSink

### Complete System Architecture
1. **Data Source**: DetailedTransactionSource generates random transactions
2. **Processing**: DetailedFraudDetector analyzes transactions for fraud patterns
3. **Output**: DetailedAlertSink logs fraud alerts with detailed information

### Key Features
- **End-to-End Integration**: Complete fraud detection pipeline
- **Real-time Processing**: Continuous stream processing of transactions
- **Location-based Detection**: Uses zip code information for enhanced accuracy
- **Comprehensive Logging**: Detailed fraud alerts with all relevant information

---

## Commands & Usage

### General Commands

#### Compilation
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
mvn clean compile
```

#### Run Original Fraud Detection Job
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.FraudDetectionJob
```

#### Run with Maven (Alternative)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
mvn exec:java -Dexec.mainClass="spendreport.FraudDetectionJob" -Dexec.args="--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED"
```

---

## Task-Specific Commands

### Task 1: DetailedTransaction Class

#### Test DetailedTransaction Class
```bash
# Compile the project (includes DetailedTransaction)
cd /home/anandyala/acads/532/HW-4/frauddetection
mvn compile

# Verify DetailedTransaction is compiled successfully
ls target/classes/spendreport/DetailedTransaction.class
```

#### Run Original Job (uses DetailedTransaction indirectly)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.FraudDetectionJob
```

---

### Task 2: DetailedAlert Class

#### Test DetailedAlert and DetailedAlertSink Classes
```bash
# Compile the project (includes DetailedAlert and DetailedAlertSink)
cd /home/anandyala/acads/532/HW-4/frauddetection
mvn compile

# Verify classes are compiled successfully
ls target/classes/spendreport/DetailedAlert.class
ls target/classes/spendreport/DetailedAlertSink.class
```

#### Test Log4j2 Configuration
```bash
# Check log4j2.properties configuration
cd /home/anandyala/acads/532/HW-4/frauddetection
cat src/main/resources/log4j2.properties | grep -A 2 "detailedSink"
```

#### Run Job with DetailedAlertSink (when integrated)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectionJob
```

---

### Task 3: DetailedTransactionSource Class

#### Test Hand-crafted Transaction Source (Debugging)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 20s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedTransactionSourceTestJob
```

#### Test Random Transaction Source (Production)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 15s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedTransactionSourceRandomTestJob
```

#### Verify Transaction Generation Properties
```bash
# Check that transactions have correct properties:
# - Account IDs: 1,2,3,4,5
# - Zip Codes: 01003, 02115, 78712  
# - Amounts: (0, 1000]
# - Timestamps: incrementing by 1 second
```

#### Run Extended Test (2+ minutes for fraud detection)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 120s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedTransactionSourceRandomTestJob
```

---

### Task 4: DetailedFraudDetector Class

#### Test DetailedFraudDetector with Hand-crafted Data
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 30s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectorTestJob
```

#### Verify Fraud Detection Logic
```bash
# Expected output should show:
# - Small transaction detection messages
# - FRAUD DETECTED messages for valid patterns
# - DetailedAlertSink logging with account, timestamp, zip, amount
```

#### Test Fraud Detection with Random Data
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 60s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectorTestJob
```

#### Verify Fraud Detection Properties
```bash
# Check that fraud detection works correctly:
# - Small transactions: < $10
# - Large transactions: >= $500
# - Same account: Both transactions from same account
# - Same zip code: Both transactions in same zip code
# - Time window: Within 1 minute
```

---

### Task 5: DetailedFraudDetectionJob Class

#### Run Complete Fraud Detection System
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 60s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectionJob
```

#### Run Extended Test (2+ minutes for fraud detection)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 120s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectionJob
```

#### Verify Complete System Integration
```bash
# Expected output should show:
# - Random transaction generation
# - Small transaction detection messages
# - FRAUD DETECTED messages for valid patterns
# - DetailedAlertSink logging with account, timestamp, zip, amount
```

---

### Combined Task Testing

#### Test All Tasks Together (when DetailedFraudDetectionJob is ready)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedFraudDetectionJob
```

#### Debug Mode (with hand-crafted data)
```bash
cd /home/anandyala/acads/532/HW-4/frauddetection
timeout 30s java --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" spendreport.DetailedTransactionSourceTestJob
```

---

## Expected Output Examples

### Hand-crafted Test Source Output
```
Generated Transaction: DetailedTransaction{accountId=1, amount=0.5, timestamp=1000, zipCode='01003'}
Generated Transaction: DetailedTransaction{accountId=2, amount=100.0, timestamp=2000, zipCode='02115'}
Generated Transaction: DetailedTransaction{accountId=1, amount=750.0, timestamp=3000, zipCode='01003'}
Generated Transaction: DetailedTransaction{accountId=3, amount=0.25, timestamp=4000, zipCode='78712'}
Generated Transaction: DetailedTransaction{accountId=4, amount=200.0, timestamp=5000, zipCode='02115'}
Generated Transaction: DetailedTransaction{accountId=3, amount=800.0, timestamp=6000, zipCode='78712'}
Generated Transaction: DetailedTransaction{accountId=5, amount=150.0, timestamp=7000, zipCode='01003'}
Generated Transaction: DetailedTransaction{accountId=1, amount=0.75, timestamp=8000, zipCode='01003'}
Generated Transaction: DetailedTransaction{accountId=1, amount=900.0, timestamp=9000, zipCode='01003'}
```

### Random Test Source Output
```
Random Transaction: DetailedTransaction{accountId=5, amount=760.72, timestamp=1761593896791, zipCode='01003'}
Random Transaction: DetailedTransaction{accountId=1, amount=475.31, timestamp=1761593897791, zipCode='02115'}
Random Transaction: DetailedTransaction{accountId=1, amount=646.79, timestamp=1761593898791, zipCode='78712'}
Random Transaction: DetailedTransaction{accountId=1, amount=70.44, timestamp=1761593899791, zipCode='02115'}
Random Transaction: DetailedTransaction{accountId=5, amount=728.82, timestamp=1761593900791, zipCode='01003'}
```

### DetailedFraudDetector Output (Task 4)
```
Small transaction detected - Account: 1, Amount: $0.5, Zip: 01003, Timestamp: 1000
FRAUD DETECTED - Account: 1, Small Amount: $0.5, Large Amount: $750.0, Zip: 01003
15:49:21,592 INFO  spendreport.DetailedAlertSink - FRAUD DETECTED - Account: 1, Timestamp: 3000, Zip: 01003, Amount: $750.0

Small transaction detected - Account: 3, Amount: $0.25, Zip: 78712, Timestamp: 4000
FRAUD DETECTED - Account: 3, Small Amount: $0.25, Large Amount: $800.0, Zip: 78712
15:49:27,599 INFO  spendreport.DetailedAlertSink - FRAUD DETECTED - Account: 3, Timestamp: 6000, Zip: 78712, Amount: $800.0

Small transaction detected - Account: 1, Amount: $0.75, Zip: 01003, Timestamp: 8000
FRAUD DETECTED - Account: 1, Small Amount: $0.75, Large Amount: $900.0, Zip: 01003
15:49:33,607 INFO  spendreport.DetailedAlertSink - FRAUD DETECTED - Account: 1, Timestamp: 9000, Zip: 01003, Amount: $900.0
```

### Complete System Output (Task 5)
```
Small transaction detected - Account: 5, Amount: $0.2259435431079757, Zip: 78712, Timestamp: 1761594965794
FRAUD DETECTED - Account: 5, Small Amount: $0.2259435431079757, Large Amount: $980.0995459896936, Zip: 78712
15:56:13,823 INFO  spendreport.DetailedAlertSink - FRAUD DETECTED - Account: 5, Timestamp: 1761594973794, Zip: 78712, Amount: $980.0995459896936
```

---

## Fraud Detection Logic

### Current Implementation (Original)
- **Small Transaction**: < $1.00
- **Large Transaction**: ≥ $500.00
- **Time Window**: 1 minute
- **Pattern**: Small transaction followed by large transaction

### Enhanced Implementation (Tasks 1-3)
- **Small Transaction**: < $10.00 (from random source)
- **Large Transaction**: ≥ $500.00
- **Time Window**: 1 minute
- **Pattern**: Small transaction followed by large transaction **in the same zip code**

---

## Testing Strategy

### 1. Hand-crafted Data Testing
- **Purpose**: Debug fraud detection logic with known patterns
- **Advantage**: Guaranteed fraud scenarios
- **Usage**: Use `DetailedTransactionSourceTestJob` for debugging

### 2. Random Data Testing
- **Purpose**: Test with realistic random data
- **Advantage**: Natural transaction distribution
- **Usage**: Use `DetailedTransactionSourceRandomTestJob` for simulation

### 3. Expected Alert Frequency
- **Hand-crafted**: 3 guaranteed fraud alerts
- **Random**: Rare but should occur within 2 minutes
- **Parameters**: Small transactions are rare with uniform (0, 1000] distribution

---

## Troubleshooting

### Common Issues
1. **No Alerts Generated**: Check if small transactions (< $10) are being generated
2. **Compilation Errors**: Ensure Java 25 is installed and Maven dependencies are resolved
3. **Runtime Warnings**: Use the VM option `--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED`

### Debug Commands
```bash
# Check Java version
java -version

# Check Maven dependencies
mvn dependency:tree

# Run with debug logging
mvn exec:java -Dexec.mainClass="spendreport.FraudDetectionJob" -X
```

---

## Next Steps

The project is ready for the remaining tasks:
- **Task 4**: Create DetailedFraudDetector with zip code logic
- **Task 5**: Create DetailedFraudDetectionJob using new classes

---

## Technical Notes

### Dependencies
- Apache Flink 1.17.2 (DataStream API)
- slf4j (logging facade)
- Log4j2 (logging implementation)
- Maven (build management)

### Development Environment
- Java 25 (OpenJDK)
- Maven 3.x
- IntelliJ IDEA IDE
- Linux environment

### Code Quality
- All classes follow Java naming conventions
- Comprehensive JavaDoc documentation
- Proper error handling and null safety
- Clean, maintainable code structure

---

**Project Status**: All Tasks Complete ✅  
**Tasks Completed**: 1, 2, 3, 4, 5  
**Last Updated**: October 27, 2025
