package com.go2super;

import com.go2super.resources.ResourceManager;
import com.go2super.server.GameLogin;
import com.go2super.server.GameServer;
import com.go2super.service.BattleService;
import com.go2super.service.GameNetworkService;
import com.go2super.service.GalaxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
@SpringBootApplication
public class Go2SuperApplication implements ApplicationListener<ContextRefreshedEvent> {

	public static void main(String[] args) {
		SpringApplication.run(Go2SuperApplication.class, args);
	}
	
	@Value("${spring.application.name}") String name;	
	@Value("${spring.application.version}") String version;
	@Value("${spring.application.restPort}") String restPort;
	@Value("${application.game.transport:both}") String gameTransport;

	@Autowired
    private GalaxyService galaxyService;

	@Autowired
    private BattleService battleService;

	@Autowired
	private GameNetworkService gameNetworkService;

    @Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

        try {

            disableAccessWarnings();

            galaxyService.calculatePositions();
            battleService.setup();

            log.info("Se inicializó Go2Super!");

            if (gameTransport.contains("tcp")) {
                GameLogin login2 = new GameLogin(458, gameNetworkService.getPacketRouter());
                GameLogin login3 = new GameLogin(5050, gameNetworkService.getPacketRouter());
                GameServer game = new GameServer(90, gameNetworkService.getPacketRouter());

                new Thread(login2).start();
                new Thread(login3).start();
                new Thread(game).start();

                log.info("Legacy TCP game transport enabled on ports 458, 5050, and 90");
            } else {
                log.info("Legacy TCP game transport disabled (transport={})", gameTransport);
            }

            if (gameTransport.contains("websocket")) {
                log.info("WebSocket game transport enabled");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public static void disableAccessWarnings() {

        try {

            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);

            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);

        } catch (Exception ignored) {
        }

    }

}
