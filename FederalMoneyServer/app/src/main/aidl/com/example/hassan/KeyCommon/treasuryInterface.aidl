// treasuryInterface.aidl
package com.example.hassan.KeyCommon;


interface treasuryInterface {

     int[] monthlyAvgCash(int aYear);
     int[] dailyCash(int aYear,int aMonth, int aDay, int aNumber);
     String[] getKey();

}
