package com.example.mydemo;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2022/4/28 8:54
 * desc：
 */
public class ItemBean {
    public String tag;
    public long maxLength;
    public long currentLength;

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(long maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public String toString() {
        return "ItemBean{" +
                "tag='" + tag + '\'' +
                ", maxLength=" + maxLength +
                ", currentLength=" + currentLength +
                '}';
    }
}
