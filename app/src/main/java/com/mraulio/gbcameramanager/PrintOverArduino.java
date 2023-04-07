package com.mraulio.gbcameramanager;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrintOverArduino {
    private final String INIT = "88 33 01 00 00 00 01 00 00 00";//81 00
    private final String PRINT = "88 33 02 00 04 00 01 03 E4 7F 6D 01 00 00";//E4 standard palette, 7F max print intensity - 81 00
    private final String DATA_COMMAND = "88 33 04 00 80 02";
    private final String EMPTY_DATA = "88 33 04 00 00 00 04 00 00 00";
    private final String END_DATA = "00 00";
    private final String START_CHECKSUM = "04 00 80 02";//First part of the checksum, next will be the data
    byte[] checksum;
    public static double count = 0;
    public static double percentage = 0;
    //DATA at the bottom of the Class
    private final int TIMEOUT_SEND = 0;//0 so there is no TIMEOUT       120 working for INIT, PRINT, EMPTY
    private final int TIMEOUT_READ = 100;
    public static int sleepTime = 0;
    public boolean banner = false;
    public boolean oneImage = false;


    public byte[] checksumCalc(byte[] tileData) {
        int checksum = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] startBytes = getCommandBytes(START_CHECKSUM);
        try {
            outputStream.write(startBytes);
            outputStream.write(tileData);
        } catch (Exception e) {
        }
        byte[] data = outputStream.toByteArray();
        //Iterate through the byte array, adding each byte to the checksum. Make sure to use the bitwise AND operator to mask off the higher bits of the bytes, since the checksum is only 2 bytes long.
        for (byte b : data) {
            checksum = (checksum + (b & 0xff)) & 0xffff;
        }
        //Convert the checksum to a byte array in little endian order, using bitwise operations to extract the lower and higher bytes.
        byte[] checksumBytes = new byte[2];
        checksumBytes[0] = (byte) (checksum & 0xff);
        checksumBytes[1] = (byte) ((checksum >> 8) & 0xff);

        return checksumBytes;
    }

    private byte[] createDataWithImageBytes(TextView tv, byte[] imageBytes) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(getCommandBytes(INIT));
        } catch (Exception e) {
        }
        if (banner) {
            imageBytes = getCommandBytes(mysteryPacket);
        }
        tv.append("Los bytes del esto son: " + imageBytes.length);
//        List<byte[]> chunkList = new ArrayList<>();
//        String data_nospace = onlyTileData.replaceAll(System.lineSeparator(), " ").replaceAll(" ", "");
//        int len2 = data_nospace.length();
//        byte[] bytesTileData = new byte[len2 / 2];
//        for (int i = 0; i < len2; i += 2) {
//            bytesTileData[i / 2] = (byte) ((Character.digit(data_nospace.charAt(i), 16) << 4)
//                    + Character.digit(data_nospace.charAt(i + 1), 16));
//        }
        int chunkSize = 640;//each data packet size, 640 + 4 from the start checksum
        for (int i = 0; i < imageBytes.length; i += chunkSize) {
            int chunkLength = Math.min(chunkSize, imageBytes.length - i);
            byte[] chunk = Arrays.copyOfRange(imageBytes, i, i + chunkLength);
            try {
                outputStream.write(getCommandBytes(DATA_COMMAND));
                outputStream.write(chunk);//I write the 640 bytes
                outputStream.write(checksumCalc(chunk)); //I write the Checksum, need to calculate it with start_checksum+data
                outputStream.write(getCommandBytes(END_DATA));//The last 2 bytes

            } catch (Exception e) {
                tv.append(e.toString());
            }
//            System.out.println(chunk.length);
            // haz algo con el chunk, como enviarlo a través de la red o guardarlo en un archivo
        }

        try {
            outputStream.write(getCommandBytes(EMPTY_DATA));
            outputStream.write(getCommandBytes(PRINT));
        } catch (Exception e) {
            tv.append("Error EMPTY, PRINT" + e.toString());

        }
        tv.append("" + outputStream.toByteArray().length);
        return outputStream.toByteArray();
    }

    /**
     *
     */

    private byte[] getCommandBytes(String data2) {
        String data = data2.replaceAll(" ", "");
        int len2 = data.length();
        byte[] bytes = new byte[len2 / 2];
        for (int i = 0; i < len2; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                    + Character.digit(data.charAt(i + 1), 16));
        }
        return bytes;
    }

    private List<byte[]> createDataDelay(TextView tv) {
        List<byte[]> listBytes = new ArrayList<>();

        listBytes.add(getCommandBytes(INIT));//listBytes.get(0)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytesTileData;
        String data_nospace = "";
        if (banner) {
            data_nospace = mysteryPacket.replaceAll(System.lineSeparator(), " ").replaceAll(" ", "");
            int len2 = data_nospace.length();
            bytesTileData = new byte[len2 / 2];
            for (int i = 0; i < len2; i += 2) {
                bytesTileData[i / 2] = (byte) ((Character.digit(data_nospace.charAt(i), 16) << 4)
                        + Character.digit(data_nospace.charAt(i + 1), 16));
            }
        } else {
            bytesTileData = Methods.gbcImagesList.get(MainActivity.printIndex).getImageBytes();
        }

        int chunkSize = 640;//each data packet size
        for (int i = 0; i < bytesTileData.length; i += chunkSize) {
            int chunkLength = Math.min(chunkSize, bytesTileData.length - i);
            byte[] chunk = Arrays.copyOfRange(bytesTileData, i, i + chunkLength);
            try {
                outputStream.write(getCommandBytes(DATA_COMMAND));
                outputStream.write(chunk);//I write the 640 bytes
                outputStream.write(checksumCalc(chunk)); //I write the Checksum, need to calculate it with start_checksum+data
                outputStream.write(getCommandBytes(END_DATA));//The last 2 bytes
                listBytes.add(outputStream.toByteArray());//listBytes.get(1-X)
                outputStream.reset();//Empty the outputstream
            } catch (Exception e) {
                tv.append(e.toString());
            }
        }
        listBytes.add(getCommandBytes(EMPTY_DATA));//listBytes.get(size-1)
        listBytes.add(getCommandBytes(PRINT));//listBytes.get(size)

        return listBytes;
    }

    public void sendThreadDelay(UsbDeviceConnection connection, UsbDevice usbDevice, TextView textView, Context context) {
        UsbEndpoint endpoint = null;
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint usbEndpoint = usbInterface.getEndpoint(j);
                if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpoint = usbEndpoint;
                    break;
                }
            }
            if (endpoint != null) {
                break;
            }
        }
        if (endpoint == null) {
            textView.append("No se pudo encontrar un endpoint válido");
        }
        UsbEndpoint finalEndpoint = endpoint;
        List<byte[]> listBytes = createDataDelay(textView);

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (byte[] byteArray : listBytes) {
                    // Enviar datos por USB
                    connection.bulkTransfer(finalEndpoint, byteArray, byteArray.length, 0);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Esperar 20000ms después de enviar el último byte

//                MainActivity.freeTv = true;
                count = 0;

//                if (twoImages) {
//                    try {
//                        Thread.sleep(15000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    MainActivity.freeTv = false;
//                    connection.bulkTransfer(finalEndpoint, byteArray, byteArray.length, 0);
//                }
                // Realizar cualquier otra operación necesaria después de la espera de 1200ms
            }
        });
        sendThread.start();
    }

    public void sendThread(UsbDeviceConnection connection, UsbDevice usbDevice, TextView textView) {
        UsbEndpoint endpoint = null;
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint usbEndpoint = usbInterface.getEndpoint(j);
                if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpoint = usbEndpoint;
                    break;
                }
            }
            if (endpoint != null) {
                break;
            }
        }
        if (endpoint == null) {
            textView.append("No se pudo encontrar un endpoint válido");
        }
        UsbEndpoint finalEndpoint = endpoint;

//        MainActivity.freeTv = false;

//        /**
//         * Sending this works
//         */
//        String all = INIT + " " + DATA_COMMAND + " " + mysteryPacket.replaceAll(System.lineSeparator(), " ") + " " + EMPTY_DATA + " " + PRINT;
//        String[] hexValues = all.split(" ");
//        byte[] byteArray = new byte[hexValues.length];
////        textView.append("length: "+hexValues.length);
//        for (int i = 0; i < hexValues.length; i++) {
//            byteArray[i] = (byte) Integer.parseInt(hexValues[i], 16);
////            System.out.println(byteArray[i]);
//        }
//        /**
//         *
//         */

//        byte[] byteArray = createData(textView);
        byte[] byteArray = createDataWithImageBytes(textView, Methods.gbcImagesList.get(MainActivity.printIndex).getImageBytes());
        textView.append("byte array length:" + byteArray.length);
        percentage = byteArray.length;

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Enviar datos por USB
                connection.bulkTransfer(finalEndpoint, byteArray, byteArray.length, 0);
                // Esperar 20000ms después de enviar el último byte

//                MainActivity.freeTv = true;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count = 0;

                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                connection.bulkTransfer(finalEndpoint, byteArray, byteArray.length, 0);

                // Realizar cualquier otra operación necesaria después de la espera de 1200ms
            }
        });
        sendThread.start();
    }

    String blackLine = "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF";
    String mysteryPacket = "C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 18 00 3C 00 3C 00 18 00 01 00 01 00 00 18 00 3C C0 C0 06 00 06 00 C0 00 E0 00 E0 00 C0 00 00 06 00 00 00 00 00 00 00 00 40 00 01 01 03 02 07 04 0E 0F 1F 31 3E 60 7D C0 F9 84 F3 0F EF 1F F8 34 00 00 00 80 80 80 80 80 F0 F0 F8 F8 F8 F8 80 80 00 05 00 10 00 00 00 10 00 00 00 10 00 00 00 10 00 55 00 00 FF FF FE 9C BE DC FE 9C BE DC FE 80 00 55 00 00 C0 C0 C0 C0 CF CF DF D8 DF D3 D7 DB 00 55 00 00 07 07 07 04 E7 E6 F7 76 B3 32 B3 32 00 55 00 00 E7 E7 67 64 67 66 67 66 63 62 63 62 00 55 00 00 E0 E0 60 60 63 63 67 66 67 64 65 66 00 55 00 00 01 01 01 01 F9 F9 FD 1D ED CD ED CD 00 55 00 00 F0 F0 B0 30 B0 30 B0 30 B0 30 B0 30 00 00 01 41 02 02 02 42 02 02 02 42 02 02 02 42 FE FE 83 FF FE 82 82 FE FE FE 82 FE FE FE 82 FE 00 00 00 00 80 80 80 80 E2 80 E0 80 E0 80 E0 80 03 00 00 60 00 60 00 03 00 07 00 07 00 03 60 60 00 18 00 3C 00 3C 00 18 00 80 00 80 18 18 3C 3C C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 C3 D8 00 3C 00 18 00 01 00 01 18 18 3C 3C 3C 3C 18 18 00 06 00 C0 00 E0 00 E0 00 C0 06 06 06 06 C0 C0 07 04 05 07 07 07 43 03 01 03 01 01 03 03 00 00 FD E1 FD F1 CF F0 FD CE FF AF BF C3 7F E0 1F 3F E0 E0 70 98 F8 08 F0 18 E0 F0 80 C5 80 80 00 00 00 00 00 10 00 00 00 50 00 00 00 50 00 00 00 15 BE DC FE 9C BE DC FE 9C FF FF FF FF 00 00 00 55 DF D0 D7 DB DF D3 DF D8 DF DF CF CF 00 00 00 55 B3 32 F3 F2 33 32 73 72 F3 F3 E3 E3 00 00 00 55 63 62 63 62 63 62 63 62 E3 E3 E3 E3 00 00 00 55 67 64 65 66 67 64 67 66 E7 E7 E3 E3 00 00 00 55 ED CD ED CD ED CD FD 1D FD FD F9 F9 00 00 00 55 B0 30 B0 30 F0 F0 B0 30 F0 F0 F0 F0 00 00 00 55 02 02 02 42 02 02 02 42 02 02 02 42 03 03 00 40 FE FE 00 00 00 00 06 C0 06 C0 00 00 FF FF FF 00 E0 80 E0 80 E0 80 E2 80 E0 80 E0 80 E0 80 E0 00 60 60 03 03 07 07 07 07 03 03 60 00 60 00 03 00 3C 3C 18 18 80 80 80 80 18 00 3C 00 3C 00 18 00 C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B C3 1B";
}
