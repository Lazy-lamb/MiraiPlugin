package org.cyy.dao.impl;

import org.cyy.utils.MyConnection;
import org.cyy.bean.User;
import org.cyy.dao.UserDao;
import org.cyy.utils.WebUtils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDaoImpl implements UserDao {
    @Override
    public User getUserByFile(String college) {
        User user = new User();
        Properties properties = WebUtils.loadProperties("college/" + college + ".properties");
        //如果配置信息为空直接返回null
        if (properties == null) {
            return null;
        } else {
            user.setCookie(properties.getProperty("cookie"));
            user.setUsername(properties.getProperty("username"));
            user.setPassword(properties.getProperty("password"));
            user.setCollegeNo(college);
            try {
                String authorization = this.getAuthorization(user);
                user.setAuthorization(authorization);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return user;
        }
    }

    @Override
    public Map<String, String> getUsernameAndPassword(String collegeNo) {
        if (!WebUtils.collegeIsOk(collegeNo, 0)) {
            //如果文件不存在
            return null;
        }
        HashMap<String, String> map = null;
        //加载文件,前面已经判断文件是否存在，所以这里文件一定存在
        Properties prop = WebUtils.loadProperties("college/" + collegeNo + ".properties");
        //读取用户名和密码
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");
        //存入map
        map = new HashMap();
        map.put("txtUid", username);
        map.put("txtPwd", password);
        return map;
    }

    @Override
    public boolean updateCookie(String collegeNo, String cookie) {
        FileOutputStream out = null;
        try {
            if (!WebUtils.collegeIsOk(collegeNo, 0)) {
                return false;
            }
            //读取文件
            Properties p = WebUtils.loadProperties("college/" + collegeNo + ".properties");
            //System.out.println(cookie);
            //设置cookie保存文件
            p.setProperty("cookie", cookie);
            out = new FileOutputStream("college/" + collegeNo + ".properties");
            p.store(out, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            WebUtils.closeResource(null, out);
        }
    }


    /**
     * 以下几个方法耦合度太高，已经不想改了，模拟浏览器向学校后台发送请求，获取之后要用的请求验证
     *
     * @param user
     * @return 没有异常情况(cookie未失效)则正常返回验证
     * @throws Exception
     */
    private String getAuthorization(User user) throws Exception {
        String authorization;
        String result = signGetOne(user);
        authorization = "Bearer " + result;
        return authorization;
    }

    /**
     * 第一次get得到post的url
     */
    private String signGetOne(User user) throws Exception {
        String code = "";
        MyConnection mc = new MyConnection();
        HttpURLConnection connection = null;
        try {
            connection = mc.setUrl("http://spcp.cdnu.zovecenter.com/Web/Account/ChooseSys").setCookie(user.getCookie()).set302(false).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String postUrl = connection.getHeaderField("Location");     //获取头中的location项的信息
        //正则取出结果中的id
        String regex = "OpenId=(.*+)";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(postUrl);
        while (matcher.find()) {
            code = matcher.group(1);
        }
        connection.disconnect();
        if (code == "") {
            return "错误";
        }
        //System.out.println("code="+code);
        //拼接后传入post
        //System.out.println("code="+code);
        return signPostOne(user, "grant_type=password&username=&password=", "http://spcp.cdnu.zovecenter.com/FYPhone/PhoneApi/api/Account/Login?code=" + code);
    }

    /**
     * 第一次post得到token接口
     *
     * @param url
     */
    private String signPostOne(User user, String param, String url) throws Exception {
        //post携带的参数
        MyConnection mc = new MyConnection();
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String code = "";

        HttpURLConnection connection = mc.setUrl(url).setInput(true).setOutput(true).setMethod("POST").setCookie(user.getCookie()).build();
        //设置不缓存（不知道有啥用）
        connection.setUseCaches(false);
        out = new OutputStreamWriter(connection.getOutputStream());
        out.write(param);       //向输出流写入参数
        out.flush();
        //System.out.println("cookie = "+user.getCookie()+"yz="+user.getAuthorization());
        //System.out.println("connectionCode=" + connection.getResponseCode());
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        String result = "";
        while ((line = in.readLine()) != null) {
            result += line;
        }
        connection.disconnect();
        //正则匹配结果
        Pattern p = Pattern.compile("\"refresh_token\":\"(.*?)\"");
        Matcher matcher = p.matcher(result);
        while (matcher.find()) {
            code += matcher.group(1);
        }
        WebUtils.closeResource(in, out);

        //进入第二次post
        //System.out.println("2.post:"+code);
        return signPostTwo(user, "grant_type=refresh_token&refresh_token=" + code, "http://spcp.cdnu.zovecenter.com/FYPhone/PhoneApi/api/Account/Login?userType=T&collegeNo=" + user.getCollegeNo());
    }

    /**
     * 第二次post获得验证
     *
     * @param url
     * @param param
     */
    private String signPostTwo(User user, String param, String url) throws Exception {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        MyConnection mc = new MyConnection();
        String code = "";

        HttpURLConnection connection = mc.setUrl(url).setInput(true).setOutput(true).setMethod("POST").setCookie(user.getCookie()).build();

        out = new OutputStreamWriter(connection.getOutputStream());
        out.write(param);       //向输出流写入参数
        out.flush();

        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        String result = "";
        while ((line = in.readLine()) != null) {
            result += line;
        }

        //正则匹配结果
        Pattern p = Pattern.compile("\"access_token\":\"(.*?)\"");
        Matcher matcher = p.matcher(result);

        while (matcher.find()) {
            code += matcher.group(1);
        }
        //System.out.println("3.post:"+code);

        WebUtils.closeResource(in, out);

        return code;
    }

}
