package com.go2super.logger;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.ansi;

public class BotLogger {

    public static final boolean debug = false;
    public static final boolean packetDebug = false;

    private static final String pattern = "MM/dd/yyyy HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    public static void setup() {
        AnsiConsole.systemInstall();
    }

    public static void log(Ansi message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().a(message));
    }

    public static void log(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().a(message));
    }

    public static void info(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().fg(Ansi.Color.CYAN).a(message));
    }

    public static void dev(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().fg(Ansi.Color.MAGENTA).a("DEV :: ").reset().a(message));
    }

    public static void thread(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().fg(Ansi.Color.YELLOW).a("TRH :: ").reset().a(message));
    }

    public static void packet(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().fg(Ansi.Color.BLUE).a("PKT :: ").reset().a(message));
    }

    public static void login(String message) {
        System.out.println(ansi().fg(GREEN).a("[" + getDate() + "] ").reset().fg(Ansi.Color.RED).a("LOG :: ").reset().a(message));
    }

    public static void debug(String message) {
        if(debug) {
            System.out.println(message);
        }
    }

    public static String getDate() {
        return dateFormat.format(Calendar.getInstance().getTime());
    }

}
