package org.cyy.bean;

import java.util.ArrayList;

/**
 * num:每个群包含的不同班级数(一些群里不仅有计科技还有数媒)
 * groupId:群号，唯一标识，一个群对应一个配置文件
 * signGroupList:正如num注释，每一个signGroup就是一个专业班级，比如数媒20级，比如计科20级
 */


public class MyGroup {
    private int num;
    private String groupId;
    private ArrayList<SignGroup> signGroupList;

    public MyGroup() {
    }

    public MyGroup(int num, String groupId, ArrayList<SignGroup> signGroupList) {
        this.num = num;
        this.groupId = groupId;
        this.signGroupList = signGroupList;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ArrayList<SignGroup> getSignGroupList() {
        return this.signGroupList;
    }

    public void setSignGroupList(ArrayList<SignGroup> signGroupList) {
        this.signGroupList = signGroupList;
    }

    public int getNum() {
        return this.num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
