package com.example.reKyc.Utill;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class DateTimeUtility {
    public String otpExpiryTime() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimestamp.getTime());
        calendar.add(Calendar.MINUTE, 10);
        Timestamp futureTimestamp = new Timestamp(calendar.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        String formattedTimestamp = sdf.format(futureTimestamp);
        return formattedTimestamp;
    }

    public Timestamp currentTimestamp()
    {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        return currentTimestamp;
    }

}
