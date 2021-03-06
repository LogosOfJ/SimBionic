package com.stottlerhenke.simbionic.editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;

import com.stottlerhenke.simbionic.editor.SB_Behavior;
import com.stottlerhenke.simbionic.editor.SB_Binding;
import com.stottlerhenke.simbionic.editor.SB_Function;
import com.stottlerhenke.simbionic.editor.SB_Parameter;
import com.stottlerhenke.simbionic.editor.SimBionicEditor;
import com.stottlerhenke.simbionic.editor.Util;

/**
 * SB_ToolBar.java
 */
public class SB_ToolBar extends JToolBar implements ActionListener, SB_AutocompleteListener, ListSelectionListener,
	MouseListener, MouseMotionListener
{
	
	protected boolean debugMode = false;
	
    protected JToggleButton _hackConnectorButton;
    
    protected JToolBar _toolBarTop;
    protected JToolBar _toolBarBottom;
    protected SimBionicEditor _editor;

    protected Action _backAction;
    protected Action _forwardAction;
    protected Action _homeAction;
    protected Action _bindingsAction;
    // protected Action _localsAction;
    protected Action _exprAction;

    protected SB_Autocomplete _exprField;
    protected JComboBox _varComboBox;
    protected SB_Autocomplete _bindingField;

    protected JDialog _localsDialog = null;
    protected boolean _lastVisible = false;

    protected JDialog _bindingsDialog = null;
    protected SB_BindingsTable _bindingsTable;
    protected JButton _insertButton;
    protected JButton _deleteButton;
    protected JButton _setValueButton;
    protected JButton _moveUpButton;
    protected JButton _moveDownButton;
    protected JButton _bindingsOK;
    protected JButton _bindingsCancel;
    
    protected SB_MultiDialog _compoundActionDialog = null;

    static final int MAX_STACK_SIZE = 10;
    protected Vector _backStack = new Vector();
    protected Vector _forwardStack = new Vector();

    protected JPopupMenu _backPopup = new JPopupMenu();
    protected JMenuItem[] _backMenuItems = new JMenuItem[MAX_STACK_SIZE];
    protected JPopupMenu _forwardPopup = new JPopupMenu();
    protected JMenuItem[] _forwardMenuItems = new JMenuItem[MAX_STACK_SIZE];

    protected JDialog _exprDialog = null;
    
    protected SB_AutocompleteTextArea _exprFieldInDialog;
    protected JTextArea _expressionComment;
    
    protected DefaultListModel _actionPredModel;
    protected JList _actionPredList;
    protected JLabel _actionPredListTitle;

    protected DefaultListModel _variableModel;
    protected JList _variableList;

    protected DefaultListModel _paramModel;
	protected JList _paramList;

	protected JCheckBox _filterByTypeCheckBox;

	protected JButton _exprOK;
	protected JButton _exprCancel;

	private boolean _matchingPred;

    public SB_ToolBar(SimBionicEditor editor)
    {
        _editor = editor;

        ComponentRegistry.setToolbar(this);
        setFloatable(false);
        setRollover(true);

        setOrientation(JToolBar.VERTICAL);

        _toolBarTop = new JToolBar();
        _toolBarTop.setFloatable(false);
        _toolBarTop.setRollover(true);
        _toolBarTop.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        _toolBarBottom = new JToolBar();
        _toolBarBottom.setFloatable(false);
        _toolBarBottom.setRollover(true);
        _toolBarBottom.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        _backAction = new BackAction("Go Back", Util.getImageIcon("Back16.gif"), "Previous canvas",
                new Integer(KeyEvent.VK_LEFT));

        _backAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
            InputEvent.ALT_DOWN_MASK, false));
        _forwardAction = new ForwardAction("Go Forward", Util.getImageIcon("Forward16.gif"),
                "Next canvas", new Integer(KeyEvent.VK_RIGHT));
        _forwardAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
            InputEvent.ALT_DOWN_MASK, false));
        _homeAction = new HomeAction("Return Home", Util.getImageIcon("Home16.gif"), "Home",
                new Integer(KeyEvent.VK_HOME));
        _bindingsAction = new BindingsAction("=", "Edit Bindings...", new Integer(
                KeyEvent.VK_EQUALS));
        // _localsAction = new LocalsAction("Locals", Util.getImageIcon(
        // "Local.gif"), "Locals", new Integer(KeyEvent.VK_L));
        
        _exprAction = new ExpressionAction("", Util.getImageIcon("Expression.png"),
        		"Edit Expression", new Integer(KeyEvent.VK_E));

        for (int i = 0; i < MAX_STACK_SIZE; ++i)
        {
            _backMenuItems[i] = new JMenuItem();
            _backMenuItems[i].addActionListener(this);
            _backPopup.add(_backMenuItems[i]);
            _forwardMenuItems[i] = new JMenuItem();
            _forwardMenuItems[i].addActionListener(this);
            _forwardPopup.add(_forwardMenuItems[i]);
        }

        if (ComponentRegistry.isStandAlone())
        {
            JButton newItem = new JButton(editor.newAction);
            newItem.setToolTipText(SimBionicEditor.NEW_COMMAND_TOOLTIP);
            newItem.setText("");
            _toolBarTop.add(newItem);

            JButton openItem = new JButton(editor.openAction);
            openItem.setToolTipText(SimBionicEditor.OPEN_COMMAND_TOOLTIP);
            openItem.setText("");
            _toolBarTop.add(openItem);

            JButton saveItem = new JButton(editor.saveAction);
            saveItem.setText("");
            saveItem.setToolTipText(null);
            _toolBarTop.add(saveItem);

            _toolBarTop.addSeparator();
        }

        _toolBarTop.add(editor.cutAction);
        _toolBarTop.add(editor.copyAction);
        _toolBarTop.add(editor.pasteAction);
        _toolBarTop.addSeparator();
        _toolBarTop.add(editor.undoAction);
        _toolBarTop.add(editor.redoAction);
        _toolBarTop.addSeparator();

        JButton button = new JButton(_backAction);
        button.setText(""); // an icon-only button
        button.setFocusPainted(false);
        // button.setMaximumSize(new Dimension(27, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.addMouseListener(new MouseAdapter()
        {

            public void mousePressed(MouseEvent e)
            {
                if ((e.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0)
                {
                    SB_Behavior behavior;
                    JMenuItem menuItem;
                    int j = _backStack.size() - 1;
                    if (j < 0)
                        return;
                    for (int i = 0; i < MAX_STACK_SIZE; ++i)
                    {
                        menuItem = _backMenuItems[i];
                        if (j >= 0)
                        {
                            behavior = (SB_Behavior) _backStack.get(j);
                            menuItem.setText(behavior.getName());
                            menuItem.setVisible(true);
                            --j;
                        } else
                            menuItem.setVisible(false);
                    }
                    _backPopup.show(SB_ToolBar.this, e.getX(), e.getY());
                }
            }
        });
        _toolBarTop.add(button);

        _toolBarTop.add(Box.createHorizontalStrut(3));

        button = new JButton(_forwardAction);
        button.setText(""); // an icon-only button
        button.setFocusPainted(false);
        // button.setMaximumSize(new Dimension(27, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.addMouseListener(new MouseAdapter()
        {

            public void mousePressed(MouseEvent e)
            {
                if ((e.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0)
                {
                    SB_Behavior behavior;
                    JMenuItem menuItem;
                    int j = _forwardStack.size() - 1;
                    if (j < 0)
                        return;
                    for (int i = 0; i < MAX_STACK_SIZE; ++i)
                    {
                        menuItem = _forwardMenuItems[i];
                        if (j >= 0)
                        {
                            behavior = (SB_Behavior) _forwardStack.get(j);
                            menuItem.setText(behavior.getName());
                            menuItem.setVisible(true);
                            --j;
                        } else
                            menuItem.setVisible(false);
                    }
                    _forwardPopup.show(SB_ToolBar.this, e.getX(), e.getY());
                }
            }
        });
        _toolBarTop.add(button);

        _toolBarTop.add(Box.createHorizontalStrut(3));

        button = new JButton(_homeAction);
        button.setText(""); // an icon-only button
        // button.setMaximumSize(new Dimension(27, 26));
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusPainted(false);
        _toolBarTop.add(button);

        SB_Catalog cat = _editor.getCatalog();
        _toolBarTop.addSeparator();
        _toolBarTop.add(cat._comboBehav);
       // amb commented out 4-27-04, buttons after combo behav weren't appearing
        //cat._comboBehav.setMaximumSize(new Dimension(150, 25));
        _toolBarTop.addSeparator();

//        _toolBarBottom.add(new JLabel(" ", Util.getImageIcon("Expression.gif"), JLabel.RIGHT));
        button = new JButton(_exprAction);
        button.setDisplayedMnemonicIndex(-1);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(23, 21));
        _toolBarBottom.add(button);
        _exprField = _editor.createAutocomplete();
        _exprField.setEnabled(false);
        _exprField.setMaximumSize(new Dimension(375, 21));
        _exprField.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent event)
            {
                SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
                canvas.requestFocus();
            }
        });
        FocusListener[] fls = _exprField.getFocusListeners();
        FocusListener fl = fls[fls.length - 1];
        _exprField.removeFocusListener(fl);
        _exprField.addFocusListener(new FocusListener()
        {

            public void focusGained(FocusEvent event)
            {
                SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
                SB_Drawable selDrawable = canvas._selDrawable;
                _exprField._returnsValue = selDrawable instanceof SB_Condition;
            }

            public void focusLost(FocusEvent event)
            {
                handleFocusLost(_exprField);
            }
        });
        _exprField.addFocusListener(fl);
        _toolBarBottom.add(_exprField);
        _toolBarBottom.add(Box.createHorizontalStrut(5));


        _toolBarBottom.add(new JLabel("", Util.getImageIcon("Bindings.gif"), JLabel.RIGHT));
        _toolBarBottom.add(Box.createHorizontalStrut(5));
        
        _varComboBox = new JComboBox();
        _varComboBox.setFont(_exprField.getFont());
        _varComboBox.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent event)
            {
                if ("comboBoxChanged".equals(event.getActionCommand()))
                {
                    SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
                    SB_Drawable selDrawable = canvas._selDrawable;
                    if (selDrawable instanceof SB_BindingsHolder &&
                    		!(selDrawable instanceof SB_MultiRectangle))
                    {
                        SB_BindingsHolder holder = (SB_BindingsHolder) selDrawable;
                        int i = _varComboBox.getSelectedIndex();
                        if (i != -1)
                        {
                            int size = holder.getBindingCount();
                            if (_varComboBox.getSelectedItem().equals("Insert Binding..."))
                            {
                                _varComboBox.hidePopup();
                                showBindingsDialog(true);
                            } else if (i < size)
                            {
                                _bindingField.setText(holder.getBinding(i).getExpr());
                                _bindingField.setCaretPosition(0);
                            }
                        }
                        canvas.requestFocus();
                    }
                }
            }
        });
        _varComboBox.setEnabled(false);
        _varComboBox.setMaximumSize(new Dimension(145, 21));
        _varComboBox.setPreferredSize(new Dimension(125, 21));
        _toolBarBottom.add(_varComboBox);
        _toolBarBottom.add(Box.createHorizontalStrut(1));
        _bindingsAction.setEnabled(false);
        button = new JButton(_bindingsAction);

        button.setDisplayedMnemonicIndex(-1);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(23, 21));
        // button.setMargin(new Insets(1,2,1,2));
        _toolBarBottom.add(button);
        _toolBarBottom.add(Box.createHorizontalStrut(1));
        _bindingField = _editor.createAutocomplete();
        _bindingField.setEnabled(false);
        _bindingField.setMaximumSize(new Dimension(325, 21));
        _bindingField.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent event)
            {
                SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
                canvas.requestFocus();
            }
        });
        _bindingField.addFocusListener(new FocusListener()
        {

            public void focusGained(FocusEvent event)
            {
            }

            public void focusLost(FocusEvent event)
            {
                handleFocusLost(_bindingField);
            }
        });
        _toolBarBottom.add(_bindingField);
 
        _hackConnectorButton = new JToggleButton(new AbstractAction("",Util.getImageIcon("Connector.gif")) 
        {
            public void actionPerformed(ActionEvent e)
            {
                // do nothing
            }
        });
        _hackConnectorButton.setToolTipText("Draw a connector on the canvas");
        _toolBarTop.add(_hackConnectorButton);
        
        button = new JButton(new LocalsAction("Locals", Util.getImageIcon("Local.gif"),
                "Open the variables local to this behavior", null));
        _toolBarTop.add(button);

        if (ComponentRegistry.isStandAlone())
        {
            _toolBarTop.addSeparator();
    
            button = new JButton(editor.checkErrorAction);
            _toolBarTop.add(button);
    
            _toolBarTop.addSeparator();
    
            _toolBarTop.add(new SB_Button(editor.connectAction));
            _toolBarTop.add(new SB_Button(editor.breakpointAction));
            _toolBarTop.add(new SB_Button(editor.showBreakpointsAction));
        }
        
        add(_toolBarTop);
        add(_toolBarBottom);
    }
    
    /**
     * Disables appropriate toolbar items that should not be available
     * when using debugger.
     * 
     * @param _debugger
     */
    public void setDebugModeOn(){
    	_hackConnectorButton.setEnabled(false);
    	_exprField.setEnabled(false);
    	_bindingField.setEnabled(false);
    	
    	debugMode = true;
    }
    
    /**
     * Reenables appropriate toolbar items upon leaving debug mode.
     *
     */
    public void setDebugModeOff(){
    	_hackConnectorButton.setEnabled(true);
    	debugMode = false;
    }

    public JToggleButton getHackConnectorButton()
    {
        return _hackConnectorButton;
    }
    
    public void addToTop(Component c)
    {
        _toolBarTop.add(c);
    }

    public void addToBottom(Component c)
    {
        _toolBarBottom.add(c);
    }

    public void hideDialogs()
    {
        if (_localsDialog != null)
            _localsDialog.setVisible(false);
    }

    // convenience accessors
    protected SB_ProjectBar getProjectBar()
    {
        return (SB_ProjectBar) ComponentRegistry.getProjectBar();
    }

    protected SB_TabbedCanvas getTabbedCanvas()
    {
        return (SB_TabbedCanvas) ComponentRegistry.getContent();
    }

    public void setEnabled(boolean enabled)
    {
        if (enabled == _homeAction.isEnabled())
            return;

        if (enabled == false)
        {
            _backAction.setEnabled(false);
            _forwardAction.setEnabled(false);
            _homeAction.setEnabled(false);
            _bindingsAction.setEnabled(false);
            // _editor._localsAction.setEnabled(false);

            // _exprField.setText("");
            _exprField.setEnabled(false);
            // _varComboBox.removeAllItems();
            _varComboBox.setEnabled(false);
            // _bindingField.setText("");
            _bindingField.setEnabled(false);

            if (_localsDialog != null)
            {
                _lastVisible = _localsDialog.isVisible();
                if (_lastVisible)
                    _localsDialog.setVisible(false);
            } else
                _lastVisible = false;
        } else
        {
            _backAction.setEnabled(!_backStack.isEmpty());
            _forwardAction.setEnabled(!_forwardStack.isEmpty());
            _homeAction.setEnabled(true);
            // _editor._localsAction.setEnabled(true);

            getTabbedCanvas().getActiveCanvas().clearSingle();
            getTabbedCanvas().getActiveCanvas().updateSingle();

            if (_lastVisible)
                _localsDialog.setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof JButton)
        {
            handleButtonPressed((JButton) e.getSource());
            return;
        }
        
        if (e.getSource() == _filterByTypeCheckBox) {
        	_exprFieldInDialog._filterByType = _filterByTypeCheckBox.isSelected();
        	_exprFieldInDialog.performAutocomplete(_exprFieldInDialog.getText(), _exprFieldInDialog.getCaretPosition());
        	_exprFieldInDialog.requestFocus();
        	return;
        }

        JMenuItem menuItem = (JMenuItem) (e.getSource());

        SB_TabbedCanvas tabbedCanvas = getTabbedCanvas();
        for (int i = 0; i < MAX_STACK_SIZE; ++i)
        {
            if (menuItem == _backMenuItems[i])
            {
                addToForwardStack(tabbedCanvas._behavior);
                int j = _backStack.size();
                while (i >= 1)
                {
                    addToForwardStack((SB_Behavior) _backStack.remove(--j));
                    --i;
                }
                tabbedCanvas.setBehavior((SB_Behavior) _backStack.remove(--j), false);
                break;
            } else if (menuItem == _forwardMenuItems[i])
            {
                addToBackStack(tabbedCanvas._behavior);
                int j = _forwardStack.size();
                while (i >= 1)
                {
                    addToBackStack((SB_Behavior) _forwardStack.remove(--j));
                    --i;
                }
                tabbedCanvas.setBehavior((SB_Behavior) _forwardStack.remove(--j), false);
                break;
            }
        }

        _backAction.setEnabled(!_backStack.isEmpty());
        _forwardAction.setEnabled(!_forwardStack.isEmpty());
    }

    protected void handleButtonPressed(JButton button)
    {
        if (button == _bindingsOK)
        {
            TableCellEditor cellEditor = _bindingsTable.getCellEditor();
            if (cellEditor != null)
                cellEditor.stopCellEditing();
            SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
            SB_BindingsHolder holder = (SB_BindingsHolder) canvas._selDrawable;
            if (!equalBindings(holder.getBindings(), _bindingsTable._bindings))
            {
                canvas._poly.addToUndoStack();
                holder.setBindings(_bindingsTable._bindings);
                canvas.clearSingle();
                canvas.updateSingle();
                canvas.repaint();
                canvas._poly.setModified(true);
            }
            _bindingsDialog.setVisible(false);
            int index = _bindingsTable.getSelectedRow();
            if (index == -1)
                index = 0;
            if (index < holder.getBindingCount() && _varComboBox.isEnabled())
            {
                _varComboBox.setSelectedIndex(index);
                _bindingField.setText(holder.getBinding(index).getExpr());
                _bindingField.setCaretPosition(0);
            }
            canvas.requestFocus();
        } else if (button == _bindingsCancel)
        {
            SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
            SB_BindingsHolder holder = (SB_BindingsHolder) canvas._selDrawable;
            _bindingsDialog.setVisible(false);
            if (_varComboBox.getSelectedIndex() >= holder.getBindingCount() && _varComboBox.isEnabled())
                _varComboBox.setSelectedIndex(0);
        } else if (button == _insertButton)
        {
            _bindingsTable.insertBinding();
        } else if (button == _deleteButton)
        {
            _bindingsTable.deleteBinding();
        } else if (button == _moveUpButton)
        {
            _bindingsTable.moveUp();
        } else if (button == _moveDownButton)
        {
            _bindingsTable.moveDown();
        } else if (button == _setValueButton)
        {
            _bindingsTable.setVarValue();
        }
        else if (button == _exprOK) {
            SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
            SB_Element element = (SB_Element) canvas._selDrawable;
            String expr = _exprFieldInDialog.getText();
            String comment = _expressionComment.getText();
            if (!expr.equals(element.getExpr()) || !comment.equals(element.getComment()))
            {
                canvas._poly.addToUndoStack();
                element.setExpr(expr);
                element.setComment(comment);
                canvas.clearSingle();
                canvas.updateSingle();
                canvas.repaint();
                canvas._poly.setModified(true);
            }
            
        	_exprDialog.setVisible(false);
        }
        else if (button == _exprCancel) {
        	_exprDialog.setVisible(false);
        }
    }

    protected boolean equalBindings(Vector bindings1, Vector bindings2)
    {
        int size = bindings1.size();
        if (size != bindings2.size())
            return false;
        SB_Binding binding1, binding2;
        for (int i = 0; i < size; ++i)
        {
            binding1 = (SB_Binding) bindings1.get(i);
            binding2 = (SB_Binding) bindings2.get(i);
            if (!binding1.equals(binding2))
                return false;
        }
        return true;
    }

    protected void handleFocusLost(SB_Autocomplete autocomplete)
    {
        SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
        SB_Drawable selDrawable = canvas._selDrawable;

        if (autocomplete == _exprField)
        {
            if (selDrawable instanceof SB_Element)
            {
                SB_Element element = (SB_Element) selDrawable;
                if (!element.getExpr().equals(_exprField.getText()))
                {
                    if (!autocomplete._escapePressed)
                    {
                        canvas._poly.addToUndoStack();
                        element.setExpr(_exprField.getText());
                        canvas.repaint();
                        canvas._poly.setModified(true);
                    } else
                    {
                        _exprField.setText(element.getExpr());
                        _exprField.setCaretPosition(0);
                        canvas.requestFocus();
                    }
                }
            }
        } else if (autocomplete == _bindingField)
        {
            if (selDrawable instanceof SB_BindingsHolder &&
            		!(selDrawable instanceof SB_MultiRectangle))
            {
                int i = _varComboBox.getSelectedIndex();
                Vector bindings = ((SB_BindingsHolder) selDrawable).getBindings();
                int size = bindings.size();
                if (i < size)
                {
                    SB_Binding binding = (SB_Binding) bindings.get(i);
                    if (!binding.getExpr().equals(_bindingField.getText()))
                    {
                        if (!autocomplete._escapePressed)
                        {
                            canvas._poly.addToUndoStack();
                            binding.setExpr(_bindingField.getText());
                            canvas._poly.setModified(true);
                        } else
                        {
                            _bindingField.setText(binding.getExpr());
                            canvas.requestFocus();
                        }
                    }
                }
            }
        }
    }
    
    class BackAction extends AbstractAction
    {

        public BackAction(String text, ImageIcon icon, String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            int size = _backStack.size();
            if (size > 0)
            {
                SB_TabbedCanvas tabbedCanvas = getTabbedCanvas();
                SB_Behavior behavior = (SB_Behavior) _backStack.get(size - 1);
                _backStack.remove(size - 1);
                addToForwardStack(tabbedCanvas._behavior);
                tabbedCanvas.setBehavior(behavior, false);
            }
        }
    }

    class ForwardAction extends AbstractAction
    {

        public ForwardAction(String text, ImageIcon icon, String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            int size = _forwardStack.size();
            if (size > 0)
            {
                SB_TabbedCanvas tabbedCanvas = getTabbedCanvas();
                SB_Behavior behavior = (SB_Behavior) _forwardStack.get(size - 1);
                _forwardStack.remove(size - 1);
                addToBackStack(tabbedCanvas._behavior);
                tabbedCanvas.setBehavior(behavior, false);
            }
        }
    }

    class HomeAction extends AbstractAction
    {

        public HomeAction(String text, ImageIcon icon, String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTabbedCanvas().setBehavior(getProjectBar()._catalog._main, true);
        }
    }

    class BindingsAction extends AbstractAction
    {

        public BindingsAction(String text, String desc, Integer mnemonic)
        {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            showBindingsDialog(false);
        }
    }

    class ExpressionAction extends AbstractAction
    {

        public ExpressionAction(String text, ImageIcon icon, String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            showExpressionDialog();
        }
    }

    /**
     * Display a special edit box for compound action node (MultiRect)
     * @param rect
     */
    protected void showEditCompoundActionDialog(SB_Canvas canvas, SB_MultiRectangle rect)
    {
    	if( _compoundActionDialog == null )
    		_compoundActionDialog = new SB_MultiDialog(ComponentRegistry.getFrame(), _editor);
    	
    	_compoundActionDialog.setMultiRectangle(canvas, rect);
    	_compoundActionDialog.setModal(true);
    	_compoundActionDialog.setVisible(true);
    	
    	if(_compoundActionDialog.wasOkClicked())
    	{
            if (!equalBindings(rect.getBindings(), _compoundActionDialog.getBindings()))
            {
				canvas._poly.addToUndoStack();
				rect.setBindings(_compoundActionDialog.getBindings());
				canvas.clearSingle();
				canvas.updateSingle();
				canvas.repaint();
				canvas._poly.setModified(true);
	    		canvas.requestFocus();
            }
    	}

    }
    
    protected void showBindingsDialog(boolean insert)
    {
    	SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
        SB_BindingsHolder holder = (SB_BindingsHolder) canvas._selDrawable;
        
        if(holder instanceof SB_MultiRectangle)
        {
        	showEditCompoundActionDialog(canvas, (SB_MultiRectangle) holder);
        	return;
        }
        
        if (_bindingsDialog == null)
        {
            _bindingsDialog = new JDialog(ComponentRegistry.getFrame(), "Edit Bindings", true);
            _bindingsTable = new SB_BindingsTable(_editor);
            JScrollPane scrollPane = new JScrollPane(_bindingsTable);
            scrollPane.setPreferredSize(new Dimension(525, 175));

            // _bindingsDialog.getContentPane().add(scrollPane);
            // _bindingsDialog.pack();

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            buttonPanel.add(Box.createHorizontalGlue());
            _insertButton = new JButton("Insert");
            _insertButton.setFocusPainted(false);
            _insertButton.addActionListener(this);
            buttonPanel.add(_insertButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            _deleteButton = new JButton("Delete");
            _deleteButton.setFocusPainted(false);
            _deleteButton.addActionListener(this);
            buttonPanel.add(_deleteButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            _setValueButton = new JButton("Set Value");
            _setValueButton.setFocusPainted(false);
            _setValueButton.addActionListener(this);
            buttonPanel.add(_setValueButton);
            _moveUpButton = new JButton("Move Up");
            _moveUpButton.setFocusPainted(false);
            _moveUpButton.addActionListener(this);
            buttonPanel.add(_moveUpButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            _moveDownButton = new JButton("Move Down");
            _moveDownButton.setFocusPainted(false);
            _moveDownButton.addActionListener(this);
            buttonPanel.add(_moveDownButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            _bindingsOK = new JButton("OK");
            _bindingsOK.setFocusPainted(false);
            _bindingsOK.addActionListener(this);
            buttonPanel.add(_bindingsOK);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            _bindingsCancel = new JButton("Cancel");
            _bindingsCancel.setFocusPainted(false);
            _bindingsCancel.addActionListener(this);
            buttonPanel.add(_bindingsCancel);
            buttonPanel.add(Box.createHorizontalGlue());

            _bindingsDialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
            _bindingsDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            _bindingsDialog.pack();

            Dimension dialogSize = _bindingsDialog.getSize();
            Rectangle frameBounds = ComponentRegistry.getFrame().getBounds();
            _bindingsDialog.setLocation(frameBounds.x + (frameBounds.width - dialogSize.width) / 2,
                frameBounds.y + (frameBounds.height - dialogSize.height) / 2);
        }
        
        _bindingsTable.setBindings(canvas._poly, holder.getBindings(), insert);
        System.out.println("Bindings dialog: " + !debugMode);
        _insertButton.setEnabled(!debugMode);
        _deleteButton.setEnabled(!debugMode);
        _moveUpButton.setEnabled(!debugMode);
        _moveDownButton.setEnabled(!debugMode);
        _bindingsTable.setEnabled(!debugMode);
        _bindingsOK.requestFocus();
        _bindingsDialog.setVisible(true);
    }
    
    protected void showExpressionDialog() {
    	SB_Canvas canvas = getTabbedCanvas().getActiveCanvas();
        SB_Element element = (SB_Element) canvas._selDrawable;
        if (_exprDialog == null) {
        	createExpressionDialog();
        }
        
        _exprFieldInDialog.removeAutoCompleteListener(this);
        _exprFieldInDialog.setReturnsValue(element instanceof SB_Condition);
        _exprFieldInDialog.clearNames();
        _exprFieldInDialog.initializeNames();
        String expr = element.getExpr();
        _exprFieldInDialog.setText(expr);
        _exprFieldInDialog.setCaretPosition(0);
        _exprFieldInDialog.addAutoCompleteListener(this);
        _exprFieldInDialog.startAutoComplete();
        if (expr.length() == 0)
        	_exprFieldInDialog.requestFocus();
        else
        	_exprOK.requestFocus();
        
        _expressionComment.setText(element.getComment());
        _exprDialog.setVisible(true);
    }
    
    /**
     * create the UI for the expression variable represented by {@link #_exprDialog}
     */
    protected void createExpressionDialog() {
    	 if (_exprDialog != null) return;
    	
    	_exprFieldInDialog = _editor.createAutocompleteTextArea();
    	_exprFieldInDialog.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    	JPanel exprPanel = new TitledComponentPanel("Expression:", _exprFieldInDialog);


    	_expressionComment = new JTextArea(3,20);
    	_expressionComment.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    	
    	JPanel expressionCommentPanel = new TitledComponentPanel("Comment:", _expressionComment);

    	//the code below comments our some UI that is not longer used like
    	//the list of behaviors, predicates, etc.
    	//However there is still code somewhere that refers to UI elements: see
    	//references to _actionPredList,_actionPredListTitle,_variableList,etc.
    	
    	//JPanel listPanel = new JPanel();
    	//listPanel.setLayout(new GridLayout(1,2));
    	
    	
    	_actionPredModel = new DefaultListModel();
    	_actionPredList = new JList(_actionPredModel);
    	_actionPredList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    	_actionPredList.addListSelectionListener(this);
    	_actionPredList.addMouseListener(this);
    	_actionPredList.addMouseMotionListener(this);

    	//JScrollPane scrollPane = new JScrollPane(_actionPredList);
    	//scrollPane.setPreferredSize(new Dimension(85, 150));
    	
    	_actionPredListTitle = new JLabel("Action List:");
    	
    	//TitledComponentPanel tcPanel = new TitledComponentPanel(_actionPredListTitle, scrollPane);
    	//tcPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
    	//listPanel.add(tcPanel);
    	
    	_variableModel = new DefaultListModel();
    	_variableList = new JList(_variableModel);
    	_variableList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    	_variableList.addListSelectionListener(this);
    	_variableList.addMouseListener(this);
    	
    	//scrollPane = new JScrollPane(_variableList);
    	//scrollPane.setPreferredSize(new Dimension(85, 150));
    	//tcPanel = new TitledComponentPanel("Variable List:", scrollPane);
    	//tcPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    	//listPanel.add(tcPanel);

    	JPanel bottomPanel = new JPanel(new BorderLayout());
    	_paramModel = new DefaultListModel();
    	_paramList = new JList(_paramModel);
    	_paramList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    
    	//scrollPane = new JScrollPane(_paramList);
    	//scrollPane.setPreferredSize(new Dimension(100, 82));
    	//bottomPanel.add(new TitledComponentPanel("Parameter List:", scrollPane), BorderLayout.CENTER);

    	JPanel buttonPanel = new JPanel();
    	//buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    	_filterByTypeCheckBox = new JCheckBox("Filter By Type");
    	_filterByTypeCheckBox.addActionListener(this);
    	_filterByTypeCheckBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
    	
    	//buttonPanel.add(_filterByTypeCheckBox);
    	//buttonPanel.add(Box.createVerticalGlue());
    	
    	_exprOK = new JButton("   OK   ");
    	_exprOK.setAlignmentX(JButton.CENTER_ALIGNMENT);
    	_exprOK.addActionListener(this);
    	buttonPanel.add(_exprOK);
    	
    	_exprCancel = new JButton("Cancel");
    	//buttonPanel.add(Box.createVerticalStrut(5));
    	buttonPanel.add(_exprCancel);
    	_exprCancel.setAlignmentX(JButton.CENTER_ALIGNMENT);
    	_exprCancel.addActionListener(this);
    	
    	//buttonPanel.add(Box.createVerticalStrut(5));
    	buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
    	bottomPanel.add(buttonPanel, BorderLayout.CENTER);

    	exprPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
    	expressionCommentPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
    	//listPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 7));
    	bottomPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

    	//create UI
    	
    	_exprDialog = new JDialog(ComponentRegistry.getFrame(), "Edit Expression", true);
    	_exprDialog.getContentPane().setLayout(new BorderLayout());
    	
    	JScrollPane scroll = new JScrollPane(expressionCommentPanel);
    	scroll.setBorder(null);
    	_exprDialog.getContentPane().add(scroll,BorderLayout.NORTH);
    	
    	scroll = new JScrollPane(exprPanel);
    	scroll.setBorder(null);
    	_exprDialog.getContentPane().add(scroll, BorderLayout.CENTER);
    	
    	//_exprDialog.getContentPane().add(listPanel, BorderLayout.CENTER);
    	_exprDialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

    	Dimension dialogSize = new Dimension(395, 450);
    	_exprDialog.setPreferredSize(dialogSize);

    	_exprDialog.pack();

    	//            Dimension dialogSize = _exprDialog.getSize();
    	Rectangle frameBounds = ComponentRegistry.getFrame().getBounds();
    	_exprDialog.setLocation(frameBounds.x + (frameBounds.width - dialogSize.width) / 2,
    			frameBounds.y + (frameBounds.height - dialogSize.height) / 2);
    }
    
    
    class LocalsAction extends AbstractAction
    {

        public LocalsAction(String text, ImageIcon icon, String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            if (mnemonic != null)
                putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            SB_LocalsTree localsTree = ComponentRegistry.getLocalsTree();
            if (_localsDialog == null)
            {
                _localsDialog = new JDialog(ComponentRegistry.getFrame(), "Local Variables", false);
                JScrollPane scrollPane = new JScrollPane(localsTree);
                scrollPane.setPreferredSize(new Dimension(188, 217));
                _localsDialog.getContentPane().add(scrollPane);
                _localsDialog.pack();

                Dimension dialogSize = _localsDialog.getSize();
                Rectangle frameBounds = ComponentRegistry.getFrame().getBounds();
                _localsDialog.setLocation(
                    frameBounds.x + frameBounds.width - dialogSize.width - 40, frameBounds.y + 118);
                _localsDialog.addWindowListener(new WindowAdapter()
                {

                    public void windowClosing(WindowEvent e)
                    {
                        getTabbedCanvas().getActiveCanvas().requestFocus();
                    }
                });
            }
            DefaultMutableTreeNode locals = getTabbedCanvas().getActiveCanvas()._poly.getLocals();
            localsTree.setRoot(locals);
            _localsDialog.setVisible(true);
        }
    }

    protected void addToBackStack(SB_Behavior behavior)
    {
        int size = _backStack.size();
        if (size >= MAX_STACK_SIZE)
            _backStack.remove(0);
        _backStack.add(behavior);
    }

    protected void addToForwardStack(SB_Behavior behavior)
    {
        int size = _forwardStack.size();
        if (size >= MAX_STACK_SIZE)
            _forwardStack.remove(0);
        _forwardStack.add(behavior);
    }

    protected void clearStacks()
    {
        _backStack.clear();
        _forwardStack.clear();
        _backAction.setEnabled(false);
        _forwardAction.setEnabled(false);
    }

    protected void removeBehavior(SB_Behavior behavior)
    {
        while (_backStack.remove(behavior))
            ;
        while (_forwardStack.remove(behavior))
            ;
        SB_ProjectBar pb = ComponentRegistry.getProjectBar();
        SB_Catalog cat = pb._catalog;
        if (cat._comboBehav != null)
            cat._comboBehav.removeItem(behavior);

        SB_Behavior prevBehavior = null;
        int size = _backStack.size();
        for (int i = size - 1; i >= 0; --i)
        {
            behavior = (SB_Behavior) _backStack.get(i);
            if (behavior == prevBehavior)
                _backStack.remove(i);
            else
                prevBehavior = behavior;
        }
        size = _backStack.size();
        if (size > 0 && _backStack.get(size - 1) == getTabbedCanvas()._behavior)
        {
            _backStack.remove(size - 1);
            --size;
        }
        _backAction.setEnabled(size > 0);

        prevBehavior = null;
        size = _forwardStack.size();
        for (int i = size - 1; i >= 0; --i)
        {
            behavior = (SB_Behavior) _forwardStack.get(i);
            if (behavior == prevBehavior)
                _forwardStack.remove(i);
            else
                prevBehavior = behavior;
        }
        size = _forwardStack.size();
        if (size > 0 && _forwardStack.get(size - 1) == getTabbedCanvas()._behavior)
        {
            _forwardStack.remove(size - 1);
            --size;
        }
        _forwardAction.setEnabled(size > 0);
    }
    
    // XXX: MOTL
    public Action getBackAction() {
        return _backAction;
    }
    
    // XXX: MOTL
    public Action getForwardAction() {
        return _forwardAction;
    }

	@Override
	public void matchListChanged(Vector matchList, String funcName, String paramName, int paramIndex) {
		if (paramIndex == -1 && !_exprFieldInDialog.returnsValue()) {
			_actionPredListTitle.setText("Action/Behavior List:");
			_matchingPred = false;
		}
		else {
			_actionPredListTitle.setText("Predicate List:");
			_matchingPred = true;
		}
		
		_actionPredModel.clear();
		_variableModel.clear();
		for (Object match : matchList) {
			String matchText = (String) match;
			int pos = matchText.indexOf('(');
			if (pos != -1) {
				String matchName = matchText.substring(0, pos);
				_actionPredModel.addElement(matchName);
			}
			else {
				pos = matchText.indexOf(' ');
				if (pos!=-1) {
					String matchName = matchText.substring(0, pos);
					_variableModel.addElement(matchName);
				}
			}
		}
		
		_paramModel.clear();
		
		// typing outermost function
		if (paramIndex == -1 && _actionPredModel.getSize() > 0 && funcName.length() > 0) {
			_actionPredList.setSelectedIndex(0);
			populateParamList();
		}
		
		// typing inner function
		if (paramIndex != -1 && _actionPredModel.getSize() > 0 && paramName.length() > 0) {
			_actionPredList.setSelectedIndex(0);
			populateParamList();
		}
		
		if (_actionPredModel.getSize() == 0 && _variableModel.size() > 0) {
			_variableList.setSelectedIndex(0);
		}
		
		if (_actionPredList.getSelectedIndex() == -1 && _variableList.getSelectedIndex() == -1) {
			_exprFieldInDialog.changeMatchSelection(null);
		}
		
		if (paramIndex != -1 && (_actionPredModel.getSize() == 0 || paramName.length() == 0)) {
			DefaultMutableTreeNode funcNode = _exprFieldInDialog.getPredicate(funcName);
			if (funcNode == null) {
				funcNode = _exprFieldInDialog.getActionBehavior(funcName);
			}
			populateParamList(funcNode);
			if (paramIndex < _paramModel.size())
				_paramList.setSelectedIndex(paramIndex);
		}
	}

	@Override
	public void matchSelectionChanged(String matchSel) {
		int pos = matchSel.indexOf('(');
		if (pos != -1) {
			String matchName = matchSel.substring(0, pos);
			_actionPredList.setSelectedValue(matchName, true);
			_variableList.clearSelection();
			populateParamList();
		}
		else {
			pos = matchSel.indexOf(' ');
			String matchName = matchSel.substring(0, pos);
			_actionPredList.clearSelection();
			_variableList.setSelectedValue(matchName, true);
		}
	}

	private void populateParamList() {
		_paramModel.clear();
		String funcName = (String) _actionPredList.getSelectedValue();
		if (funcName != null) {
			DefaultMutableTreeNode funcNode = null;
			if (_matchingPred) {
				funcNode = _exprFieldInDialog.getPredicate(funcName);
			}
			else {
				funcNode = _exprFieldInDialog.getActionBehavior(funcName);
			}
			populateParamList(funcNode);
		}
	}
	
	private void populateParamList(DefaultMutableTreeNode funcNode) {
		if (funcNode != null) {
			int size = funcNode.getChildCount();
			for (int i = 0; i < size; ++i) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) funcNode.getChildAt(i);
				SB_Parameter param = ((SB_Parameter) childNode.getUserObject());
				_paramModel.addElement(param.getName() + " : " + param.getFullTypeName());
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			JList list = (JList) e.getSource();
			if (list == _actionPredList) {
//				System.out.println("valueChanged: actionPredList");
				_variableList.clearSelection();
				populateParamList();
				_exprFieldInDialog.changeMatchSelection((String) _actionPredList.getSelectedValue());
				_exprFieldInDialog.requestFocus();
			}
			else if (list == _variableList) {
				_actionPredList.clearSelection();
				_exprFieldInDialog.changeMatchSelection((String) _variableList.getSelectedValue());
				_exprFieldInDialog.requestFocus();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getClickCount() == 2) {
			JList list = (JList) e.getSource();
			String text = (String) list.getSelectedValue();
			if (text != null) {
				completeExpression(text);
			}
		}
	}

	@Override
	public void completeExpression() {
		String text = (String) _actionPredList.getSelectedValue();
		if (text == null)
			text = (String) _variableList.getSelectedValue();
		if (text != null)
			completeExpression(text);
	}
	
	private void completeExpression(String text) {
//		int selStart = _exprFieldInDialog.getSelectionStart();
//		int selEnd = _exprFieldInDialog.getSelectionStart();
//		try {
//			_exprFieldInDialog.getDocument().remove(selStart, selEnd - selStart);
//		}
//		catch (BadLocationException e) {
//			e.printStackTrace();
//		}
		
		String expr = _exprFieldInDialog.getText();
		int endPos = _exprFieldInDialog.getCaretPosition();
		int startPos = endPos - 1;
		while (startPos >= 0 && isValidChar(expr.charAt(startPos))) {
			startPos--;
		}
		int n = expr.length();
		while (endPos < n && isValidChar(expr.charAt(endPos))) {
			endPos++;
		}
		startPos++;
		expr = expr.substring(0, startPos) + text + expr.substring(endPos);
		_exprFieldInDialog.removeAutoCompleteListener(this);
		_exprFieldInDialog.setText(expr);
		_exprFieldInDialog.addAutoCompleteListener(this);
		_exprFieldInDialog.setCaretPosition(startPos + text.length());
		_exprFieldInDialog.requestFocus();
	}
	
	private boolean isValidChar(char c) {
		return ('a' <= c && c <= 'z')  || ('A' <= c && c <= 'Z')
				 || ('0' <= c && c <= '1') || c == '_';
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getSource() == _actionPredList) {
	        _actionPredList.setToolTipText(null);
			int index = _actionPredList.locationToIndex(e.getPoint());
			if (index != -1) {
				String funcName = (String) _actionPredModel.getElementAt(index);
				DefaultMutableTreeNode funcNode = null;
				if (_matchingPred) {
					funcNode = _exprFieldInDialog.getPredicate(funcName);
				}
				else {
					funcNode = _exprFieldInDialog.getActionBehavior(funcName);
				}
				if (funcNode==null) return;
		        SB_Function func = ((SB_Function) funcNode.getUserObject());
		        String description = func.getDescription();
		        if (description.length() > 0)
		        	_actionPredList.setToolTipText(func.getDescription());
			}
		}
	}
}
