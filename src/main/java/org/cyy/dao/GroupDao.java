package org.cyy.dao;

import org.cyy.bean.MyGroup;

public interface GroupDao {

    MyGroup getInstanceForFile(String qun);
    String queryCollegeNo(String qun);
}


