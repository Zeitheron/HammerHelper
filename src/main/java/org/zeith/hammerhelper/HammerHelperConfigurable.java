package org.zeith.hammerhelper;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.ui.MapSettingsComponent;

import javax.swing.*;
import java.awt.*;

public class HammerHelperConfigurable
		implements Configurable
{
	private final MapSettingsComponent mapSettingsComponent = new MapSettingsComponent("Path", "Namespace");
	
	@Override
	public @NlsContexts.ConfigurableName String getDisplayName()
	{
		return "HammerHelper";
	}
	
	@Override
	public @Nullable JComponent createComponent()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel generatorLabel = new JLabel("HammerHelper");
		generatorLabel.setFont(generatorLabel.getFont().deriveFont(18.0f));
		panel.add(generatorLabel);

//		addCheckboxWithTooltip(panel, jCheckBox, "");
		
		panel.add(mapSettingsComponent.getMainPanel());
		return panel;
	}
	
	@Override
	public boolean isModified()
	{
		return !mapSettingsComponent.getMap().equals(HammerHelper.cfg(HammerHelper.currentProject()).getNamespaces());
	}
	
	@Override
	public void apply()
			throws ConfigurationException
	{
		var proj = HammerHelper.currentProject();
		var cfgs = HammerHelper.cfg(proj);
		cfgs.setNamespaces(mapSettingsComponent.getMap());
	}
	
	@Override
	public void reset()
	{
		mapSettingsComponent.setMap(HammerHelper.cfg(HammerHelper.currentProject()).getNamespaces());
	}
	
	private void addCheckboxWithTooltip(JPanel panel, JCheckBox checkBox, String tooltip)
	{
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		checkBoxPanel.add(checkBox);
		checkBoxPanel.add(createTooltipLabel(tooltip));
		panel.add(checkBoxPanel);
	}
	
	private JLabel createTooltipLabel(String tooltipText)
	{
		JLabel label = new JLabel(AllIcons.General.TodoQuestion);
		label.setToolTipText(tooltipText);
		return label;
	}
}