package org.cyy.bean;

/**
 * User对应一个学院配置文件，某学院的一个老师的账号可以查看整个学院学生的信息
 * username:该学院某位老师用户名
 * password:密码
 * collegeNo:学院标号，唯一标识，collegeNo对应学院配置文件的命名，（这是由学校系统决定的，抓包时的请求参数）
 * cookie：cookie有效期为一周，过期后就要重新登录获得
 * authorization:验证，通过cookie发送几次请求可以得到，查询签到信息需要他
 */
public class User {
    private String username;
    private String password;
    private String collegeNo;
    private String cookie;
    private String authorization;

    public String getAuthorization() {
        return this.authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public User(String username, String password, String collegeNo, String cookie) {
        this.username = username;
        this.password = password;
        this.collegeNo = collegeNo;
        this.cookie = cookie;
    }

    public User() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCollegeNo() {
        return this.collegeNo;
    }

    public void setCollegeNo(String collegeNo) {
        this.collegeNo = collegeNo;
    }

    public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String toString() {
        return "User{username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", collegeNo='" + this.collegeNo + '\'' + ", cookie='" + this.cookie + '\'' + '}';
    }
}
