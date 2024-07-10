package main.java.com.own.plugin.bpep.view;

import static org.eclipse.swt.SWT.APPLICATION_MODAL;
import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.CHECK;
import static org.eclipse.swt.SWT.DIALOG_TRIM;
import static org.eclipse.swt.SWT.HORIZONTAL;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.RADIO;
import static org.eclipse.swt.SWT.SHADOW_ETCHED_IN;
import static org.eclipse.swt.SWT.Selection;
import static org.eclipse.swt.SWT.VERTICAL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import main.java.com.own.plugin.bpep.generator.BuilderGenerator;
import main.java.com.own.plugin.bpep.generator.ToStringGenerator;
import main.java.com.own.plugin.bpep.resolver.Resolver;

public class MyOwnDialog extends AbstractModalDialog {

    public MyOwnDialog(Shell parent) {
        super(parent);
    }

    public void show(final ICompilationUnit compilationUnit) throws JavaModelException {
        final Shell shell = new Shell(getParent(), DIALOG_TRIM | APPLICATION_MODAL | CENTER);

        shell.setText("Generate My Own Code");
        shell.setLayout(new GridLayout(2, false));

        Group typeGroup = new Group(shell, SHADOW_ETCHED_IN);
        typeGroup.setText("Select builders to use:");
        typeGroup.setLayout(new RowLayout(HORIZONTAL));
        GridData typeGroupLayoutData = new GridData();
        typeGroupLayoutData.horizontalSpan = 2;
        typeGroup.setLayoutData(typeGroupLayoutData);
        final Button toStringButton = new Button(typeGroup, RADIO);
        toStringButton.setText("toString()");
        final Button builderButton = new Button(typeGroup, RADIO);
        builderButton.setText("builder()");
        final Button bothButton = new Button(typeGroup, RADIO);
        bothButton.setText("both");
        
        Group fieldGroup = new Group(shell, SHADOW_ETCHED_IN);
        fieldGroup.setText("Select fields to include:");
        fieldGroup.setLayout(new RowLayout(VERTICAL));
        GridData fieldGroupLayoutData = new GridData();
        fieldGroupLayoutData.verticalSpan = 2;
		fieldGroup.setLayoutData(fieldGroupLayoutData);

        final List<Button> fieldButtons = createFieldSelectionCheckboxes(compilationUnit, fieldGroup);
        createSelectAllButton(shell, fieldButtons);
        createSelectNoneButton(shell, fieldButtons);
        
        Group optionGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        optionGroup.setText("Options:");
        optionGroup.setLayout(new RowLayout(SWT.VERTICAL));
        GridData optionGridData = new GridData();
        optionGridData.horizontalSpan = 2;
        optionGridData.horizontalAlignment = SWT.FILL;
		optionGroup.setLayoutData(optionGridData);

        createCreateClassConstructorOption(optionGroup);
        
        final Button createBuilderConstructor = new Button(optionGroup, SWT.RADIO);
        createBuilderConstructor.setText("Create constructor in builder");

        final Button createCopyConstructorButton = new Button(optionGroup, SWT.CHECK);
        createCopyConstructorButton.setSelection(true);
        createCopyConstructorButton.setText("Create copy constructor in builder");

        final Button formatSourceButton = new Button(optionGroup, SWT.CHECK);
        formatSourceButton.setSelection(true);
        formatSourceButton.setText("Format source (entire file)");
        
        final Button executeButton = new Button(typeGroup, PUSH);
        executeButton.setText("Generate");
        shell.setDefaultButton(executeButton);
        final Button cancelButton = new Button(typeGroup, PUSH);
        cancelButton.setText("Cancel");

        Listener clickListener = new Listener() {
        	int type = 1;
        	public void handleEvent(Event event) {
        		if (event.widget == toStringButton) {
        			type = 1;
        		} else if (event.widget == builderButton) {
        			type = 2;
        		} else if (event.widget == bothButton) {
        			type = 3;
        		} else if (event.widget == executeButton) {
        			List<IField> selectedFields = new ArrayList<IField>();
        			for (Button button : fieldButtons) {
						if (button.getSelection()) {
							selectedFields.add((IField)button.getData());
						}
					}
        			if (type == 2 || type == 3) {
        				new BuilderGenerator().generate(compilationUnit, selectedFields);
        			}
        			if (type == 1 || type == 3) {
        				new ToStringGenerator().generate(compilationUnit, selectedFields);
        			}
        			shell.dispose();
        		} else {
        			shell.dispose();
        		}
        	}
        };

        toStringButton.addListener(Selection, clickListener);
        builderButton.addListener(Selection, clickListener);
        bothButton.addListener(Selection, clickListener);
        executeButton.addListener(Selection, clickListener);
        cancelButton.addListener(Selection, clickListener);

        display(shell);
    }

	private List<Button> createFieldSelectionCheckboxes(final ICompilationUnit compilationUnit, Group fieldGroup) {
		List<IField> fields = Resolver.findAllFields(compilationUnit);
		final List<Button> fieldButtons = new ArrayList<Button>();
		for (IField field : fields) {
			Button button = new Button(fieldGroup, CHECK);
			button.setText(Resolver.getName(field) + "(" + Resolver.getType(field) + ")");
			button.setData(field);
			button.setSelection(true);
			fieldButtons.add(button);
		}
		return fieldButtons;
	}

	private void createSelectAllButton(final Shell shell, final List<Button> fieldButtons) {
		Button btnSelectAll = new Button(shell, SWT.PUSH);
		btnSelectAll.setText("Select All");
		GridData btnSelectAllLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		btnSelectAllLayoutData.verticalIndent = 10;
		btnSelectAll.setLayoutData(btnSelectAllLayoutData);
		btnSelectAll.addSelectionListener(new FieldSelectionAdapter(fieldButtons, true));
	}

	private void createSelectNoneButton(final Shell shell, final List<Button> fieldButtons) {
		Button btnSelectNone = new Button(shell, SWT.PUSH);
		btnSelectNone.setText("Deselect All");
		GridData selectNoneGridData = new GridData();
		selectNoneGridData.verticalAlignment = SWT.BEGINNING;
		btnSelectNone.setLayoutData(selectNoneGridData);
		btnSelectNone.addSelectionListener(new FieldSelectionAdapter(fieldButtons, false));
	}
	
	private void createCreateClassConstructorOption(Group optionGroup) {
		final Button createClassConstructor = new Button(optionGroup, SWT.RADIO);
		createClassConstructor.setSelection(true);
		createClassConstructor.setText("Create class constructor");
	}
	
	private class FieldSelectionAdapter extends SelectionAdapter {
		private final List<Button> buttons;
		private final boolean checked;

		public FieldSelectionAdapter(final List<Button> buttons, final boolean checked) {
			this.buttons = buttons;
			this.checked = checked;
		}

		@Override
		public void widgetSelected(SelectionEvent event) {
			for (Button button : buttons) {
				button.setSelection(checked);
			}
		}
	}
}
