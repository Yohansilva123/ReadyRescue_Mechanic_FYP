package com.example.readyrescueproject.utils;

import com.example.readyrescueproject.ServiceConfirmActivity;

public class CalculatePrices {
    public static String calculatePrice() {

        int jTime = ServiceConfirmActivity.time[1];

        if (jTime==0){
            jTime = 1;
        }

        int finalPrice = Integer.parseInt(ServiceConfirmActivity.price)*jTime;
        if (finalPrice < Integer.parseInt(ServiceConfirmActivity.basePrice))
            finalPrice = Integer.parseInt(ServiceConfirmActivity.basePrice);

        finalPrice = finalPrice-Integer.parseInt(ServiceConfirmActivity.points);
        finalPrice = finalPrice+Integer.parseInt(ServiceConfirmActivity.addServiceCost);

        ServiceConfirmActivity.priceFinal = String.valueOf(finalPrice);

        return ServiceConfirmActivity.priceFinal;
    }
}
