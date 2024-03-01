package dev.lennoxlotl.obfuscator;

import dev.lennoxlotl.obfuscator.source.StringPool;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.*;

public class MethodContext {

    public NativeObfuscator obfuscator;

    public final MethodNode method;
    public final ClassNode clazz;
    public final int methodIndex;
    public final int classIndex;

    public final StringBuilder output;
    public final StringBuilder nativeMethods;

    public Type ret;
    public ArrayList<Type> argTypes;

    public int line;
    public List<Integer> stack;
    public List<Integer> locals;
    public Set<TryCatchBlockNode> tryCatches;
    public Map<CatchesBlock, String> catches;

    public HiddenMethodsPool.HiddenMethod proxyMethod;
    public MethodNode nativeMethod;

    public int stackPointer;

    private final LabelPool labelPool = new LabelPool();

    public String cppNativeMethodName;

    public MethodContext(NativeObfuscator obfuscator, MethodNode method, int methodIndex, ClassNode clazz,
                         int classIndex) {
        this.obfuscator = obfuscator;
        this.method = method;
        this.methodIndex = methodIndex;
        this.clazz = clazz;
        this.classIndex = classIndex;

        this.output = new StringBuilder();
        this.nativeMethods = new StringBuilder();

        this.line = -1;
        this.stack = new ArrayList<>();
        this.locals = new ArrayList<>();
        this.tryCatches = new HashSet<>();
        this.catches = new HashMap<>();
    }

    public NodeCache<String> getCachedStrings() {
        return obfuscator.getCachedStrings();
    }

    public NodeCache<String> getCachedClasses() {
        return obfuscator.getCachedClasses();
    }

    public NodeCache<CachedMethodInfo> getCachedMethods() {
        return obfuscator.getCachedMethods();
    }

    public NodeCache<CachedFieldInfo> getCachedFields() {
        return obfuscator.getCachedFields();
    }

    public Snippets getSnippets() {
        return obfuscator.getSnippets();
    }

    public StringPool getStringPool() {
        return obfuscator.getStringPool();
    }

    public LabelPool getLabelPool() {
        return labelPool;
    }
}
