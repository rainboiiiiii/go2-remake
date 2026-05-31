package com.go2super.listener;

import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.task.RequestTaskInfoPacket;

public class TaskListener implements PacketListener {

    @PacketProcessor
    public void onTaskInfo(RequestTaskInfoPacket packet) {

        /*ResponseTaskInfoPacket responseTaskInfoPacket = new ResponseTaskInfoPacket();

        responseTaskInfoPacket.setDataLen((short) 29);
        responseTaskInfoPacket.setAwardLen((short) 1);
        responseTaskInfoPacket.setByteArray(new ByteArray(new int[]{0,0,0,0,0,0,0,0,48,-14,19,0,0,0,0,0,-117,108,33,64,1,0,0,}));

        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(153, 0, 0, -1, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(500, 0, 1, 16, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(501, 0, 1, 16, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(502, 0, 1, 16, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(503, 0, 1, 30, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(504, 0, 1, 14, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(505, 0, 1, 8, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(506, 0, 1, 21, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(507, 0, 1, 10, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(509, 0, 1, 8, 1, 1));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(510, -15628, 1, 12, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(511, 23416, 1, 9, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(512, 0, 1, 3, 1, 1));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(513, 0, 1, 3, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(514, 0, 1, 0, 1, 1));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(515, 0, 1, 2, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(516, 0, 1, 28, 1, 1));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(517, 0, 1, 0, 1, 1));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1000, 0, 2, 2, 1, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1001, 3, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1002, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1003, 1, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1004, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1005, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1006, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1007, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1008, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1009, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1010, 0, 2, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-14350, 16407, 1, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(5, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(0, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(9888, 2887, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-28208, 2394, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(12384, 3013, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(23616, 2919, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-10608, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(12384, 3013, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(23616, 2919, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(23648, 2919, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-8144, 16407, 1, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-8640, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(517, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(8656, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-8640, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-16208, -25391, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-8640, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-16208, -25391, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-8640, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-16208, -25391, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(1, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(153, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(12384, 3013, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(153, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(3296, 16935, 1, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(0, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-10608, 2892, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-1839, 16407, 1, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(517, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(0, 0, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-15729, 15733, 0, 0, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(21833, 0, 17, 78, 0, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-31578, 0, 64, 66, 15, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(16960, 15, 64, 66, 15, 0));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(7864, 16005, -51, -52, 76, 62));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-15729, 15989, 10, -41, -93, 60));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(-10486, 15523, -112, -62, 117, 61));
        responseTaskInfoPacket.getTaskInfos().add(new TaskInfo(0, 0, 0, 0, 0, 0));

        packet.getSmartServer().send(responseTaskInfoPacket);*/

    }

}
