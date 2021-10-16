package org.cyy;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSenderOnMessage;
import net.mamoe.mirai.console.command.FriendCommandSenderOnMessage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.cyy.commend.*;
import org.cyy.service.LoginService;
import org.cyy.service.SignService;
import org.cyy.utils.WebUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public final class Plugin extends JavaPlugin {
    public static final Plugin INSTANCE = new Plugin();
    private Map<String,String> map;     //储存登录时必要信息
    public static Bot MY_BOT;           //储存当前bot
    private LoginService loginService = new LoginService(); //service层

    private Plugin() {
        super(new JvmPluginDescriptionBuilder("org.cyy.plugin", "3.0").build());
    }

    @Override
    public void onEnable() {
        //测试信息
        getLogger().info("Plugin loaded!");
        //命令注册
        CommandManager.INSTANCE.registerCommand(MyCommendAdd.INSTANCE, true);   //注册add命令
        CommandManager.INSTANCE.registerCommand(MyCommendDelete.INSTANCE, true);   //注册delete命令
        CommandManager.INSTANCE.registerCommand(MyCommendCheck.INSTANCE, true);   //注册check命令
        CommandManager.INSTANCE.registerCommand(MyCommendCookie.INSTANCE, true);   //注册cookie命令
        CommandManager.INSTANCE.registerCommand(MyCommendCheckFiles.INSTANCE, true);   //注册checkFiles命令
        CommandManager.INSTANCE.registerCommand(MyCommendNotice.INSTANCE, true);   //注册notice命令
        //创建放配置文件的文件夹
        createFile();
        //事件注册
        this.registerEvent();
    }


    /**
     *
     * @author cyy
     * @date 2021/10/1618:17
     * 将特殊消息，mirai自带事件 看作为事件处理，并监听事件（注册事件）
     */
    public void registerEvent(){
        //消息窗口接收并执行管理员命令事件注册
        Listener listenerUser = GlobalEventChannel.INSTANCE.filter(ev -> ev instanceof MessageEvent && (((MessageEvent) ev).getSender().getId() == 1597081640))
                .subscribeAlways(FriendMessageEvent.class, (FriendMessageEvent messageEvent) -> {
            CommandSenderOnMessage commandSender = new FriendCommandSenderOnMessage(messageEvent);
            CommandManager.INSTANCE.executeCommand(commandSender,messageEvent.getMessage(),true);
        });

        //机器人登录事件注册
        Listener listenerBot = GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, (BotOnlineEvent e)->{
            MY_BOT = e.getBot();
            startAutoSign();
        });

        //自动同意加群事件注册
        Listener listenerJoinGroup = GlobalEventChannel.INSTANCE.filter(ev->ev instanceof BotInvitedJoinGroupRequestEvent)
        .subscribeAlways(BotInvitedJoinGroupRequestEvent.class,(BotInvitedJoinGroupRequestEvent joinGroup) ->{
            joinGroup.accept();
        });

        //自动同意加好友事件注册
        Listener listenerAddFriend = GlobalEventChannel.INSTANCE.filter(ev->ev instanceof NewFriendRequestEvent )
                .subscribeAlways(NewFriendRequestEvent.class,(NewFriendRequestEvent addFriend) ->{
            addFriend.accept();
        });

        //催签到事件注册
        Listener listenerSign = GlobalEventChannel.INSTANCE.filter(ev->ev instanceof GroupMessageEvent && ((GroupMessageEvent) ev).getMessage().contentToString().contains("催签到"))
                .subscribeAlways(GroupMessageEvent.class,(GroupMessageEvent e)->{
                    sign(e);
        });

        //账号登录事件注册
        Listener listenerLogin = GlobalEventChannel.INSTANCE.filter(ev->ev instanceof GroupMessageEvent
                        && ((GroupMessageEvent) ev).getMessage().contains(new At(MY_BOT.getId()))
                        && ((GroupMessageEvent) ev).getMessage().contentToString().contains("登录")
                        && ((GroupMessageEvent) ev).getGroup().getId() == 714538913)
                .subscribeAlways(GroupMessageEvent.class,(GroupMessageEvent e)->{
                    sendCodePic(e);
        });
        //获取验证码事件注册
        Listener listenerGetCode = GlobalEventChannel.INSTANCE.filter(ev->ev instanceof GroupMessageEvent
                        && ((GroupMessageEvent) ev).getMessage().contentToString().startsWith("验证码")
                        && ((GroupMessageEvent) ev).getGroup().getId() == 714538913)
                .subscribeAlways(GroupMessageEvent.class,(GroupMessageEvent e)->{
                    receiveCode(e);
        });
    }

    /**
     * 签到功能实现
     * @param e 监听到的信息事件，通过事件可以获取群信息
     */
    private void sign(GroupMessageEvent e){
        //service层
        SignService signService = new SignService();
        Group group = e.getGroup();     //发送催签到指令的qq群

        //*****************************************************************
        //判断群是否授权
        if(WebUtils.groupIsOk(group.getId(),0)) {
            ArrayList<Message> messages = signService.makeSignMessages(group);
            for (int i = 0; i < messages.size(); i++) {
                //System.out.println(messages.get(i).contentToString());
                MessageReceipt<Group> groupMessageReceipt = e.getSubject().sendMessage(messages.get(i));
                groupMessageReceipt.recallIn(50*1000);
            }
        }else {
            e.getSubject().sendMessage("本群未授权");
        }
        //*********************************************************************
    }
    private void sendCodePic(GroupMessageEvent e){
        //1.获取用户名和密码
        map = loginService.getUsernameAndPasswordService(String.valueOf(e.getGroup().getId()));
        if(map == null){
            e.getSubject().sendMessage("该学院没有配置信息！");
        }
        //2.获取html中的重复提交标识
        String reSubmitFlag = loginService.getReSubmitFlag();
        if(reSubmitFlag == null){
            //如果重复码为空则表明c微信验证code出错
            e.getSubject().sendMessage("code出错，联系我更换！");
        }else {
            map.put("ReSubmitFlag", loginService.getReSubmitFlag());
            //3.获取验证码图片向用户发送验证码图片
            byte[] picByte = loginService.getPicByte();
            ExternalResource externalResource = ExternalResource.create(picByte);
            Image image = e.getSubject().uploadImage(externalResource);
            e.getSubject().sendMessage(new PlainText("请回复验证码\n(格式为:\"验证码:xxxx\")\n").plus(image));
            try {
                externalResource.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    private void receiveCode(GroupMessageEvent e){
        //4.接收验证码
        String tempCode = e.getMessage().contentToString().trim();
        String code = tempCode.substring(tempCode.length()-4);
        //System.out.println(code);
        map.put("code",code);
        //5.submit
        boolean submitAns = loginService.submit(map);

        if(!submitAns){
            e.getSubject().sendMessage("登录出错，try again");
        }else{
            e.getSubject().sendMessage("登录成功！");
        }
    }


    private void createFile(){
        File group = new File("group");
        File college = new File("college");

        if(!group.exists()){
            group.mkdir();
        }
        if(!college.exists()){
            college.mkdir();
        }
    }
    private void startAutoSign(){
        // 1、创建调度器Scheduler
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException schedulerException) {
            schedulerException.printStackTrace();
        }
        // 2、创建JobDetail实例，并与PrintWordsJob类绑定(Job执行内容)
        JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
                .withIdentity("job1", "group1").build();
        // 3、创建CronTrigger实例，传入corn表达式
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .startNow()//立即生效
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9,11,12,14 * * ?"))
                .build();

        //4、执行
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            getLogger().info("--------自动催签到任务开始 ! ------------");
            scheduler.start();
        } catch (SchedulerException schedulerException) {
            schedulerException.printStackTrace();
        }
    }

}

