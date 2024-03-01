package dev.lennoxlotl.obfuscator.compiletime;

public class LoaderPlain {
    public static native void registerNativesForClass(int index, Class<?> clazz);

    static {
        System.loadLibrary("%LIB_NAME%");
    }
}
