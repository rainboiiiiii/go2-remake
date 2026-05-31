package com.go2super.packet;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartListener;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.service.LoginService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.util.*;

public class PacketRouter {

    private static PacketRouter instance;

    private LinkedList<SmartListener> smartListeners = new LinkedList<>();
    private Map<Integer, Class<? extends Packet>> packetsMap = new HashMap<>();

    public PacketRouter() {
        instance = this;
    }

    public void broadcast(Packet packet, User...excludes) {

        List<LoggedGameUser> gameUsers = LoginService.getInstance().getGameUsers();

        main: for(LoggedGameUser user : gameUsers) {
            for(User exclude : excludes)
                if(exclude.getGuid() == user.getGuid())
                    continue main;
            user.getSmartServer().send(packet);
        }

    }


    @SneakyThrows
    public void fireEvent(Packet packet) {

        for(SmartListener listener : smartListeners)
            if (packet.getClass().isAssignableFrom(listener.getPacketProcessor())) {
                BotLogger.packet("🗲 INVOKE " + listener.getPacketMethod().getDeclaringClass().getName() + "#" + listener.getPacketMethod().getName());
                listener.getPacketMethod().invoke(listener.getInstance(), packet);
                return;
            }

    }

    @SneakyThrows
    public void sendPacket(Packet packet) {

        Go2Buffer go2Buffer = packet.unmap();
        packet.getSmartServer().send(go2Buffer);
        BotLogger.packet("↗ SEND " + go2Buffer.getType() + "(" + packet.getClass().getSimpleName() + ")");

    }

    @SneakyThrows
    public void craftPackets() {

        Reflections reflections = new Reflections("com.go2super.packet");
        Set<Class<? extends Packet>> classes = reflections.getSubTypesOf(Packet.class);

        for(Class<? extends Packet> classPacket : classes) {

            Field[] fields = FieldUtils.getAllFields(classPacket);

            int type = -1;

            for(Field field : fields)
                if(field.getName().equals("TYPE"))
                    type = field.getInt(classPacket.newInstance());

            if(type >= 0)
                packetsMap.put(type, classPacket);

        }

    }

    @SneakyThrows
    public void craftListeners() {

        Reflections reflections = new Reflections("com.go2super.listener");
        Set<Class<? extends PacketListener>> classes = reflections.getSubTypesOf(PacketListener.class);

        for(Class<? extends PacketListener> classListener : classes)
            for (Method declaredMethod : classListener.getDeclaredMethods())
                if(declaredMethod.isAnnotationPresent(PacketProcessor.class))
                    for (Parameter parameter : declaredMethod.getParameters()) {
                        if (parameter.getType().getSuperclass() != null && parameter.getType().getSuperclass().isAssignableFrom(Packet.class)) {
                            PacketListener packetListener = BeanUtils.instantiateClass(classListener);
                            Class<? extends Packet> packetClass = (Class<? extends Packet>) parameter.getType();
                            smartListeners.add(SmartListener.of(packetClass, declaredMethod, packetListener));
                        }
                    }

    }

    @SneakyThrows
    public void playPacket(int size, int type, Go2Buffer buffer, Socket socket, SmartServer smartServer) {

        for(Map.Entry<Integer, Class<? extends Packet>> entry : packetsMap.entrySet())
            if(entry.getKey() == type) {
                BotLogger.packet("↙ FOUND " + type + " (" + entry.getValue().getSimpleName() + ")");
                Packet packet = entry.getValue().newInstance();
                try {
                    fireEvent(packet.map(size, type, buffer, socket, smartServer));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

        BotLogger.packet("⚠ NOT FOUND " + type);

    }

    public boolean contains(Method method, Method...interfaceMethods) {

        for(Method cache : interfaceMethods)
            if(cache.getName().equals(method.getName()))
                return true;

        return false;

    }

    public static PacketRouter getInstance() {
        return instance;
    }

}
