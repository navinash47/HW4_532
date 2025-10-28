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

public class DetailedFraudDetectorLogicTest {
    
    public static void main(String[] args) {
        System.out.println("=== DetailedFraudDetector Logic Test ===\n");
        
        System.out.println("Test Case 1: Same zip code - SHOULD generate alert");
        testFraudDetection(
            new DetailedTransaction(1L, 5.0, 1000L, "01003"),  
            new DetailedTransaction(1L, 750.0, 2000L, "01003") 
        );
        
        System.out.println("\nTest Case 2: Different zip code - SHOULD NOT generate alert");
        testFraudDetection(
            new DetailedTransaction(2L, 3.0, 1000L, "01003"),  
            new DetailedTransaction(2L, 600.0, 2000L, "02115") 
        );
        
        System.out.println("\nTest Case 3: Amount too small - SHOULD NOT generate alert");
        testFraudDetection(
            new DetailedTransaction(3L, 2.0, 1000L, "01003"),  
            new DetailedTransaction(3L, 400.0, 2000L, "01003") 
        );
        
        System.out.println("\nTest Case 4: First transaction too large - SHOULD NOT generate alert");
        testFraudDetection(
            new DetailedTransaction(4L, 15.0, 1000L, "01003"), 
            new DetailedTransaction(4L, 600.0, 2000L, "01003") 
        );
        
        System.out.println("\nTest Case 5: Assignment example - SHOULD generate alert");
        System.out.println("  Transaction sequence:");
        System.out.println("  1. Account 1: $0.05, Zip 01003 (small transaction)");
        System.out.println("  2. Account 2: $50, Zip 02115 (medium transaction, different account/zip)");
        System.out.println("  3. Account 1: $1000, Zip 01003 (large transaction, same account/zip as #1)");
        testFraudDetectionSequence(
            new DetailedTransaction(1L, 0.05, 1005L, "01003"),  
            new DetailedTransaction(2L, 50.0, 5000L, "02115"),  
            new DetailedTransaction(1L, 1000.0, 55000L, "01003")
        );
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testFraudDetection(DetailedTransaction small, DetailedTransaction large) {
        System.out.println("  Small: Account " + small.getAccountId() + 
                          ", $" + small.getAmount() + 
                          ", Zip " + small.getZipCode());
        System.out.println("  Large: Account " + large.getAccountId() + 
                          ", $" + large.getAmount() + 
                          ", Zip " + large.getZipCode());
        
        boolean isSmall = small.getAmount() < 10.0;
        boolean isLarge = large.getAmount() >= 500.0;
        boolean sameAccount = small.getAccountId().equals(large.getAccountId());
        boolean sameZip = small.getZipCode().equals(large.getZipCode());
        
        boolean shouldAlert = isSmall && isLarge && sameAccount && sameZip;
        
        System.out.println("  Result: " + (shouldAlert ? "🚨 ALERT" : "✅ No Alert"));
        System.out.println("  Logic: Small=" + isSmall + 
                          ", Large=" + isLarge + 
                          ", SameAccount=" + sameAccount + 
                          ", SameZip=" + sameZip);
    }
    
    private static void testFraudDetectionSequence(DetailedTransaction first, DetailedTransaction second, DetailedTransaction third) {
        System.out.println("  First: Account " + first.getAccountId() + 
                          ", $" + first.getAmount() + 
                          ", Zip " + first.getZipCode());
        System.out.println("  Second: Account " + second.getAccountId() + 
                          ", $" + second.getAmount() + 
                          ", Zip " + second.getZipCode());
        System.out.println("  Third: Account " + third.getAccountId() + 
                          ", $" + third.getAmount() + 
                          ", Zip " + third.getZipCode());
        
        boolean firstIsSmall = first.getAmount() < 10.0;
        boolean thirdIsLarge = third.getAmount() >= 500.0;
        boolean sameAccount = first.getAccountId().equals(third.getAccountId());
        boolean sameZip = first.getZipCode().equals(third.getZipCode());
        
        boolean shouldAlert = firstIsSmall && thirdIsLarge && sameAccount && sameZip;
        
        System.out.println("  Result: " + (shouldAlert ? "🚨 ALERT" : "✅ No Alert"));
        System.out.println("  Logic: FirstSmall=" + firstIsSmall + 
                          ", ThirdLarge=" + thirdIsLarge + 
                          ", SameAccount=" + sameAccount + 
                          ", SameZip=" + sameZip);
        System.out.println("  Note: Second transaction (Account " + second.getAccountId() + 
                          ") is ignored as it's a different account");
    }
}
