package com.example.lixudong.days;

/**
 * Created by asus on 2016/12/16.
 */

import java.io.Serializable;
public class whichday implements Serializable {
    public String title;
    public String days;
    public int tag;
    public int beforeORafter;
    public String Remind_str;
    public String check_str;

    public whichday(int tag, int beforeORafter, String title, String days) {
        this.tag = tag;
        this.beforeORafter = beforeORafter;
        this.title = title;
        this.days = days;
        if (beforeORafter == R.mipmap.before) {  //这里要改》》》》》》
            this.check_str = "已有";
            this.Remind_str = "那天的一切你还记得吗？";
        } else {
            this.check_str = "还有";
            if (this.tag == R.mipmap.icon_birthday) {
                this.Remind_str = "给TA准备个生日惊喜吧！";
            } else if (this.tag == R.mipmap.icon_study || this.tag == R.mipmap.icon_work) {
                this.Remind_str = "赶紧动手！时间迫在眉睫！";
            } else {
                this.Remind_str = "不要忘了这件事喔(*^__^*) ";
            }
        }
    }
    public String getTitle() {
        return this.title;
    }
    public int getTag() {
        return this.tag;
    }
    public String getRemind_str() {
        return this.Remind_str;
    }
    public String getDays() {
        return this.days;
    }
    public String getCheck_str() {
        return this.check_str;
    }
}
