package android.content;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;

public class Context {
    public static final int MODE_PRIVATE = 0;

    public Context() {
    }

    public final FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return new FileOutputStream(new File("store", name).toString());
    }

    public final FileInputStream openFileInput(String name) throws FileNotFoundException {
        return new FileInputStream(new File("store", name).toString());
    }

    public final String[] fileList() {
        File folder = new File("store");
        return folder.list();
    }
}
