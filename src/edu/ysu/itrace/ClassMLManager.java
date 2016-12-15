package edu.ysu.itrace;

import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolygonDecoration;
import edu.ysu.onionuml.ui.graphics.figures.ClassSectionFigure;
import edu.ysu.onionuml.ui.graphics.figures.ClassFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Keeps updated information about the UML document viewed in one GraphicalEditor.
 */
public class ClassMLManager {
	/**
     * Types of UML entities.
     */
	
    public enum UMLEType {
    	RELATIONSHIP, //for CONNECTION Part
    	CONNECTION, // for CONNECTION Part
    	CONNECTOR, // for CONNECTION Part
        CLASS, //for CLASS Part
        METHOD, //for CLASS Part
    	MEMBER, //for CLASS Part
    	TEST,
    	WHITESPACE,
    	AMBIGUOUS
    }
    
    /**
     * Main Parts of UML entities.
     */
    public enum UMLEPart {
    	CLASS,
    	CONNECTION,
    	TEST,
    	WHITESPACE,
    	AMBIGUOUS
    }

    /**
     * Information extracted about a stack overflow entity.
     */
    public class UMLEntity {
        public UMLEPart umlPart = UMLEPart.AMBIGUOUS;
        public UMLEType umlType = UMLEType.AMBIGUOUS;
        public String entityName = "";
        public String entityVisibility = "";
        public String type = "";
        public String entityClass = "";
        public String returnType = "";
        public String sourceClass = "";
        public String targetClass = "";
    }
    
    private GraphicalViewer graphicalViewer;
    

    /**
     * Constructor. Sets up the GraphicalViewer.
     * @param graphicalViewer GraphicalViewer to which this UML DOM pertains.
     */
    public ClassMLManager(GraphicalViewer graphicalViewer) {
        this.graphicalViewer = graphicalViewer;
    }
    
    
    /**
     * Gets the UML entity found at a location on the GraphicalEditor.
     * @param relativeX x gaze coordinate relative to the control GraphicalEditor.
     * @param relativeY y gaze coordinate relative to the control GraphicalEditor.
     * @return the UML entity
     * @throws ClassNotFoundException 
     */
    public UMLEntity getUMLE(int relativeX, int relativeY){
    	UMLEntity entity = new UMLEntity();
    	Viewport objViewport;
    	IFigure objFigure;
    	
    	try {
    		//get the graphicalViewer's control -- it's a FigureCanvas
    		
        	Control control = graphicalViewer.getControl();
            if (control instanceof FigureCanvas) {
            	objViewport = ((FigureCanvas) control).getViewport(); // represents the visible portion of the ScrollPane
            	objFigure = objViewport.findFigureAt(relativeX, relativeY);  //lightweight graphical object at (x,y)
            	
            	
            	switch(objFigure.getClass().getName()){
                case "org.eclipse.draw2d.Figure":
                	//looking at whitespace - don't track entity
                	break;
                case "org.eclipse.draw2d.Label":
                	//looking at class, method, or property/attribute
                	
                	Label objLabel = (Label) objFigure;
                	String labelText = objLabel.getText();
                	IFigure objFigureParent = objFigure.getParent();
                	
                	if(objFigureParent.getClass().getName() == "edu.ysu.onionuml.ui.graphics.figures.ClassFigure"){
                		entity.umlPart = UMLEPart.CLASS;
                 		entity.umlType = UMLEType.CLASS;
                 		entity.entityName = labelText;
                	}
                	else if(objFigureParent.getClass().getName() == "edu.ysu.onionuml.ui.graphics.figures.ClassSectionFigure"){
                		//set visibility
                		switch(labelText.charAt(0)){
                		case '+':
                			entity.entityVisibility = "public";
                			break;
                		case '-':
                			entity.entityVisibility = "private";
                			break;
                		case '#':
                			entity.entityVisibility = "protected";
                			break;
                		default:
                			entity.entityVisibility = "?";
                			break;
                		}
                		
                		if(labelText.contains("(") && labelText.contains(")")){
                			entity.umlPart = UMLEPart.CLASS;
                     		entity.umlType = UMLEType.METHOD;
                     		
                     		// get method name
                     		if(labelText.contains("+ ") || labelText.contains("- ") || labelText.contains("# ")){
                     			System.out.println(labelText.substring(2,  labelText.indexOf("(")));
                    			entity.entityName = labelText.substring(2,  labelText.indexOf("("));
                    		}
                    		else{
                    			System.out.println(labelText.substring(0,  labelText.indexOf("("))); 
                    			entity.entityName = labelText.substring(0,  labelText.indexOf("("));
                    		}
                     		
                     		//get return type
                     		if(labelText.charAt(labelText.length() - 1) == ')'){
                     			entity.returnType = "void";
                     		}else{
                     			entity.returnType = labelText.substring(labelText.lastIndexOf(":") + 2, labelText.length());
                     		}
                     		//get class
                        	
                     		
                		} else {
                			entity.umlPart = UMLEPart.CLASS;
                     		entity.umlType = UMLEType.MEMBER;
                     		System.out.println(labelText.substring(labelText.indexOf(" ") + 1,  labelText.indexOf(":") - 1));
                			entity.entityName = labelText.substring(labelText.indexOf(" ") + 1,  labelText.indexOf(":") - 1);
                			entity.type = labelText.substring(labelText.indexOf(":") + 2,  labelText.length());
                			
                			System.out.println("WHAT IM LOOKING AT : " + objFigureParent.getClass().getName());
                     		entity.entityClass = labelText;
                		}
                	}
                	else{
                		entity.umlPart = UMLEPart.AMBIGUOUS;
                 		entity.umlType = UMLEType.AMBIGUOUS;
                	}
                	
                	break;
                case "org.eclipse.draw2d.PolylineConnection":	//CLASS LOADING ISSUE
                	//looking at line connection between classes
                	
                	PolylineConnection objConnection = (PolylineConnection) objFigure;
                	ConnectionAnchor srcAnchor = objConnection.getSourceAnchor();
                	ConnectionAnchor targetAnchor = objConnection.getTargetAnchor();
                	                	
                	try{
                		//get source anchor's figure
                    	//Figure srcFigure = (Figure) srcAnchor.getOwner();
                    	//System.out.println("SRC FIGURE: " + srcFigure.getClass().getName());
                    	
                    	
                    	ClassFigure srcClassFigure = (ClassFigure) srcFigure;  //casting error
                    	
                		//TODO: Get classname for source anchor
                		entity.sourceClass = "SourceClass";
                	}catch(Exception ex){
                		System.out.println("exception: " + ex.getMessage());
                	}
                	
                	
                	try{
                		//get target anchor's figure
<<<<<<< HEAD
                		//Figure targetFigure = (Figure) targetAnchor.getOwner();
                    	//ClassFigure targetClassFigure = (ClassFigure) targetFigure;  //casting error
=======
                		Figure targetFigure = (Figure) targetAnchor.getOwner();
                    	System.out.println(targetFigure.getClass().getName());
                    	ClassFigure targetClassFigure = (ClassFigure) targetFigure;  //casting error
                    	System.out.println(targetClassFigure.getNameString());
>>>>>>> origin/issue48-trackUML-continued
                    	
                    	
                		//TODO: Get classname for target anchor
                    	entity.targetClass = "TargetClass";
                	}catch(Exception ex){
                		System.out.println("exception: " + ex.getMessage());
                	}
                	
                	entity.umlPart = UMLEPart.CONNECTION;
             		entity.umlType = UMLEType.CONNECTION;
                	break;
                case "org.eclipse.draw2d.PolygonDecoration":	//CLASS LOADING ISSUE
                	//looking at connection head/tail between classes
                	
                	PolygonDecoration objDecoration = (PolygonDecoration) objFigure;
                	PolylineConnection objParentConnection = (PolylineConnection) objDecoration.getParent();
                	System.out.println("Poly Conn: "+objParentConnection.getClass().getName());
                	
                	//TODO: Get Parent and Child class
                	entity.entityName = "RelationshipType";
                	entity.sourceClass = "ParentClass";
                	entity.targetClass = "ChildClass";
                	
                	entity.umlPart = UMLEPart.CONNECTION;
             		entity.umlType = UMLEType.RELATIONSHIP;
                	break;
                case "org.eclipse.draw2d.Ellipse":
                	//looking at connector that groups multiple connections
                	entity.umlPart = UMLEPart.CONNECTION;
             		entity.umlType = UMLEType.CONNECTOR;
                	break;
                case "edu.ysu.onionuml.ui.graphics.figures.ClassFigure":	//CLASS LOADING ISSUE
                	//looking at class container

                	entity.entityName = "ClassName";
                	entity.entityClass = "ClassName";
                	
                	
                
                	entity.umlPart = UMLEPart.CLASS;
             		entity.umlType = UMLEType.CLASS;
                	break;
                case "edu.ysu.onionuml.ui.graphics.figures.ClassSectionFigure":
                	//looking at section of class
                	//PropertiesFigure, OperationsFigure, or OnionRelationshipsFigure
                	
<<<<<<< HEAD
                	ClassSectionFigure csf = (ClassSectionFigure) objFigure;
                	
                	entity.entityClass = csf.getClassName();
=======
                	
                	
>>>>>>> origin/issue48-trackUML-continued
                	entity.entityName = "ClassName";
                	
                	entity.umlPart = UMLEPart.CLASS;
             		entity.umlType = UMLEType.CLASS;
                	break;
                default:
                	entity.umlPart = UMLEPart.AMBIGUOUS;
             		entity.umlType = UMLEType.AMBIGUOUS;
                	break;
                }
            }
           
    	} catch (NullPointerException ex) {
    		//TODO: HANDLE EXCEPTION
    		return null;
    	}
    	
 		return entity;

    }
    
}