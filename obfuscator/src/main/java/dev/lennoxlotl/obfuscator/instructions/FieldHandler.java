package dev.lennoxlotl.obfuscator.instructions;

import dev.lennoxlotl.obfuscator.CachedFieldInfo;
import dev.lennoxlotl.obfuscator.MethodContext;
import dev.lennoxlotl.obfuscator.MethodProcessor;
import dev.lennoxlotl.obfuscator.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldHandler extends GenericInstructionHandler<FieldInsnNode> {

    @Override
    protected void process(MethodContext context, FieldInsnNode node) {
        boolean isStatic = node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC;
        CachedFieldInfo info = new CachedFieldInfo(node.owner, node.name, node.desc, isStatic);

        instructionName += "_" + Type.getType(node.desc).getSort();
        if (isStatic) {
            props.put("class_ptr", context.getCachedClasses().getPointer(node.owner));
        }

        int classId = context.getCachedClasses().getId(node.owner);

        context.output.append(String.format("if (!cclasses[%d]  || env->IsSameObject(cclasses[%d], NULL)) { cclasses_mtx[%d].lock(); if (!cclasses[%d] || env->IsSameObject(cclasses[%d], NULL)) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewWeakGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
                classId,
                classId,
                classId,
                MethodProcessor.getClassGetter(context, node.owner),
                classId,
                classId,
                trimmedTryCatchBlock));

        int fieldId = context.getCachedFields().getId(info);
        props.put("fieldid", context.getCachedFields().getPointer(info));

        context.output.append(String.format("if (!cfields[%d]) { cfields[%d] = env->Get%sFieldID(%s, %s, %s); %s  } ",
                fieldId,
                fieldId,
                isStatic ? "Static" : "",
                context.getCachedClasses().getPointer(node.owner),
                context.getStringPool().get(node.name),
                context.getStringPool().get(node.desc),
                trimmedTryCatchBlock));
    }

    @Override
    public String insnToString(MethodContext context, FieldInsnNode node) {
        return String.format("%s %s.%s %s", Util.getOpcodeString(node.getOpcode()), node.owner, node.name, node.desc);
    }

    @Override
    public int getNewStackPointer(FieldInsnNode node, int currentStackPointer) {
        if (node.getOpcode() == Opcodes.GETFIELD || node.getOpcode() == Opcodes.PUTFIELD) {
            currentStackPointer -= 1;
        }
        if (node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.GETFIELD) {
            currentStackPointer += Type.getType(node.desc).getSize();
        }
        if (node.getOpcode() == Opcodes.PUTSTATIC || node.getOpcode() == Opcodes.PUTFIELD) {
            currentStackPointer -= Type.getType(node.desc).getSize();
        }
        return currentStackPointer;
    }
}
