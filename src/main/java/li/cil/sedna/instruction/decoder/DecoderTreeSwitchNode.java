package li.cil.sedna.instruction.decoder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import li.cil.sedna.instruction.InstructionDeclaration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public final class DecoderTreeSwitchNode extends AbstractDecoderTreeNode {
    public final int mask;
    public final int[] patterns;
    public final AbstractDecoderTreeNode[] children;

    DecoderTreeSwitchNode(final int mask, final Int2ObjectArrayMap<ArrayList<InstructionDeclaration>> groups) {
        this.mask = mask;

        final int[] groupPatterns = groups.keySet().toIntArray();
        assert groupPatterns.length > 1;
        sortUnsigned(groupPatterns);

        patterns = new int[groupPatterns.length];
        children = new AbstractDecoderTreeNode[groupPatterns.length];

        for (int i = 0; i < groupPatterns.length; i++) {
            final int groupPattern = groupPatterns[i];
            final ArrayList<InstructionDeclaration> group = groups.get(groupPattern);

            patterns[i] = groupPattern;
            children[i] = DecoderTree.create(group, mask);
        }
    }

    @Override
    public int maxDepth() {
        int maxChildDepth = 0;
        for (final AbstractDecoderTreeNode child : children) {
            maxChildDepth = Math.max(maxChildDepth, child.maxDepth());
        }

        return 1 + maxChildDepth;
    }

    @Nullable
    @Override
    public InstructionDeclaration findDeclaration(final int instruction) {
        for (int i = 0; i < patterns.length; i++) {
            if ((instruction & mask) == patterns[i]) {
                return children[i].findDeclaration(instruction);
            }
        }

        return null;
    }

    @Override
    public void accept(final DecoderTreeVisitor visitor) {
        final DecoderTreeSwitchVisitor switchVisitor = visitor.visitSwitch();
        if (switchVisitor != null) {
            switchVisitor.visit(this);
            for (int i = 0; i < patterns.length; i++) {
                final DecoderTreeVisitor switchCaseVisitor = switchVisitor.visitSwitchCase(this, i);
                if (switchCaseVisitor != null) {
                    children[i].accept(switchCaseVisitor);
                }
            }

            switchVisitor.visitEnd();
        }

        visitor.visitEnd();
    }

    @Override
    public String toString() {
        return "[SWITCH]";
    }

    private static void sortUnsigned(final int[] values) {
        final int length = values.length;
        for (int i = 0; i < length; i++) {
            values[i] = values[i] ^ Integer.MIN_VALUE;
        }
        Arrays.sort(values);
        for (int i = 0; i < length; i++) {
            values[i] = values[i] ^ Integer.MIN_VALUE;
        }
    }
}
