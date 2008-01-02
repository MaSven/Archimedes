/*
 * Created on 11/05/2006
 */

package br.org.archimedes.copypaste;

import java.util.HashSet;
import java.util.Set;

import br.org.archimedes.Constant;
import br.org.archimedes.controller.commands.PutOrRemoveElementCommand;
import br.org.archimedes.exceptions.InvalidParameterException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.factories.SelectionPointVectorFactory;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.interfaces.Command;
import br.org.archimedes.interfaces.Parser;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Vector;
import br.org.archimedes.parser.ReturnDecoratorParser;
import br.org.archimedes.undo.UndoCommand;

/**
 * Belongs to package com.tarantulus.archimedes.commands.
 */
public class CopyPasteFactory extends SelectionPointVectorFactory {

    private Command command;

    private int pastes;

    public CopyPasteFactory () {

        pastes = 0;
    }

    /**
     * Just overrides to handle the "u" and enter.
     * 
     * @see com.tarantulus.archimedes.factories.CommandFactory#next(java.lang.Object)
     */
    public String next (Object parameter) throws InvalidParameterException {

        String message = null;
        if (parameter == null) {
            deactivate();
            message = Messages.CommandFinished;
        }
        else if (parameter.equals("u") && pastes > 0) { //$NON-NLS-1$
            pastes--;
            command = new UndoCommand();
            message = Messages.PasteUndone + Constant.NEW_LINE
                    + Messages.TargetExpected;
        }
        else {
            message = super.next(parameter);
        }
        return message;
    }

    /**
     * Moves the elements from reference point to target.
     * @param vector
     *            The vector to complete the command
     * @param selection
     *            The selection of elements to complete the command
     * 
     * @return A message to the user indicating if the command was successfully
     *         finished.
     */
    @Override
    protected String completeCommand (Set<Element> selection, Point point, Vector vector) {

        String result = Messages.CommandFinished;

        Set<Element> copied = new HashSet<Element>();
        Element aCopy;
        for (Element element : selection) {
            aCopy = element.clone();
            aCopy.move(vector.getX(), vector.getY());
            copied.add(aCopy);
        }
        try {
            command = new PutOrRemoveElementCommand(copied, false);
            pastes++;
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }

        return result;
    }

    public String getName () {

        return "copy"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.commands.Command#getNextParser()
     */
    public Parser getNextParser () {

        Parser returnParser = super.getNextParser();

        if (pastes > 0 && !isDone()) {
            returnParser = new ReturnDecoratorParser(returnParser);
        }

        return returnParser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.factories.SelectionPointVectorFactory#drawVisualHelper(com.tarantulus.archimedes.model.writers.Writer,
     *      java.util.Set, com.tarantulus.archimedes.model.Point,
     *      com.tarantulus.archimedes.model.Vector)
     */
    @Override
    protected void drawVisualHelper (Set<Element> selection,
            Point reference, Vector vector) {

        for (Element element : selection) {
            Element copied = element.clone();
            copied.move(vector.getX(), vector.getY());
            copied.draw(OpenGLWrapper.getInstance());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.factories.SelectionPointVectorFactory#getUniqueCommand()
     */
    @Override
    protected Command getUniqueCommand () {

        Command returnCmd = command;
        command = null;
        return returnCmd;
    }
}