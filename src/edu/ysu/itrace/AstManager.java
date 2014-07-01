package edu.ysu.itrace;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.ui.progress.UIJob;

/**
 * Keeps updated information about the source code viewed in one StyledText.
 */
public class AstManager {
    /**
     * Types of source code entities.
     */
    public enum SCEType {
        TYPE,
        METHOD,
        VARIABLE,
        COMMENT,
    }

    /**
     * Information extracted about a source code entity.
     */
    public class SourceCodeEntity {
        public SCEType type;
        public String name;
        public int totalLength;
        public int startLine, endLine;
        public int startCol, endCol;
    }

    /**
     * Response object for getSCE(). Contains source code entity and the fully
     * qualified name of the entity.
     */
    public class SCEQueryResponse {
        public SourceCodeEntity sce;
        public String fullyQualifiedName;
    }

    /**
     * Task ran on UI thread to reload the AST. Like any UIJob, it can be
     * scheduled to occur later and can be canceled.
     */
    private class ReloadAstJob extends UIJob {
        private AstManager astManager;

        public ReloadAstJob(String name, AstManager astManager) {
            super(name);
            this.astManager = astManager;
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            astManager.reload();
            return Status.OK_STATUS;
        }
    }

    final private int AFTER_KEYPRESS_RELOAD_THRESHOLD_MILLIS = 1000;

    private StyledText styledText;
    private ReloadAstJob reloadAstJob;
    private LinkedList<SourceCodeEntity> sourceCodeEntities;

    /**
     * Constructor. Loads the AST and sets up the StyledText to automatically
     * reload after certain events.
     * @param styledText StyledText to which this AST pertains.
     */
    public AstManager(StyledText styledText) {
        this.styledText = styledText;
        hookupAutoReload();
        reload();
    }

    /**
     * Gets the source code entity found at a location in source.
     * @param lineNumber 1-based line number.
     * @param colNumber 0-based column number.
     * @return Response containing source code entity and its fully qualified
     *     name.
     */
    public SCEQueryResponse getSCE(int lineNumber, int colNumber) {
        SourceCodeEntity responseSce = null;
        Stack<SourceCodeEntity> sceFullyQualified =
                new Stack<SourceCodeEntity>();

        for (SourceCodeEntity sce : sourceCodeEntities) {
            boolean found = true;
            if (lineNumber < sce.startLine || lineNumber > sce.endLine)
                found = false;
            if (lineNumber == sce.startLine && colNumber < sce.startCol)
                found = false;
            if (lineNumber == sce.endLine && colNumber > sce.endCol)
                found = false;
            if (found) {
                sceFullyQualified.push(sce);
                //The first encountered SCE is the most specific SCE and will be
                //returned in the response.
                if (responseSce == null)
                    responseSce = sce;
            }
        }

        if (responseSce != null) {
            String fqName = "";
            while (!sceFullyQualified.empty())
                fqName += sceFullyQualified.pop().name + ".";
            fqName = fqName.substring(0, fqName.length() - 1);

            SCEQueryResponse result = new SCEQueryResponse();
            result.sce = responseSce;
            result.fullyQualifiedName = fqName;
            return result;
        } else {
            return null;
        }
    }

    /**
     * Reloads the AST from the current contents of the StyledText.
     */
    public void reload() {
        //Reset source code entities list.
        sourceCodeEntities = new LinkedList<SourceCodeEntity>();

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
                sce.name = node.getName().getFullyQualifiedName();
                determineSCEPosition(compileUnit, node, sce);
                sourceCodeEntities.add(sce);
                return true;
            }

            public boolean visit(MethodDeclaration node) {
                SourceCodeEntity sce = new SourceCodeEntity();
                sce.type = SCEType.METHOD;
                sce.name = node.getName().getFullyQualifiedName();
                determineSCEPosition(compileUnit, node, sce);
                sourceCodeEntities.add(sce);
                return true;
            }

            public boolean visit(VariableDeclarationFragment node) {
                SourceCodeEntity sce = new SourceCodeEntity();
                sce.type = SCEType.VARIABLE;
                sce.name = node.getName().getFullyQualifiedName();
                determineSCEPosition(compileUnit, node, sce);
                sourceCodeEntities.add(sce);
                return true;
            }
        };
        compileUnit.accept(visitor);
        //Get comments separately.
        for (Object comment_obj : compileUnit.getCommentList()) {
            if (comment_obj instanceof Comment) {
                Comment comment = (Comment) comment_obj;
                SourceCodeEntity sce = new SourceCodeEntity();
                sce.type = SCEType.COMMENT;
                sce.name = comment.toString();
                determineSCEPosition(compileUnit, comment, sce);
                sourceCodeEntities.add(sce);
            }
        }
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

    /**
     * Get line/column start/end information about an ASTNode.
     * @param compileUnit Compilation unit to which the ASTNode belongs.
     * @param node The ASTNode.
     * @param sce The SourceCodeEntity in which to store the result.
     */
    private static void determineSCEPosition(CompilationUnit compileUnit,
            ASTNode node, SourceCodeEntity sce) {
        sce.totalLength = node.getLength();
        sce.startLine = compileUnit.getLineNumber(node.getStartPosition());
        sce.endLine = compileUnit.getLineNumber(node.getStartPosition() +
                                                node.getLength());
        sce.startCol = compileUnit.getColumnNumber(node.getStartPosition());
        sce.endCol = compileUnit.getColumnNumber(node.getStartPosition() +
                                                 node.getLength());
    }

    /**
     * Called by constructor. Hooks up the StyledText with listeners to reload
     * the AST when it is likely to have changed.
     */
    private void hookupAutoReload() {
        final AstManager astManager = this;

        //Listen for key activity, then reload when inactivity follows.
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                //Do nothing.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (reloadAstJob != null)
                    reloadAstJob.cancel();

                reloadAstJob = new ReloadAstJob("reloadAstJob", astManager);
                reloadAstJob.schedule(AFTER_KEYPRESS_RELOAD_THRESHOLD_MILLIS);
            }
        };
        styledText.addKeyListener(keyListener);
    }
}
