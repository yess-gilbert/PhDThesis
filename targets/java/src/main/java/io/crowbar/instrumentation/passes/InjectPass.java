package io.crowbar.instrumentation.passes;

import io.crowbar.sandbox.Tree.Node;
import io.crowbar.instrumentation.runtime.ProbeType;

import javassist.*;
import javassist.bytecode.*;

public class InjectPass extends Pass {
    public InjectPass (Granularity g) {
        granularity = g;
    }

    @Override
    public void transform (CtClass c) throws Exception {
        boolean injected = false;


        for (CtMethod m : c.getDeclaredMethods()) {
            MethodInfo info = m.getMethodInfo();
            CodeAttribute ca = info.getCodeAttribute();

            if (ca == null)
                continue;

            injected = true;
            handleMethod(c, m);
            ca.setMaxStack(ca.computeMaxStack());
        }

        if (injected) {
            CtField f = CtField.make("public static boolean[]  " + HIT_VECTOR_NAME + " = " +
                                     "Collector.getDefault().getHitVector(\"" + c.getName() +
                                     "\");", c);
            c.addField(f);
        }
    }

    protected void handleMethod (CtClass c,
                                 CtMethod m) throws Exception {
        MethodInfo info = m.getMethodInfo();
        CodeAttribute ca = info.getCodeAttribute();
        CodeIterator ci = ca.iterator();


        for (int last_line = -1, index, cur_line;
             ci.hasNext();
             last_line = cur_line) {
            index = ci.next();
            cur_line = info.getLineNumber(index);

            if (cur_line == -1)
                continue;

            if (cur_line <= last_line)
                continue;


            Bytecode b = getInstrumentationCode(c, m, cur_line, info.getConstPool());
            ci.insert(index, b.get());

            if (granularity == Granularity.FUNCTION)
                break;
        }
    }

    protected Bytecode getInstrumentationCode (CtClass c,
                                               CtMethod m,
                                               int line,
                                               ConstPool p) {
        Bytecode b = new Bytecode(p);
        Node n = getNode(c, m, line);
        int id = registerProbe(c, n, ProbeType.HIT_PROBE);


        b.addGetstatic(c, HIT_VECTOR_NAME, HIT_VECTOR_TYPE);
        b.addIconst(id);
        b.addOpcode(Opcode.ICONST_1);
        b.addOpcode(Opcode.BASTORE);

        return b;
    }

    private final String HIT_VECTOR_NAME = "__CROWBAR_HIT_VECTOR__";
    private final String HIT_VECTOR_TYPE = "[Z";

    public enum Granularity {
        STATEMENT,
        FUNCTION
    }
    private Granularity granularity;
}