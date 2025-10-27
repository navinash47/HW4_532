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
 * Test job to verify DetailedFraudDetector functionality.
 * This job uses the hand-crafted test source to verify fraud detection logic.
 */
public class DetailedFraudDetectorTestJob {
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Use the test source with hand-crafted data for debugging
        DataStream<DetailedTransaction> transactions = env
            .addSource(new DetailedTransactionSourceTest())
            .name("detailed-transactions-test");
        
        // Apply fraud detection logic
        DataStream<DetailedAlert> alerts = transactions
            .keyBy(DetailedTransaction::getAccountId)
            .process(new DetailedFraudDetector())
            .name("detailed-fraud-detector");
        
        // Send alerts to DetailedAlertSink
        alerts
            .addSink(new DetailedAlertSink())
            .name("detailed-alert-sink");
        
        env.execute("Detailed Fraud Detector Test");
    }
}
