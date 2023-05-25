package com.my.xiaozhang.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 22603
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultMessage {
    private boolean isSystem;
    private String fromName;
    private String nowTime;
    private Object message;
}
