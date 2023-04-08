package com.example.smartflashcards.keenanClasses;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.List;

public class MyFileOutputStream extends FileOutputStream {
    public MyFileOutputStream(FileDescriptor fileDescriptor) throws FileNotFoundException {
        super(fileDescriptor);
    }

    public MyFileOutputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public void writeInt(int integer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(integer);
        try {
            super.write(byteBuffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //write string as length and string bytes
    public void writeString(String string) {
        try {
            if (nonNull(string)) {
                byte[] byteArray = string.getBytes(StandardCharsets.UTF_8);
                int length = byteArray.length; //can't use string length because some characters use 2 bytes
                super.write(length);
                super.write(byteArray);
            } else {
                super.write((int)0);
            }
            super.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //write boolean as int
    public void writeBoolean(Boolean myBoolean) {
        try {
            write((int)(myBoolean?1:0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //write size, then keys and strings
    public void writeHashString(Hashtable hashtable) {
        try {
            super.write(hashtable.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        hashtable.forEach((key, string) -> {
            try {
                write((int)key);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeString((String) string);
        });
    }

    //write size, then keys and integers
    public void writeHashInt(Hashtable hashtable) {
        try {
            super.write(hashtable.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        hashtable.forEach((key, integer) -> {
            try {
                write((int)key);
                writeInt((int)integer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
