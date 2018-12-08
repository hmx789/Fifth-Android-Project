// treasureInterface.aidl
package com.example.hassan.KeyCommon;

// Declare any non-default types here with import statements

interface treasuryInterface {
     int[] monthlyAvgCash(int aYear);
     int[] dailyCash(int aYear,int aMonth, int aDay, int aNumber);
     String[] getKey();

}
