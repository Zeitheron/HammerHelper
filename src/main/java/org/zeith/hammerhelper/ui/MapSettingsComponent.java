package org.zeith.hammerhelper.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.HashMap;

public class MapSettingsComponent
{
	private JPanel mainPanel;
	private JTable table;
	private DefaultTableModel tableModel;
	
	private final String key, value;
	
	public MapSettingsComponent(String key, String value)
	{
		this.key = key;
		this.value = value;
		
		mainPanel = new JPanel(new BorderLayout());
		// remove border color
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		// Add add button
		JButton addButton = new JButton(AllIcons.ToolbarDecorator.AddBlankLine);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		addButton.addActionListener(e -> openAddDialog());
		buttonPanel.add(addButton);
		JButton removeButton = new JButton(AllIcons.Diff.Remove);
		removeButton.addActionListener(e ->
		{
			int selectedRow = table.getSelectedRow();
			if(selectedRow != -1)
			{
				tableModel.removeRow(selectedRow);
			}
		});
		buttonPanel.add(removeButton);
		mainPanel.add(buttonPanel, BorderLayout.NORTH);
		tableModel = new DefaultTableModel(new Object[] { key, value }, 0);
		table = new JBTable(tableModel);
		table.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_INSERT)
				{
					openAddDialog();
				}
				if(e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					int selectedRow = table.getSelectedRow();
					if(selectedRow != -1)
					{
						tableModel.removeRow(selectedRow);
					}
				}
			}
		});
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	public JPanel getMainPanel()
	{
		return mainPanel;
	}
	
	public void setMap(Map<String, String> map)
	{
		tableModel.setRowCount(0); // Clear existing rows
		map.forEach((key, value) -> tableModel.addRow(new Object[] { key, value }));
	}
	
	public Map<String, String> getMap()
	{
		Map<String, String> map = new HashMap<>();
		for(int i = 0; i < tableModel.getRowCount(); i++)
		{
			map.put((String) tableModel.getValueAt(i, 0), (String) tableModel.getValueAt(i, 1));
		}
		return map;
	}
	
	private void openAddDialog()
	{
		JDialog addDialog = new JDialog();
		addDialog.setLayout(new GridLayout(3, 2));
		
		// Name field
		addDialog.add(new JLabel(key + ":"));
		JTextField nameField = new JTextField();
		addDialog.add(nameField);
		
		// Reference field
		addDialog.add(new JLabel(value + ":"));
		JTextField referenceField = new JTextField();
		addDialog.add(referenceField);
		
		// Add button
		JButton addButton = new JButton("Add");
		addButton.addActionListener(e ->
		{
			String name = nameField.getText();
			String reference = referenceField.getText();
			if(!name.isEmpty() && !reference.isEmpty())
			{
				tableModel.addRow(new Object[] { name, reference });
				addDialog.dispose();
			}
		});
		//center the button
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addDialog.add(addButton);
		
		// Setup dialog properties
		addDialog.pack();
		addDialog.setLocationRelativeTo(mainPanel);
		addDialog.setModal(true);
		addDialog.setVisible(true);
	}
}