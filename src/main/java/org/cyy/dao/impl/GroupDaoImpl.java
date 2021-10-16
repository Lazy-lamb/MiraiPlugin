package org.cyy.dao.impl;

import org.cyy.bean.MyGroup;
import org.cyy.bean.SignGroup;
import org.cyy.dao.GroupDao;
import org.cyy.utils.WebUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class GroupDaoImpl implements GroupDao {
    /**
     *
     *
     * @return  返回MyGroup实例,null为没有该群配置文件
     */
    public MyGroup getInstanceForFile(String qun){
        MyGroup myGroup = new MyGroup();
        ArrayList<SignGroup> signGroups = new ArrayList<>();
        Properties prop = WebUtils.loadProperties("group/"+qun+".properties");
        //如果文件不存在直接返回null
        if(prop == null){
            return null;
        }else {
            String num = prop.getProperty("num");
            int intNum = Integer.parseInt(num);
            //初始化num属性
            myGroup.setNum(intNum);
            //初始化signGroupList属性
            for (int i = 1; i <= intNum; i++) {
                SignGroup signGroup = new SignGroup();
                signGroup.setSpecialtyNo((prop.getProperty("specialtyNo" + i) == null) ? "" : prop.getProperty("specialtyNo" + i));
                signGroup.setSpeGrade((prop.getProperty("speGrade" + i) == null) ? "" : prop.getProperty("speGrade" + i));
                signGroup.setClassNo((prop.getProperty("classNo" + i) == null) ? "" : prop.getProperty("classNo" + i));
                signGroup.setCollegeNo((prop.getProperty("collegeNo") == null) ? "" : prop.getProperty("collegeNo"));
                signGroups.add(signGroup);
            }
            myGroup.setSignGroupList(signGroups);
            return myGroup;
        }
    }

    @Override
    public String queryCollegeNo(String qun) {
        String fileName = "group/"+qun+".properties";
        InputStream is = null;
        String collegeNo = null;
        try {
            is = new FileInputStream(fileName);
            Properties prop = new Properties();
            prop.load(is);
            collegeNo = prop.getProperty("collegeNo");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return collegeNo;
    }
}
