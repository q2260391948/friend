package com.my.xiaozhang.Enum;

/**
 * 集合状态枚举
 */
public enum ListTypeEnum {

    MIXEECAE(0, "中英文混合"),
    CHINESE(1, "纯中文"),
    ENGLISH(2, "纯英文"),
    ENGLISHAOTHER(3, "英文和其他字符");
    private int value;

    private String text;

    public static ListTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        //values():返回所有枚举类对象构成的数组
        ListTypeEnum[] values = ListTypeEnum.values();
        //遍历这个数组
        for (ListTypeEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    ListTypeEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
