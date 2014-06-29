package edu.ysu.itrace;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.custom.StyledText;

public class AstManager {
    public enum SCEType {
        TYPE,
        METHOD,
        VARIABLE,
        COMMENT,
    }

    public class SourceCodeEntity {
        public SCEType type;
        public String fullyQualifiedName;
        public int totalLength;
        public int startLine, endLine;
        public int startCol, endCol;
    }

    private StyledText styledText;
    private LinkedList<SourceCodeEntity> sourceCodeEntities =
            new LinkedList<SourceCodeEntity>();

    public AstManager(StyledText styledText) {
        this.styledText = styledText;
        reload();
    }

    public SourceCodeEntity getSCE(int lineNumber, int colNumber) {
        for (SourceCodeEntity sce : sourceCodeEntities) {
            boolean found = true;
            if (lineNumber < sce.startLine || lineNumber > sce.endLine)
                found = false;
            if (lineNumber == sce.startLine && colNumber < sce.startCol)
                found = false;
            if (lineNumber == sce.endLine && colNumber > sce.endCol)
                found = false;
            if (found)
                return sce;
        }
        return null;
    }

    public void reload() {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(styledText.getText().toCharArray());
        parser.setResolveBindings(true);
        final CompilationUnit compileUnit =
                (CompilationUnit) parser.createAST(null);

        ASTVisitor visitor = new ASTVisitor() {
            public boolean visit(TypeDeclaration node) {
                SourceCodeEntity sce = new SourceCodeEntity();
                sce.type = SCEType.TYPE;
                sce.fullyQualifiedName = node.getName().getFullyQualifiedName();
                determineSCEPosition(compileUnit, node, sce);
                sourceCodeEntities.add(sce);
                return true;
            }

            public boolean visit(MethodDeclaration node) {
                SourceCodeEntity sce = new SourceCodeEntity();
                sce.type = SCEType.METHOD;
                sce.fullyQualifiedName = node.getName().getFullyQualifiedName();
                determineSCEPosition(compileUnit, node, sce);
                sourceCodeEntities.add(sce);
                return true;
            }
        };
        compileUnit.accept(visitor);
        //Smaller entities take higher priority. If a method appears in a class,
        //for example, a query for the method should return the method instead
        //of the class.
        Collections.sort(sourceCodeEntities,
            new Comparator<SourceCodeEntity>() {
                @Override
                public int compare(SourceCodeEntity lhs, SourceCodeEntity rhs) {
                    return lhs.totalLength - rhs.totalLength;
                }
            });
    }

    private static void determineSCEPosition(CompilationUnit compileUnit,
            ASTNode node,SourceCodeEntity sce) {
        sce.totalLength = node.getLength();
        sce.startLine = compileUnit.getLineNumber(node.getStartPosition());
        sce.endLine = compileUnit.getLineNumber(node.getStartPosition() +
                                                node.getLength());
        sce.startCol = compileUnit.getColumnNumber(node.getStartPosition());
        sce.endCol = compileUnit.getColumnNumber(node.getStartPosition() +
                                                 node.getLength());
    }
}
