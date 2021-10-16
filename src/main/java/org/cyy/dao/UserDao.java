package org.cyy.dao;

import org.cyy.bean.User;

import java.util.Map;

public interface UserDao {
    /**
     * 通过配置文件获取userBean对象
     * @param college
     * @return
     */
    User getUserByFile(String college);
    Map<String,String> getUsernameAndPassword(String collegeNo);
    /**
     * 更新cookie
     * @param collegeNo
     * @param cookie
     * @return 返回true代表更新成功，false代表文件不存在或者存储过程错误
     */
    boolean updateCookie(String collegeNo,String cookie);
}
