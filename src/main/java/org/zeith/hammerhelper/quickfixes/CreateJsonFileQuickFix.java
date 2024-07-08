package org.zeith.hammerhelper.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CreateJsonFileQuickFix
		implements LocalQuickFix
{
	protected static final Logger LOG = LoggerFactory.getLogger("CreateJsonFileQuickFix");
	
	protected final String name;
	protected final String fileContent;
	protected final int caretOffset;
	
	@SafeFieldForPreview
	protected final VFF file;
	
	public CreateJsonFileQuickFix(String name, String fileContent, int caretOffset, VFF file)
	{
		this.name = name;
		this.fileContent = fileContent;
		this.caretOffset = caretOffset;
		this.file = file;
	}
	
	public static VirtualFile createFile(Project project, Object requestor, String path, String fileName)
			throws IOException
	{
		// Define the file path and create the JSON file
		if(!path.startsWith("/")) path = "/" + path;
		String directoryPath = project.getBasePath() + path;
		VirtualFile directory = VfsUtil.createDirectories(directoryPath);
		return directory.createChildData(requestor, fileName);
	}
	
	@Override
	public @IntentionFamilyName @NotNull String getFamilyName()
	{
		return name;
	}
	
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor)
	{
		CreateJsonFileQuickFix deez = this;
		
		try
		{
			ApplicationManager.getApplication().invokeLater(() ->
			{
				WriteCommandAction.runWriteCommandAction(project, () ->
				{
					try
					{
						var file = this.file.apply(project, deez);
						VfsUtil.saveText(file, fileContent);
						openFileInEditorWithCaretPosition(project, file, caretOffset);
					} catch(Throwable e)
					{
						throw new IncorrectOperationException("Error creating JSON file", e);
					}
				});
			});
		} catch(RuntimeException err)
		{
			if(err.getMessage().contains("INVOKE_LATER"))
				LOG.info("Side effects are currently disallowed, soft-failing.");
			else
				throw err;
		}
	}
	
	private void openFileInEditorWithCaretPosition(Project project, VirtualFile file, int caretOffset)
	{
		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		fileEditorManager.openFile(file, true);
		
		// Wait for the file to be opened
		ApplicationManager.getApplication().invokeLater(() ->
		{
			Editor[] editors = EditorFactory.getInstance().getEditors(fileEditorManager.getSelectedTextEditor().getDocument(), project);
			if(editors.length > 0)
			{
				Editor editor = editors[0];
				Document document = editor.getDocument();
				if(caretOffset <= document.getTextLength())
					editor.getCaretModel().moveToOffset(caretOffset);
			}
		});
	}
	
	public interface VFF
	{
		VirtualFile apply(Project project, Object requestor)
				throws IOException;
	}
}