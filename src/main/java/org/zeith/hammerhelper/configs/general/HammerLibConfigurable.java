package org.zeith.hammerhelper.configs.general;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.*;

public class HammerLibConfigurable
		implements Configurable
{
	protected GeneralConfigsHH configs = GeneralConfigsHH.pull();
	
	public HammerLibConfigurable()
	{
	}
	
	@Override
	public @NlsContexts.ConfigurableName String getDisplayName()
	{
		return "HammerHelper";
	}
	
	@Override
	public @Nullable JComponent createComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		JLabel nsLbl = new JLabel("HammerHelper");
		nsLbl.setFont(nsLbl.getFont().deriveFont(18.0f));
		p.add(nsLbl);
		
		p.add(makeToggle("Enable MC Model Refs",
				"If enabled, you will be able to navigate through minecraft json model/blockstate files.",
				GeneralConfigsHH::enableMCJsonReferences, GeneralConfigsHH::withEnableMCJsonReferences
		));
		
		return p;
	}
	
	protected JPanel makeToggle(
			String title,
			String tooltip,
			Predicate<GeneralConfigsHH> enabled,
			BiFunction<GeneralConfigsHH, Boolean, GeneralConfigsHH> op
	)
	{
		JCheckBox box = new JCheckBox(title);
		box.setSelected(enabled.test(configs));
		box.addActionListener(e -> configs = op.apply(configs, box.isSelected()));
		return addCheckboxWithTooltip(box, tooltip);
	}
	
	@Override
	public boolean isModified()
	{
		return !configs.equals(GeneralConfigsHH.pull());
	}
	
	@Override
	public void apply()
			throws ConfigurationException
	{
		configs.push();
	}
	
	private JPanel addCheckboxWithTooltip(JCheckBox checkBox, String tooltip)
	{
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		checkBoxPanel.add(checkBox);
		if(tooltip != null && !tooltip.isBlank()) checkBoxPanel.add(createTooltipLabel(tooltip));
		return checkBoxPanel;
	}
	
	private JLabel createTooltipLabel(String tooltipText)
	{
		JLabel label = new JLabel(AllIcons.General.TodoQuestion);
		label.setToolTipText(tooltipText);
		return label;
	}
}