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

public class DetailedFraudDetectorNoAlertTestJob {
    
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DetailedTransaction testTransaction1 = new DetailedTransaction(1L, 2.0, 1000L, "01003"); 
        DetailedTransaction testTransaction2 = new DetailedTransaction(2L, 501.0, 2000L, "02115"); 
        DetailedTransaction testTransaction3 = new DetailedTransaction(1L, 1000.0, 3000L, "78712"); 
        
        DataStream<DetailedTransaction> transactions = env.fromElements(testTransaction1, testTransaction2, testTransaction3)
            .name("test-transactions-no-alert");

        DataStream<DetailedAlert> alerts = transactions
            .keyBy(DetailedTransaction::getAccountId)
            .process(new DetailedFraudDetector())
            .name("test-fraud-detector-no-alert");

        alerts
            .addSink(new DetailedAlertSink())
            .name("test-alert-sink-no-alert");

        env.execute("Detailed Fraud Detection No Alert Test");
    }
}
