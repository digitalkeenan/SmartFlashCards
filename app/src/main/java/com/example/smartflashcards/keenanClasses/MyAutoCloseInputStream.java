package com.example.smartflashcards.keenanClasses;

import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MyAutoCloseInputStream extends ParcelFileDescriptor.AutoCloseInputStream {

    public MyAutoCloseInputStream(ParcelFileDescriptor fileDescriptor) throws FileNotFoundException {
        super(fileDescriptor);
    }

    public int readInt () {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            super.read(byteBuffer.array(), 0, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer.getInt();
    }

    //read a string stored in file as length and string bytes
    public String readString () {
        //ArrayList<byte> bytes;
        String readString = "";
        try {
            int length = super.read();
            byte[] byteArray = new byte[length];
            super.read(byteArray, 0, length);
            readString = new String(byteArray, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readString;
    }

    // read int and convert to Boolean
    public Boolean readBoolean () {
        int val = 0;
        try {
            val = super.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (val > 0);
    }
}
