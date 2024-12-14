package org.zeith.hammerhelper.configs.namespaces;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.ui.MapSettingsComponent;

import javax.swing.*;

public class NamespacesConfigurable
		implements Configurable
{
	private final MapSettingsComponent mapSettingsComponent = new MapSettingsComponent("Path", "Namespace");
	
	protected final Project project;
	
	public NamespacesConfigurable(Project project)
	{
		this.project = project;
	}
	
	@Override
	public @NlsContexts.ConfigurableName String getDisplayName()
	{
		return "Extra Namespaces";
	}
	
	@Override
	public @Nullable JComponent createComponent()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JLabel nsLbl = new JLabel("Extra Namespaces | Project-Level");
		nsLbl.setFont(nsLbl.getFont().deriveFont(18.0f));
		panel.add(nsLbl);

		panel.add(mapSettingsComponent.getMainPanel());
		
		return panel;
	}
	
	@Override
	public boolean isModified()
	{
		return !mapSettingsComponent.getMap().equals(new NamespaceConfigsHH(project).getNamespaces());
	}
	
	@Override
	public void apply()
			throws ConfigurationException
	{
		var cfgs = new NamespaceConfigsHH(project);
		cfgs.setNamespaces(mapSettingsComponent.getMap());
	}
	
	@Override
	public void reset()
	{
		mapSettingsComponent.setMap(new NamespaceConfigsHH(project).getNamespaces());
	}
}