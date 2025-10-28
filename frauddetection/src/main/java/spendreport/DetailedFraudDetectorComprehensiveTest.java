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

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;


public class DetailedFraudDetectorComprehensiveTest {
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        DetailedTransaction[] testTransactions = {
            new DetailedTransaction(1L, 5.0, 1000L, "01003"),
            
            new DetailedTransaction(2L, 100.0, 2000L, "02115"),
            
            new DetailedTransaction(1L, 750.0, 3000L, "01003"),
            
            new DetailedTransaction(3L, 2.0, 4000L, "78712"),
            
            new DetailedTransaction(4L, 200.0, 5000L, "02115"),
            
            new DetailedTransaction(3L, 800.0, 6000L, "78712"),
            
            new DetailedTransaction(5L, 1.0, 7000L, "01003"),
            
            new DetailedTransaction(5L, 600.0, 8000L, "02115"),
            
            new DetailedTransaction(6L, 3.0, 9000L, "78712"),
            
            new DetailedTransaction(7L, 700.0, 10000L, "78712"),
            
            new DetailedTransaction(8L, 0.5, 11000L, "02115"),
            
            new DetailedTransaction(8L, 900.0, 12000L, "02115"),
            
            new DetailedTransaction(1L, 0.05, 1005L, "01003"),  
            new DetailedTransaction(2L, 50.0, 5000L, "02115"),  
            new DetailedTransaction(1L, 1000.0, 55000L, "01003") 
        };
        
        DataStream<DetailedTransaction> transactions = env.fromElements(testTransactions)
            .name("test-transactions");
        
        DataStream<DetailedAlert> alerts = transactions
            .keyBy(DetailedTransaction::getAccountId)
            .process(new DetailedFraudDetector())
            .name("fraud-detector-test");
        
        transactions
            .addSink(new SinkFunction<DetailedTransaction>() {
                @Override
                public void invoke(DetailedTransaction transaction, Context context) throws Exception {
                    System.out.println("=== TRANSACTION ===");
                    System.out.println("Account: " + transaction.getAccountId());
                    System.out.println("Amount: $" + transaction.getAmount());
                    System.out.println("Zip: " + transaction.getZipCode());
                    System.out.println("Timestamp: " + transaction.getTimestamp());
                    System.out.println("==================");
                }
            })
            .name("print-transactions");
        
        alerts
            .addSink(new SinkFunction<DetailedAlert>() {
                @Override
                public void invoke(DetailedAlert alert, Context context) throws Exception {
                    System.out.println("\n🚨 FRAUD ALERT DETECTED! 🚨");
                    System.out.println("Account: " + alert.getAccountId());
                    System.out.println("Amount: $" + alert.getAmount());
                    System.out.println("Zip: " + alert.getZipCode());
                    System.out.println("Timestamp: " + alert.getTimestamp());
                    System.out.println("========================\n");
                }
            })
            .name("print-alerts");
        
        System.out.println("Starting DetailedFraudDetector Comprehensive Test...");
        System.out.println("Expected: 4 fraud alerts should be generated");
        System.out.println("1. Account 1: $5.00 -> $750.00 in zip 01003");
        System.out.println("2. Account 3: $2.00 -> $800.00 in zip 78712");
        System.out.println("3. Account 8: $0.50 -> $900.00 in zip 02115");
        System.out.println("4. Account 1: $0.05 -> $1000.00 in zip 01003 (Assignment example)");
        System.out.println("\nRunning test...\n");
        
        env.execute("Detailed Fraud Detector Comprehensive Test");
    }
}
