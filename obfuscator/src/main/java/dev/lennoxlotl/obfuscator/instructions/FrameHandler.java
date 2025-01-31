package dev.lennoxlotl.obfuscator.instructions;

import dev.lennoxlotl.obfuscator.MethodContext;
import dev.lennoxlotl.obfuscator.MethodProcessor;
import dev.lennoxlotl.obfuscator.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;
import java.util.function.Consumer;

public class FrameHandler implements InstructionTypeHandler<FrameNode> {

    @Override
    public void accept(MethodContext context, FrameNode node) {
        Consumer<Object> appendLocal = local -> {
            if (local instanceof String) {
                context.locals.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else if (local instanceof LabelNode) {
                context.locals.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else {
                context.locals.add(MethodProcessor.STACK_TO_STACK[(int) local]);
            }
        };

        Consumer<Object> appendStack = stack -> {
            if (stack instanceof String) {
                context.stack.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else if (stack instanceof LabelNode) {
                context.stack.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else {
                context.stack.add(MethodProcessor.STACK_TO_STACK[(int) stack]);
            }
        };


        switch (node.type) {
            case Opcodes.F_APPEND:
                node.local.forEach(appendLocal);
                context.stack.clear();
                break;

            case Opcodes.F_CHOP:
                node.local.forEach(item -> context.locals.remove(context.locals.size() - 1));
                context.stack.clear();
                break;

            case Opcodes.F_NEW:
            case Opcodes.F_FULL:
                context.locals.clear();
                context.stack.clear();
                node.local.forEach(appendLocal);
                node.stack.forEach(appendStack);
                break;

            case Opcodes.F_SAME:
                context.stack.clear();
                break;

            case Opcodes.F_SAME1:
                context.stack.clear();
                appendStack.accept(node.stack.get(0));
                break;
        }

        if (context.stack.stream().anyMatch(x -> x == 0)) {
            int currentSp = 0;
            context.output.append("    ");
            for (int type : context.stack) {
                if (type == 0) {
                    context.output.append("refs.erase(cstack").append(currentSp).append(".l); ");
                }
                currentSp += Math.max(1, type);
            }
            context.output.append("\n");
        }

        if (context.locals.stream().anyMatch(x -> x == 0)) {
            int currentLp = 0;
            context.output.append("    ");
            for (int type : context.locals) {
                if (type == 0) {
                    context.output.append("refs.erase(clocal").append(currentLp).append(".l); ");
                }
                currentLp += Math.max(1, type);
            }
            context.output.append("\n");
        }
        context.output.append("    utils::clear_refs(env, refs);\n");
    }

    @Override
    public String insnToString(MethodContext context, FrameNode node) {
        return String.format("FRAME %s L: %s S: %s", Util.getOpcodesString(node.type, "F_"),
                node.local == null ? "null" : Arrays.toString(node.local.toArray(new Object[0])),
                node.stack == null ? "null" : Arrays.toString(node.stack.toArray(new Object[0])));
    }

    @Override
    public int getNewStackPointer(FrameNode node, int currentStackPointer) {
        switch (node.type) {
            case Opcodes.F_APPEND:
            case Opcodes.F_SAME:
            case Opcodes.F_CHOP:
                return 0;
            case Opcodes.F_NEW:
            case Opcodes.F_FULL:
                return node.stack.stream().mapToInt(argument -> Math.max(1, argument instanceof Integer ?
                        MethodProcessor.STACK_TO_STACK[(int) argument] : MethodProcessor.TYPE_TO_STACK[Type.OBJECT])).sum();
            case Opcodes.F_SAME1:
                return node.stack.stream().limit(1).mapToInt(argument -> Math.max(1, argument instanceof Integer ?
                        MethodProcessor.STACK_TO_STACK[(int) argument] : MethodProcessor.TYPE_TO_STACK[Type.OBJECT])).sum();
        }
        throw new RuntimeException();
    }
}
