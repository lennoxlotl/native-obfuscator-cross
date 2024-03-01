package dev.lennoxlotl.obfuscator.instructions;

import dev.lennoxlotl.obfuscator.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

public class InstructionHandlerContainer<T extends AbstractInsnNode> {

    private final InstructionTypeHandler<T> handler;
    private final Class<T> clazz;

    public InstructionHandlerContainer(InstructionTypeHandler<T> handler, Class<T> clazz) {
        this.handler = handler;
        this.clazz = clazz;
    }

    public void accept(MethodContext context, AbstractInsnNode node) {
        handler.accept(context, clazz.cast(node));
    }

    public String insnToString(MethodContext context, AbstractInsnNode node) {
        return handler.insnToString(context, clazz.cast(node));
    }

    public int getNewStackPointer(AbstractInsnNode node, int stackPointer) {
        return handler.getNewStackPointer(clazz.cast(node), stackPointer);
    }
}
