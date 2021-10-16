package org.cyy.service;

import org.cyy.utils.MyConnection;
import org.cyy.dao.impl.GroupDaoImpl;
import org.cyy.dao.impl.UserDaoImpl;
import org.cyy.utils.WebUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//1.获取用户名和密码
//2.获取html中的重复提交标识
//3.获取验证码图片向用户发送验证码图片
//4.接收验证码
//5.submit
public class LoginService {
    public UserDaoImpl userDao = new UserDaoImpl();
    public GroupDaoImpl groupDao = new GroupDaoImpl();

    /**
     * 获取用户名和密码
     * @param qun qq群号
     * @return
     */
    public Map<String,String> getUsernameAndPasswordService(String qun){

        // 1.通过群号查找是哪个学院要登录
        String collegeNo = groupDao.queryCollegeNo(qun);
        // 2.查找到用户名和密码存入map中
        Map<String, String> usernameAndPassword = userDao.getUsernameAndPassword(collegeNo);
        if(usernameAndPassword == null){
            //如果map为null则直接返回
            return null;
        }else {
            usernameAndPassword.put("collegeNo", collegeNo);
            return usernameAndPassword;
        }
    }

    public byte[] getPicByte(){
        MyConnection myConnection = new MyConnection();
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        String url = "http://spcp.cdnu.zovecenter.com/Web/Account/GetLoginVCode?dt="+new Date().getTime();
        try {
            HttpURLConnection connection = myConnection.setUrl(url).setCookie("ASP.NET_SessionId=vwmfm3bjjb3xmydnz45c4tpt").build();
            bis = new BufferedInputStream(connection.getInputStream());
            baos = new ByteArrayOutputStream();

            int len;
            byte[] buf = new byte[1024];
            while((len = bis.read(buf) ) != -1){
                baos.write(buf,0,len);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            WebUtils.closeResource(bis,null);
            if(baos != null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public String getReSubmitFlag() {
        MyConnection myConnection = new MyConnection();
        BufferedReader br = null;
        String rec = null;
        String url = "http://spcp.cdnu.zovecenter.com/Web/Account/TLogin";
        try {
            HttpURLConnection connection = myConnection.setUrl(url).setCookie("ASP.NET_SessionId=vwmfm3bjjb3xmydnz45c4tpt").build();
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            String ans = null;
            while((line = br.readLine())!= null){
                ans+=line;
            }

            String regex = "<input name=\"ReSubmiteFlag\" type=\"hidden\" value=\"(.*?)\" />";
            Pattern p = Pattern.compile(regex);
            Matcher macher = p.matcher(ans);
            while(macher.find()){
                 rec = macher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rec;
    }

    /**
     * 提交登录信息
     * @param map
     * @return 返回302则表示提交成功，反之登录失败
     */
    public boolean submit(Map<String, String> map) {
        MyConnection myConnection = new MyConnection();
        OutputStreamWriter out = null;
        BufferedReader bf = null;
        String res = null;
        String url = "http://spcp.cdnu.zovecenter.com/Web/Account/TLogin";
        String param = null;
        int responseCode = 0;
        try {
            param = "ReSubmitFlag="+map.get("ReSubmitFlag")+"&txtUid="+map.get("txtUid")+"&txtPwd="+map.get("txtPwd")+"&code="+map.get("code");
            //System.out.println("param="+param);

            HttpURLConnection connection = myConnection.setUrl(url).setMethod("POST").setInput(true).setOutput(true).set302(false).setCookie("ASP.NET_SessionId=vwmfm3bjjb3xmydnz45c4tpt").build();
            out = new OutputStreamWriter(connection.getOutputStream());
            out.write(param);       //向输出流写入参数
            out.flush();

            String headerField = connection.getHeaderField("Set-Cookie");
            //System.out.println("setCookie="+headerField);
            String regex = "(.*); expires";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(headerField);
            while (matcher.find()) {
                res = matcher.group(1);
            }
            //System.out.println(res);
            String collegeNo = map.get("collegeNo");
            String cookie = "ASP.NET_SessionId=2qupfjzgo1uzjvtriacqamod;" + res;
            userDao.updateCookie(collegeNo, cookie);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (responseCode==302)?true:false;
    }
}
