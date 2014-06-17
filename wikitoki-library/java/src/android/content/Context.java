package android.content;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

public class Context {
    public static final int MODE_PRIVATE = 0;

    public Context() {
    }

    public final FileOutputStream openFileOutput(String name, int mode) {
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fs;
    }

    public final FileInputStream openFileInput(String name) {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fs;
    }
}
