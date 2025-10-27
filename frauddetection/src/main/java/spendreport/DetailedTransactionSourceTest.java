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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * DetailedTransactionSourceTest class for testing with hand-crafted static data.
 * This class generates a predefined set of transactions designed to trigger fraud alerts
 * for debugging purposes before using the random DetailedTransactionSource.
 * 
 * Test scenario: Account 1 has a small transaction followed by a large transaction in the same zip code.
 */
public class DetailedTransactionSourceTest extends RichSourceFunction<DetailedTransaction> {
    
    private static final long serialVersionUID = 1L;
    
    // Predefined test transactions designed to trigger fraud alerts
    private static final List<DetailedTransaction> TEST_TRANSACTIONS = Arrays.asList(
        // Account 1: Small transaction in zip 01003
        new DetailedTransaction(1L, 0.50, 1000L, "01003"),
        
        // Account 2: Normal transaction
        new DetailedTransaction(2L, 100.0, 2000L, "02115"),
        
        // Account 1: Large transaction in same zip 01003 (should trigger fraud alert)
        new DetailedTransaction(1L, 750.0, 3000L, "01003"),
        
        // Account 3: Small transaction in zip 78712
        new DetailedTransaction(3L, 0.25, 4000L, "78712"),
        
        // Account 4: Normal transaction
        new DetailedTransaction(4L, 200.0, 5000L, "02115"),
        
        // Account 3: Large transaction in same zip 78712 (should trigger fraud alert)
        new DetailedTransaction(3L, 800.0, 6000L, "78712"),
        
        // Account 5: Normal transaction
        new DetailedTransaction(5L, 150.0, 7000L, "01003"),
        
        // Account 1: Another small transaction
        new DetailedTransaction(1L, 0.75, 8000L, "01003"),
        
        // Account 1: Another large transaction in same zip (should trigger fraud alert)
        new DetailedTransaction(1L, 900.0, 9000L, "01003")
    );
    
    private volatile boolean isRunning = true;
    private Iterator<DetailedTransaction> transactionIterator;
    
    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        transactionIterator = TEST_TRANSACTIONS.iterator();
    }
    
    @Override
    public void run(SourceFunction.SourceContext<DetailedTransaction> ctx) throws Exception {
        while (isRunning && transactionIterator.hasNext()) {
            DetailedTransaction transaction = transactionIterator.next();
            ctx.collect(transaction);
            
            // Sleep for 2 seconds between transactions for easier observation
            Thread.sleep(2000);
        }
        
        // If we've exhausted the test data, keep running but emit no more transactions
        // This allows the fraud detection logic to complete processing
        while (isRunning) {
            Thread.sleep(1000);
        }
    }
    
    @Override
    public void cancel() {
        isRunning = false;
    }
}
