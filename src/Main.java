import jssc.SerialPort;
import jssc.SerialPortException;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static String STX = "\u0002";
    static String EOT = "\u0004";
    static String SOH = "\u0001";
    static String DC4 = "\u0014";
    static String BS = "\b";
    private static String bf;

    static SerialPort serialPort = new SerialPort("COM1");





    public static void main(String[] args) {
        openPort();

        File file = new File("c:\\Users\\Aleksey\\Desktop\\puttySwimmingpiter.txt");
        //BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1251"));

        try (FileInputStream fis = new FileInputStream(file)) {

            // remaining bytes that can be read
            System.out.println("Remaining bytes that can be read : " + fis.available());

            // 8 a time
            byte[] bytes = new byte[1000];

            // reads 8192 bytes at a time, if end of the file, returns -1
            while (fis.read(bytes) != -1) {
                bf += new String(bytes);
                if (bf.contains(SOH) && bf.contains(EOT)) {
                    readData(bf.replaceAll("\n", ""));

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void openPort(){
        try {
            serialPort.openPort();
        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        }
        try {
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        }

        try {
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        }

    }

    private static void readData(String buffer) {
        String pattern = "(?=[\u0001])";
        String[] buf = buffer.split(pattern);
        String bufferSOH = "";
        String bufferEOT = "";


        bf = buf[buf.length - 1];

        String output = "";

        for (int i = 0; i < buf.length - 1; i++) {
            String b = buf[i];

            if (!b.isEmpty()) {
                if (b.contains(SOH) && b.contains(EOT)) {
                    output = b.
                            replaceAll(STX, "STX").replaceAll(EOT, "EOT").
                            replaceAll(SOH, "SOH").replaceAll(DC4, "DC4").
                            replaceAll(BS, "BS");

                } else if (b.contains(SOH) && !b.contains(EOT)) {
                    bufferSOH = b;


                } else if (!b.contains(SOH) && b.contains(EOT)) {
                    bufferEOT = b;

                }
                if ((bufferSOH+bufferEOT).contains(SOH) && (bufferSOH+bufferEOT).contains(EOT)) {
                    output = (bufferSOH+bufferEOT).replaceAll(STX, "STX").replaceAll(EOT, "EOT").
                            replaceAll(SOH, "SOH").replaceAll(DC4, "DC4").
                            replaceAll(BS, "BS");
                    bufferSOH = "";
                    bufferEOT = "";

                }
            }

            try {
                Thread.sleep(70);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(output);
            try {
                serialPort.writeString(output.replaceAll("STX", STX).replaceAll("EOT", EOT).
                        replaceAll("SOH", SOH).replaceAll("DC4", DC4).
                        replaceAll("BS", BS));
            } catch (SerialPortException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

