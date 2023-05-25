package com.my.xiaozhang.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.xiaozhang.model.domain.ResultMessage;

import java.util.Date;

public class MessageUtils {
    public static String getMessage(boolean isSystemMessage, String fromName,  String date,Object message){
        try {
            ResultMessage result = new ResultMessage();
            result.setSystem(isSystemMessage);
            result.setMessage(message);
            result.setNowTime(date);
            if (fromName!=null){
                result.setFromName(fromName);
            }
            //把字符串转成json格式的字符串
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(result);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
}

