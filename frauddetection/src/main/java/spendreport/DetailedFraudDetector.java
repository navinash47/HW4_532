/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

/**
 * DetailedFraudDetector class that implements enhanced fraud detection logic.
 * This detector identifies fraudulent transactions based on the pattern:
 * - Small transaction (< $10) followed by large transaction (>= $500)
 * - Both transactions from the same account
 * - Both transactions in the same zip code
 * - Within a 1-minute time window
 */
public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {

    private static final long serialVersionUID = 1L;

    // Fraud detection thresholds
    private static final double SMALL_AMOUNT = 10.00;  // < $10
    private static final double LARGE_AMOUNT = 500.00; // >= $500
    private static final long ONE_MINUTE = 60 * 1000;  // 1 minute in milliseconds

    // State to track small transactions
    private transient ValueState<Boolean> flagState;
    private transient ValueState<String> zipCodeState;
    private transient ValueState<Double> amountState;
    private transient ValueState<Long> timerState;

    @Override
    public void open(Configuration parameters) {
        // Initialize state for tracking small transactions
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
            "flag",
            Boolean.class);
        flagState = getRuntimeContext().getState(flagDescriptor);

        ValueStateDescriptor<String> zipCodeDescriptor = new ValueStateDescriptor<>(
            "zipCode",
            String.class);
        zipCodeState = getRuntimeContext().getState(zipCodeDescriptor);

        ValueStateDescriptor<Double> amountDescriptor = new ValueStateDescriptor<>(
            "amount",
            Double.class);
        amountState = getRuntimeContext().getState(amountDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
            "timer-state",
            Long.class);
        timerState = getRuntimeContext().getState(timerDescriptor);
    }

    @Override
    public void processElement(
            DetailedTransaction transaction,
            Context context,
            Collector<DetailedAlert> collector) throws Exception {

        // Get current transaction details
        double amount = transaction.getAmount();
        String zipCode = transaction.getZipCode();
        long timestamp = transaction.getTimestamp();
        Long accountId = transaction.getAccountId();

        // Get current state
        Boolean flag = flagState.value();
        String storedZipCode = zipCodeState.value();
        Double storedAmount = amountState.value();

        // Check if this is a small transaction
        if (amount < SMALL_AMOUNT) {
            // Set flag and store transaction details
            flagState.update(true);
            zipCodeState.update(zipCode);
            amountState.update(amount);
            
            // Set timer for 1 minute from now
            long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
            context.timerService().registerProcessingTimeTimer(timer);
            timerState.update(timer);
            
            System.out.println("Small transaction detected - Account: " + accountId + 
                             ", Amount: $" + amount + ", Zip: " + zipCode + 
                             ", Timestamp: " + timestamp);
        }
        
        // Check if this is a large transaction
        if (amount >= LARGE_AMOUNT) {
            // Check if we had a small transaction before this from the same account
            if (flag != null && flag && zipCode.equals(storedZipCode)) {
                // This is fraudulent! Small transaction followed by large transaction in same zip
                DetailedAlert alert = new DetailedAlert();
                alert.setAccountId(accountId);
                alert.setTimestamp(timestamp);
                alert.setZipCode(zipCode);
                alert.setAmount(amount);
                
                collector.collect(alert);
                
                System.out.println("FRAUD DETECTED - Account: " + accountId + 
                                 ", Small Amount: $" + storedAmount + 
                                 ", Large Amount: $" + amount + 
                                 ", Zip: " + zipCode);
                
                // Clear the state and cancel the timer
                clearState(context);
            } else if (flag != null && flag && !zipCode.equals(storedZipCode)) {
                // Small transaction followed by large transaction but different zip codes
                System.out.println("Large transaction in different zip - Account: " + accountId + 
                                 ", Small Zip: " + storedZipCode + 
                                 ", Large Zip: " + zipCode + 
                                 " - NO ALERT");
                
                // Clear the state since we found a large transaction (even if different zip)
                clearState(context);
            }
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) throws Exception {
        // Timer fired - this means we had a small transaction 1 minute ago
        // If we get here, it means no large transaction followed the small one
        // Clear the state since the time window has expired
        System.out.println("Timer expired - no large transaction followed small transaction");
        clearState(ctx);
    }

    /**
     * Helper method to clear all state and cancel timer
     */
    private void clearState(Context context) throws Exception {
        flagState.clear();
        zipCodeState.clear();
        amountState.clear();
        
        Long timer = timerState.value();
        if (timer != null) {
            context.timerService().deleteProcessingTimeTimer(timer);
            timerState.clear();
        }
    }
}
