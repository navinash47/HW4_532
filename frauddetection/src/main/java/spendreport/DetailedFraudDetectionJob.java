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

/**
 * DetailedFraudDetectionJob class that implements the complete enhanced fraud detection system.
 * This job integrates all the new classes (DetailedTransaction, DetailedAlert, DetailedTransactionSource, DetailedFraudDetector)
 * to provide location-based fraud detection using zip code information.
 * 
 * The job detects fraudulent transactions based on the pattern:
 * - Small transaction (< $10) followed by large transaction (>= $500)
 * - Both transactions from the same account
 * - Both transactions in the same zip code
 * - Within a 1-minute time window
 */
public class DetailedFraudDetectionJob {
    
    public static void main(String[] args) throws Exception {
        // Create the Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Create data stream from DetailedTransactionSource
        DataStream<DetailedTransaction> transactions = env
            .addSource(new DetailedTransactionSource())
            .name("detailed-transactions");
        
        // Apply fraud detection logic using DetailedFraudDetector
        DataStream<DetailedAlert> alerts = transactions
            .keyBy(DetailedTransaction::getAccountId)
            .process(new DetailedFraudDetector())
            .name("detailed-fraud-detector");
        
        // Send alerts to DetailedAlertSink for logging
        alerts
            .addSink(new DetailedAlertSink())
            .name("detailed-alert-sink");
        
        // Execute the job
        env.execute("Detailed Fraud Detection");
    }
}
