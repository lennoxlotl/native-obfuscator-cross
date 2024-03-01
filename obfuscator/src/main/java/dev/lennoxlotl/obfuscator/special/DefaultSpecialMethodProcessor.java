package dev.lennoxlotl.obfuscator.special;

import dev.lennoxlotl.obfuscator.MethodContext;
import dev.lennoxlotl.obfuscator.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preProcess(MethodContext context) {
        if (Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            List<Type> arguments = (Arrays.stream(Type.getArgumentTypes(context.method.desc)).collect(Collectors.toList()));
            arguments.add(0, Type.getType(Object.class));
            String resultDesc = Type.getMethodDescriptor(Type.getReturnType(context.method.desc), arguments.toArray(new Type[0]));

            String methodName = String.format("interfacestatic_%d_%d", context.classIndex, context.methodIndex);
            context.proxyMethod = context.obfuscator.getHiddenMethodsPool()
                    .getMethod(methodName, resultDesc, methodNode -> {
                        methodNode.signature = context.method.signature;
                        methodNode.access = Opcodes.ACC_NATIVE | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE;
                        methodNode.visibleAnnotations = new ArrayList<>();
                        methodNode.visibleAnnotations.add(new AnnotationNode("Ljava/lang/invoke/LambdaForm$Hidden;"));
                        methodNode.visibleAnnotations.add(new AnnotationNode("Ljdk/internal/vm/annotation/Hidden;"));
                    });
            return methodName;
        }
        context.method.access |= Opcodes.ACC_NATIVE;
        return "native_" + context.method.name + context.methodIndex;
    }

    @Override
    public void postProcess(MethodContext context) {
        context.method.instructions.clear();
        if (Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            InsnList list = new InsnList();

            if (Util.getFlag(context.method.access, Opcodes.ACC_STATIC)) {
                list.add(new LdcInsnNode(Type.getObjectType(context.clazz.name)));
            }

            int localVarsPosition = 0;

            for (Type arg : context.argTypes) {
                list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
                localVarsPosition += arg.getSize();
            }
            if (context.nativeMethod == null) {
                throw new RuntimeException("Native method not created?!");
            }
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    context.proxyMethod.getClassNode().name,
                    context.proxyMethod.getMethodNode().name,
                    context.proxyMethod.getMethodNode().desc, false));
            list.add(new InsnNode(Type.getReturnType(context.method.desc).getOpcode(Opcodes.IRETURN)));
            context.method.instructions = list;
        }
    }
}
