package org.cyy;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.cyy.Plugin;
import org.cyy.service.SignService;
import org.cyy.utils.WebUtils;
import org.quartz.*;

import java.io.File;
import java.util.ArrayList;

public class MyJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //service层
        SignService signService = new SignService();
        File FileGroup = new File("group");
        File[] files = FileGroup.listFiles();

        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            String[] id = name.split("\\.");
            //System.out.println(id[0]);
            Group group = Plugin.MY_BOT.getGroup(Long.parseLong(id[0]));
            if(group != null){
                //group.sendMessage("正在测试功能：主动发送未签到消息");
                //判断群是否授权
                if(WebUtils.groupIsOk(group.getId(),0)) {
                    ArrayList<Message> messages = signService.makeSignMessages(group);
                    for (int j = 0; j < messages.size(); j++) {
                        MessageReceipt<Group> groupMessageReceipt = group.sendMessage(messages.get(j));
                        groupMessageReceipt.recallIn(50*1000*60);
                        //System.out.println(messages.get(j));
                    }
                }else {
                    group.sendMessage("本群未授权");
                    //System.out.println("本群未授权");
                }
            }
            /*try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

    }
}
