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

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

/**
 * DetailedTransactionSource class that randomly generates DetailedTransaction instances at runtime.
 * This source generates transactions with the following properties:
 * - Account ID: uniformly random from {1, 2, 3, 4, 5}
 * - Timestamp: increments by 1 second for each transaction
 * - Zip Code: uniformly random from {"01003", "02115", "78712"}
 * - Amount: uniformly random from (0, 1000] (exclusive 0, inclusive 1000)
 */
public class DetailedTransactionSource extends RichSourceFunction<DetailedTransaction> {
    
    private static final long serialVersionUID = 1L;
    
    // Account IDs to choose from
    private static final Long[] ACCOUNT_IDS = {1L, 2L, 3L, 4L, 5L};
    
    // Zip codes to choose from
    private static final String[] ZIP_CODES = {"01003", "02115", "78712"};
    
    // Amount range: (0, 1000]
    private static final double MIN_AMOUNT = 0.01; // Small positive amount
    private static final double MAX_AMOUNT = 1000.0;
    
    // Time increment: 1 second = 1000 milliseconds
    private static final long TIME_INCREMENT = 1000L;
    
    private volatile boolean isRunning = true;
    private Random random;
    private long currentTimestamp;
    
    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        random = new Random();
        // Start with current system time
        currentTimestamp = System.currentTimeMillis();
    }
    
    @Override
    public void run(SourceFunction.SourceContext<DetailedTransaction> ctx) throws Exception {
        while (isRunning) {
            // Generate random account ID
            Long accountId = ACCOUNT_IDS[random.nextInt(ACCOUNT_IDS.length)];
            
            // Generate random zip code
            String zipCode = ZIP_CODES[random.nextInt(ZIP_CODES.length)];
            
            // Generate random amount in range (0, 1000]
            double amount = MIN_AMOUNT + (MAX_AMOUNT - MIN_AMOUNT) * random.nextDouble();
            
            // Create DetailedTransaction
            DetailedTransaction transaction = new DetailedTransaction(
                accountId,
                amount,
                currentTimestamp,
                zipCode
            );
            
            // Emit the transaction
            ctx.collect(transaction);
            
            // Increment timestamp by 1 second
            currentTimestamp += TIME_INCREMENT;
            
            // Sleep for 1 second to simulate real-time data
            Thread.sleep(TIME_INCREMENT);
        }
    }
    
    @Override
    public void cancel() {
        isRunning = false;
    }
}
