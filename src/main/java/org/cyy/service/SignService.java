package org.cyy.service;

import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.*;
import org.cyy.Plugin;
import org.cyy.utils.MyConnection;
import org.cyy.bean.MyGroup;
import org.cyy.bean.SignGroup;
import org.cyy.bean.User;
import org.cyy.dao.GroupDao;
import org.cyy.dao.UserDao;
import org.cyy.dao.impl.GroupDaoImpl;
import org.cyy.dao.impl.UserDaoImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignService {

    private GroupDao groupDao = new GroupDaoImpl();
    private UserDao userDao = new UserDaoImpl();

    private String url = "http://spcp.cdnu.zovecenter.com/FYPhone/PhoneApi/api/Health/GetDCStuReseltWebList?topId=&inputDate=&stuId=&stuName=&status=%s&collegeNo=%s&specialtyNo=%s&classNo=%s&speGrade=%s&fdyName=&pageIndex=";
    /**
     * 制作签到信息
     * @param group
     * @return 返回多条信息，如果一切正常则返回未签到的信息
     */
    public ArrayList<Message> makeSignMessages(Group group){
        ArrayList<Message> messages = new ArrayList<>();
        //通过配置文件获取被签到的群的bean对象，后续可能会用数据库代替
        MyGroup myGroup = groupDao.getInstanceForFile(String.valueOf(group.getId()));
        //如果myGroup为空则表明配置不正确，直接返回配置有误的信息
        if(myGroup != null) {
            //通过配置文件获取user bean对象，后续可能会用数据库代替
            User user = userDao.getUserByFile(myGroup.getSignGroupList().get(0).getCollegeNo());
            if(user == null){
                //如果user为空，则表面没有配置文件
                PlainText plainText = new PlainText("该学院没有配置文件");
                messages.add(plainText);
            }else if(user.getAuthorization() == null){
                //如果验证为空则表明cookie失效
                PlainText plainText = new PlainText("发生意外，再试一次吧");
                messages.add(plainText);
            }else {

                MessageChain msg;                                               //信息链
                At at;                                                           //at信息
                MessageChainBuilder buider = new MessageChainBuilder();         //信息构造

                ArrayList<String> list = new ArrayList();
                boolean judge = getAllName(user, myGroup, list,2);
                int count = 0;

                if (list.size() != 0 && judge) {
                    buider = buider.append("同学们签到啦！！！");
                    ContactList<NormalMember> members = group.getMembers();
                    StringBuffer unSuccessAtName = new StringBuffer();   //未成功at的人
                    int groupSize = members.getSize();  //群成员人数

                    //对每个未签到的姓名进行遍历
                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i);
                        ArrayList<NormalMember> atAimList = new ArrayList<>();;
                        String qqName = null;
                        for (NormalMember normalMember: members) {
                            qqName = normalMember.getNameCard();
                            if (qqName.contains(name)) {
                                atAimList.add(normalMember);
                            }
                        }

                        if(atAimList!=null && atAimList.size() == 0){
                            //如果at目标集合长度为0，则表明该人未成功@
                            unSuccessAtName.append(name+" ");
                        }else if(atAimList!=null && atAimList.size() > 1){
                            //如果at目标大于1，则为重名或者名字包含关系（陈红，陈红燕，“陈红燕”包含了“陈红”，contain判断都会为true）这类情况
                            ArrayList<String> signList = new ArrayList<>();
                            getAllName(user,myGroup,signList,1);
                            signList.addAll(list);
                            //已经签到的集合加上未签到的为总的
                            //未签到集合与签到集合合并为总名称表

                            NormalMember normalMember = selectTrueAtAim(qqName, atAimList, signList);
                            at = new At(normalMember.getId());
                            buider = buider.append(at);
                            count++;
                        }else if(atAimList != null){

                            at = new At(atAimList.get(0).getId());
                            buider = buider.append(at);
                            count++;
                        }
                    }

                    msg = buider.build();
                    messages.add(msg);
                    PlainText plainText = new PlainText("未签到总人数:" + list.size() + ",成功@人数:" + count + (list.size() == count ? "":("，@失败："+unSuccessAtName.toString())));
                    messages.add(plainText);
                } else if (list.size() == 0 && judge) {
                    PlainText plainText = new PlainText("今日已全部签到");
                    messages.add(plainText);
                } else {
                    //judge为false，则表面cookie已经过期要重新登录
                    noticeUser();
                    PlainText plainText = new PlainText("cookie失效，班级负责人将会处理");

                    messages.add(plainText);
                }
            }
        }else {
            PlainText plainText = new PlainText("本群配置信息为空或者未正确配置！");
            messages.add(plainText);
        }
        return messages;
    }

    //选择出正确的at对象
    private NormalMember selectTrueAtAim(String qqName, ArrayList<NormalMember> atAimList, ArrayList<String> allList) {

        //筛选出含有特定子串（名字）的字符串（名字）
        for (int i = 0; i < allList.size(); i++) {
            if(!qqName.contains(allList.get(i))){
                allList.remove(i);
            }
        }
        //对其进行排序
        Collections.sort(allList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        for (int i = 0; i < allList.size(); i++) {
            System.out.println(allList.get(i));
        }
        //对atAimList进行筛选
        for (int i = 0; i < allList.size(); i++) {
            for (int j = 0; j < atAimList.size(); j++) {
                if(atAimList.get(j).getNameCard().contains(allList.get(i))){
                    atAimList.remove(j);
                    break;
                }
            }
        }
        return atAimList.get(0);
    }
    //获取总页数
    private int getAllPage(User user,MyGroup myGroup,int i,int status){
        String allPage = null;
        ArrayList<SignGroup> group = myGroup.getSignGroupList();
        String tempUrl = String.format(url+"1",new Object[]{status,group.get(i).getCollegeNo(),group.get(i).getSpecialtyNo(),group.get(i).getClassNo(),group.get(i).getSpeGrade()});
        //System.out.println(tempUrl);
        BufferedReader in = null;
        try {
            MyConnection mc = new MyConnection();
            HttpURLConnection connection = mc.setUrl(tempUrl).setAuthorization(user.getAuthorization()).build();
            //设置请求头
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            String result = "";
            while((line = in.readLine())!=null){
                result+=line;
            }

            String regex = "\"TotalPages\":(\\d+)";
            Pattern p = Pattern.compile(regex);
            Matcher macher = p.matcher(result);
            while(macher.find()){
                allPage = macher.group(1);
            }
            return Integer.valueOf(allPage);

        } catch (Exception e) {
            return -1;
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //获取姓名表
    private boolean getAllName(User user,MyGroup myGroup, ArrayList<String> array,int status){
        ArrayList<SignGroup> group = myGroup.getSignGroupList();
        for (int i = 0; i < myGroup.getSignGroupList().size(); i++) {

            int allPage = getAllPage(user,myGroup,i,status);
            //System.out.println(allPage);
            if (allPage == -1) {
                return false;
            } else {
                String result;
                String pageUrl;
                String line = "";
                String tempUrl = String.format(url, new Object[]{status, group.get(i).getCollegeNo(),group.get(i).getSpecialtyNo(), group.get(i).getClassNo(), group.get(i).getSpeGrade()});
                BufferedReader in = null;
                MyConnection mc = new MyConnection();
                for (int j = 1; j <= allPage; j++) {
                    result = "";
                    pageUrl = tempUrl + j;
                    try {
                        HttpURLConnection connection = mc.setUrl(pageUrl).setAuthorization(user.getAuthorization()).build();
                        in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));

                        while ((line = in.readLine()) != null) {
                            result += line;
                        }
                        //System.out.println(result);
                        String regex = "\"Name\":\"(.*?)\"";
                        Pattern p = Pattern.compile(regex);
                        Matcher matcher = p.matcher(result);
                        while (matcher.find()) {
                            //String str = new String(matcher.group(1).getBytes(StandardCharsets.UTF_8),"UTF-8");
                            array.add(matcher.group(1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    //通知负责人cookie失效
    private void noticeUser(){
        Group group = Plugin.MY_BOT.getGroup(714538913);
        if(group != null){
            group.sendMessage("验证失效，请@我回复（登陆）进行登陆");
        }
    }
}
